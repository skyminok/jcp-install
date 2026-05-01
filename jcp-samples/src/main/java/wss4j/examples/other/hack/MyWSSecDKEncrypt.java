/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package wss4j.examples.other.hack;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.message.token.KerberosSecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.Key.SecretKeyInterface;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.params.CryptParamsInterface;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.util.List;

/**
 * Своя реализация WSSecDKEncrypt (добавление узла
 * DerivedKeyToken и шифрование).
 * Функция prepare(Document) переопределена
 */
public class MyWSSecDKEncrypt extends WSSecDKEncrypt {

    private int wscVersion = ConversationConstants.DEFAULT_VERSION;
    private String customValueType;
    private SecretKeySpec secretKeySpec;

    public MyWSSecDKEncrypt() {
        super();
    }
    public MyWSSecDKEncrypt(WSSConfig config) {
        super(config);
    }

    public void setCustomValueType(String customValueType) {
        this.customValueType = customValueType;
    }

    public void setExternalKey(SecretKey key, String tokenIdentifier) {
        secretKeySpec = (SecretKeySpec) ((GostSecretKey) key).getSpec();
        this.tokenIdentifier = tokenIdentifier;
    }

    public void prepare(Document doc) throws WSSecurityException, ConversationException {

        document = doc;

        // Create the derived keys
        // At this point figure out the key length according to the symencAlgo
        int offset = 0;
        //int length = getDerivedKeyLength();
        byte[] label;
        try {
            label = (clientLabel + serviceLabel).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new WSSecurityException("UTF-8 encoding is not supported", e);
        }
        byte[] nonce = WSSecurityUtil.generateNonce(16);

        byte[] value = new byte[label.length + nonce.length];
        System.arraycopy(label, 0, value, 0, label.length);
        System.arraycopy(nonce, 0, value, label.length, nonce.length);

        byte[][] data = new byte[1][];
        data[0] = value;

        //DerivationAlgorithm algo =
        //        AlgoFactory.getInstance(ConversationConstants.DerivationAlgorithm.P_SHA_1);

        // Байты будущего производного ключа
        derivedKeyBytes = new byte[32]; //algo.createKey(ephemeralKey, seed, offset, length);

        try {
            // Генерим массив для производного ключа
            secretKeySpec.methodGOSTR3411PRF(data, derivedKeyBytes, false);
        }
        catch (Exception e) {
            throw new WSSecurityException(e.getMessage(), e);
        }

        // Add the DKTs
        dkt = new DerivedKeyToken(wscVersion, document);
        dktId = getWsConfig().getIdAllocator().createId("DK-", dkt);

        // Добавляем алгоритм в DKT
        dkt.setAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:dk-p-gostr3411");

        //dkt.setOffset(offset);
        //dkt.setLength(length);
        dkt.setNonce(Base64.encode(nonce));
        dkt.setID(dktId);

        if (strElem == null) {
            SecurityTokenReference secRef = new SecurityTokenReference(document);
            String strUri = getWsConfig().getIdAllocator().createSecureId("STR-", secRef);
            secRef.setID(strUri);

            switch (keyIdentifierType) {
                case WSConstants.CUSTOM_KEY_IDENTIFIER:
                    secRef.setKeyIdentifier(customValueType, tokenIdentifier);
                    if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_SAML_TOKEN_TYPE);
                    } else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_SAML2_TOKEN_TYPE);
                    } else if (WSConstants.WSS_ENC_KEY_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_ENC_KEY_VALUE_TYPE);
                    }
                    break;
                default:
                    Reference ref = new Reference(document);

                    if (tokenIdDirectId) {
                        ref.setURI(tokenIdentifier);
                    } else {
                        ref.setURI("#" + tokenIdentifier);
                    }
                    if (customValueType != null && !"".equals(customValueType)) {
                        ref.setValueType(customValueType);
                    }
                    if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_SAML_TOKEN_TYPE);
                        ref.setValueType(customValueType);
                    } else if (WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_SAML2_TOKEN_TYPE);
                    } else if (WSConstants.WSS_ENC_KEY_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_ENC_KEY_VALUE_TYPE);
                        ref.setValueType(customValueType);
                    } else if (KerberosSecurity.isKerberosToken(customValueType)) {
                        secRef.addTokenType(customValueType);
                        ref.setValueType(customValueType);
                    } else if (WSConstants.WSC_SCT.equals(customValueType)) {
                        ref.setValueType(customValueType);
                    } else if (!WSConstants.WSS_USERNAME_TOKEN_VALUE_TYPE.equals(customValueType)) {
                        secRef.addTokenType(WSConstants.WSS_ENC_KEY_VALUE_TYPE);
                    }

                    secRef.setReference(ref);
            }

            dkt.setSecurityTokenReference(secRef);
        } else {
            dkt.setSecurityTokenReference(strElem);
        }
    }

    private KeyInfo createKeyInfo() throws WSSecurityException {
        KeyInfo keyInfo = new KeyInfo(document);
        SecurityTokenReference secToken = new SecurityTokenReference(document);
        secToken.addWSSENamespace();

        Reference ref = new Reference(document);
        ref.setURI("#" + dktId);

        // Добавляем тип узла
        ref.setValueType("http://schemas.xmlsoap.org/ws/2005/02/sc/dk");

        secToken.setReference(ref);

        keyInfo.addUnknownElement(secToken.getElement());
        Element keyInfoElement = keyInfo.getElement();
        keyInfoElement.setAttributeNS(
                WSConstants.XMLNS_NS, "xmlns:" + WSConstants.SIG_PREFIX, WSConstants.SIG_NS
        );

        return keyInfo;
    }

    public Element encryptForExternalRef(Element dataRef, List<WSEncryptionPart> references)
            throws WSSecurityException {

        KeyInfo keyInfo = createKeyInfo();
        //SecretKey key = WSSecurityUtil.prepareSecretKey(symEncAlgo, derivedKeyBytes);

        SecretKeyInterface derivedSecretKeySpec = null;

        try {
            // Создаем производный ключ
            derivedSecretKeySpec = new SecretKeySpec(derivedKeyBytes,
                    (CryptParamsInterface)secretKeySpec.getParams());
        } catch (KeyManagementException e) {
            throw new WSSecurityException(e.getMessage(), e);
        }

        SecretKey key = new GostSecretKey(derivedSecretKeySpec);

        List<String> encDataRefs =
                WSSecEncrypt.doEncryption(
                        document, getWsConfig(), keyInfo, key, symEncAlgo, references, callbackLookup
                );
        if (dataRef == null) {
            dataRef =
                    document.createElementNS(
                            WSConstants.ENC_NS, WSConstants.ENC_PREFIX + ":ReferenceList"
                    );
        }
        return WSSecEncrypt.createDataRefList(document, dataRef, encDataRefs);
    }

}
