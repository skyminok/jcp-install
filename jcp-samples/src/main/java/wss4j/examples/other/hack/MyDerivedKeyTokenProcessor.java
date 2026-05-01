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
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.processor.DerivedKeyTokenProcessor;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.tools.Decoder;
import wss4j.examples.other.CallbackHandlers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Своя реализация DerivedKeyTokenProcessor (хранение алгоритма
 * получения ключа, nonce, ссылки на EncryptedKey).
 */
public class MyDerivedKeyTokenProcessor extends DerivedKeyTokenProcessor {

    public List<WSSecurityEngineResult> handleToken(
            Element elem,
            RequestData data,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {

        Document doc = wsDocInfo.getDocument();
        String actor = null;
        Element securityHeader = WSSecurityUtil.getSecurityHeader(doc, actor);

        // Для создания derived key на нужен секретный ключ, который обычно лежит
        // в EncryptedKey в зашифрованном виде. При этом расшифровать ключ может
        // только сервис, т.к. он обладает закрытым ключом. Клиенту остается
        // закешировать ключ и использовать. Наличие callback указанного ниже типа
        // означает, что клиент уже поместил туда секретный ключ и в данном случае
        // пытается проверить сообщение сервиса.

        // Deserialize the DKT
        DerivedKeyToken dkt = new DerivedKeyToken(elem, data.getWssConfig().isWsiBSPCompliant());

        // Кешированный секретный ключ. Получим его либо из callback,
        // либо из EncryptedKey
        SecretKeySpec cachedSecretKey = null;

        CallbackHandler callbackHandler = data.getCallbackHandler();
        if (callbackHandler != null) {

            if (callbackHandler instanceof
                CallbackHandlers.SecretKeyAndKeyStoreCallbackHandler) {

                // Хеш wrapped key, использовавшегося в первом сообщении.
                // Ему соответствует реальный wrapped key.
                String identifier = dkt.getSecurityTokenReference().getKeyIdentifierValue();

                // Явное приведение
                cachedSecretKey = ((CallbackHandlers.SecretKeyAndKeyStoreCallbackHandler)
                    callbackHandler).getKey(identifier);
            } // if
        } // if

        //Node node = securityHeader.getFirstChild();
        //Node nextSibling = node.getNextSibling();

        // Если нет явного указания секретного ключа, то пробуем искать
        // EncryptedKey для его извлечения.
        if (cachedSecretKey == null) {

            boolean foundEncryptedKey = false;

            Node node = securityHeader.getFirstChild();
            while (node != null) {

                Node nextSibling = node.getNextSibling();
                if (Node.ELEMENT_NODE == node.getNodeType()) {

                    QName el = new QName(node.getNamespaceURI(), node.getLocalName());
                    System.out.println(el.getLocalPart());

                    // Нашли EncryptedKey
                    if (el.getLocalPart().equalsIgnoreCase("EncryptedKey")) {
                        foundEncryptedKey = true;
                        break;
                    } // if
                } // if

                //
                // If the next sibling is null and the stored next sibling is not null, then we have
                // encountered an EncryptedData element which was decrypted, and so the next sibling
                // of the current node is null. In that case, go on to the previously stored next
                // sibling
                //
                if (node.getNextSibling() == null && nextSibling != null) {
                    node = nextSibling;
                } else {
                    node = node.getNextSibling();
                }
            }

            if (!foundEncryptedKey) {
                throw new WSSecurityException("Cached secret key is absent and EncryptedKey is not found.");
            } // if

            // Декодируем EncryptedKey. Снова работаем с MyEncryptedKeyProcessor,
            // потому что WSSecurityEngineResult не может нормально передать SecretKey
            // при первом вызове.
            List<WSSecurityEngineResult> results =
                new MyEncryptedKeyProcessor().handleToken((Element) node, data, wsDocInfo);

            WSSecurityEngineResult encResult = results.get(0);

            // Транспортный алгоритм
            String encryptedKeyTransportMethod =
                (String) encResult.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_TRANSPORT_METHOD);

            X509Certificate cert = (X509Certificate) encResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
            // Зашифрованный секретный ключ (wrapped key)
            byte[] encryptedKey = (byte[]) encResult.get(WSSecurityEngineResult.TAG_ENCRYPTED_EPHEMERAL_KEY);
            PrivateKey privateKey = data.getDecCrypto().getPrivateKey(cert, data.getCallbackHandler());

            // Расшифрование зашифрованного секретного ключа (wrapped key)
            Cipher cipher = WSSecurityUtil.getCipherInstance(encryptedKeyTransportMethod);

            SecretKey decryptedSecretKey = null;

            try {
                cipher.init(Cipher.UNWRAP_MODE, privateKey);
                decryptedSecretKey = (SecretKey) cipher.unwrap(encryptedKey, null, Cipher.SECRET_KEY);
            } catch (Exception e) {
                throw new WSSecurityException(e.getMessage(), e);
            }

            cachedSecretKey = (SecretKeySpec) ((GostSecretKey) decryptedSecretKey).getSpec();

        } // if

        /* Пока не нужно.
        byte[] secret = null;
        Element secRefElement = dkt.getSecurityTokenReferenceElement();
        if (secRefElement != null) {
            STRParser strParser = new DerivedKeyTokenSTRParser();
            strParser.parseSecurityTokenReference(
                secRefElement, data, wsDocInfo, null);
            secret = strParser.getSecretKey();
        } else {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, "noReference");
        }
        */

        String tempNonce = dkt.getNonce();
        if (tempNonce == null) {
            throw new WSSecurityException("Missing wsc:Nonce value");
        }

        Decoder decoder = new Decoder();
        byte[] nonce = null;

        try {
            nonce = decoder.decodeBuffer(tempNonce);
        } catch (IOException e) {
            throw new WSSecurityException(e.getMessage(), e);
        }

        //int length = dkt.getLength();
        //byte[] keyBytes = dkt.deriveKey(length, secret);

        byte[] keyBytes = new byte[SecretKeySpec.KEY_LEN];

        byte[] label;
        try {
            label = (ConversationConstants.DEFAULT_LABEL + ConversationConstants.DEFAULT_LABEL).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new WSSecurityException("UTF-8 encoding is not supported", e);
        }

        byte[] value = new byte[label.length + nonce.length];
        System.arraycopy(label, 0, value, 0, label.length);
        System.arraycopy(nonce, 0, value, label.length, nonce.length);

        byte[][] digest = new byte[1][];
        digest[0] = value;

        try {
            // Получаем 32 байта производного ключа keyBytes
            cachedSecretKey.methodGOSTR3411PRF(digest, keyBytes, false);
        } catch (InvalidKeyException e) {
            throw new WSSecurityException(e.getMessage(), e);
        }

        WSSecurityEngineResult result =
                new WSSecurityEngineResult(WSConstants.DKT, null, keyBytes, null);
        wsDocInfo.addTokenElement(elem);
        result.put(WSSecurityEngineResult.TAG_ID, dkt.getID());
        result.put(WSSecurityEngineResult.TAG_DERIVED_KEY_TOKEN, dkt);
        result.put(WSSecurityEngineResult.TAG_SECRET, /*secret*/keyBytes);
        result.put(WSSecurityEngineResult.TAG_TOKEN_ELEMENT, dkt.getElement());
        wsDocInfo.addResult(result);

        return java.util.Collections.singletonList(result);
    }

}
