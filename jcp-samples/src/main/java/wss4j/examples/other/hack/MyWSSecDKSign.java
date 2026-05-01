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

import org.apache.ws.security.*;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.message.token.KerberosSecurity;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.Key.SecretKeyInterface;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI;

import javax.crypto.SecretKey;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

/**
 * Своя реализация WSSecDKSign (добавление узла
 * DerivedKeyToken и подпись на симметричном ключе).
 * Функция prepare(Document) переопределена
 */
public class MyWSSecDKSign extends WSSecDKSign {

    private int wscVersion = ConversationConstants.DEFAULT_VERSION;
    private String customValueType;
    private SecretKeySpec secretKeySpec;
    private KeyInfoFactory keyInfoFactory ;
    private XMLSignatureFactory signatureFactory;
    private CanonicalizationMethod c14nMethod;
    private XMLSignature sig;
    private WSDocInfo wsDocInfo;
    private Element securityHeader = null;
    private KeyInfo keyInfo;
    private String keyInfoUri = null;
    private SecurityTokenReference secRef = null;
    private String strUri = null;
    private byte[] signatureValue = null;

    public MyWSSecDKSign() {
        super();
        init();
    }

    public MyWSSecDKSign(WSSConfig config) {
        super(config);
        init();
    }

    private void init() {

        try {
            keyInfoFactory = KeyInfoFactory.getInstance("DOM", XMLDSigRI.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

        try {
            signatureFactory = XMLSignatureFactory.getInstance("DOM", XMLDSigRI.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    public void setCustomValueType(String customValueType) {
        this.customValueType = customValueType;
    }

    public void setExternalKey(SecretKey key, String tokenIdentifier) {
        secretKeySpec = (SecretKeySpec) ((GostSecretKey) key).getSpec();
        this.tokenIdentifier = tokenIdentifier;
    }

    public String getSignatureId() {
        if (sig == null) {
            return null;
        }
        return sig.getId();
    }

    public SecurityTokenReference getSecurityTokenReference() {
        return secRef;
    }

    /**
     * Returns the SignatureElement.
     * The method can be called any time after <code>prepare()</code>.
     * @return The DOM Element of the signature.
     */
    public Element getSignatureElement() {
        return
                WSSecurityUtil.getDirectChildElement(
                        securityHeader,
                        WSConstants.SIG_LN,
                        WSConstants.SIG_NS
                );
    }

    public Document build(Document doc, WSSecHeader secHeader)
            throws WSSecurityException, ConversationException {

        prepare(doc, secHeader);
        String soapNamespace = WSSecurityUtil.getSOAPNamespace(doc.getDocumentElement());
        if (parts == null) {
            parts = new ArrayList<WSEncryptionPart>(1);
            WSEncryptionPart encP =
                    new WSEncryptionPart(
                            WSConstants.ELEM_BODY,
                            soapNamespace,
                            "Content"
                    );
            parts.add(encP);
        } else {
            for (WSEncryptionPart part : parts) {
                if ("STRTransform".equals(part.getName()) && part.getId() == null) {
                    part.setId(strUri);
                }
            }
        }

        List<javax.xml.crypto.dsig.Reference> referenceList =
                addReferencesToSign(parts, secHeader);
        computeSignature(referenceList);

        //
        // prepend elements in the right order to the security header
        //
        prependDKElementToHeader(secHeader);

        return doc;
    }

    /**
     * Initialize a WSSec Derived key.
     *
     * The method prepares and initializes a WSSec derived key structure after the
     * relevant information was set. This method also creates and initializes the
     * derived token using the ephemeral key. After preparation references
     * can be added, encrypted and signed as required.
     *
     * This method does not add any element to the security header. This must be
     * done explicitly.
     *
     * @param doc The unsigned SOAP envelope as <code>Document</code>
     * @throws org.apache.ws.security.WSSecurityException
     */
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

    public void prepare(Document doc, WSSecHeader secHeader)
            throws WSSecurityException, ConversationException {
        prepare(doc);
        wsDocInfo = new WSDocInfo(doc);
        securityHeader = secHeader.getSecurityHeader();
        sig = null;

        try {
            C14NMethodParameterSpec c14nSpec = null;
            if (getWsConfig().isWsiBSPCompliant() &&
                getSigCanonicalization().equals(WSConstants.C14N_EXCL_OMIT_COMMENTS)) {

                List<String> prefixes =
                        getInclusivePrefixes(secHeader.getSecurityHeader(), false);
                c14nSpec = new ExcC14NParameterSpec(prefixes);
            }

            c14nMethod = signatureFactory.newCanonicalizationMethod(getSigCanonicalization(), c14nSpec);
        } catch (Exception ex) {
            //log.error("", ex);
            throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, "noXMLSig", null, ex
            );
        }

        keyInfoUri = getWsConfig().getIdAllocator().createSecureId("KI-", keyInfo);

        secRef = new SecurityTokenReference(doc);
        strUri = getWsConfig().getIdAllocator().createSecureId("STR-", secRef);
        secRef.setID(strUri);

        Reference refUt = new Reference(document);

        // Добавляем тип узла
        refUt.setValueType("http://schemas.xmlsoap.org/ws/2005/02/sc/dk");

        refUt.setURI("#" + dktId);
        secRef.setReference(refUt);

        XMLStructure structure = new DOMStructure(secRef.getElement());
        wsDocInfo.addTokenElement(secRef.getElement(), false);
        keyInfo =
                keyInfoFactory.newKeyInfo(
                        java.util.Collections.singletonList(structure), keyInfoUri
                );

    }

    /**
     * This method adds references to the Signature.
     *
     * @param references The list of references to sign
     * @param secHeader The Security Header
     * @throws WSSecurityException
     */
    public List<javax.xml.crypto.dsig.Reference> addReferencesToSign(
            List<WSEncryptionPart> references,
            WSSecHeader secHeader
    ) throws WSSecurityException {
        return
                addReferencesToSign(
                        document,
                        references,
                        wsDocInfo,
                        signatureFactory,
                        secHeader,
                        getWsConfig(),
                        getDigestAlgorithm()
                );
    }

    /**
     * Compute the Signature over the references.
     *
     * After references are set this method computes the Signature for them.
     * This method can be called any time after the references were set. See
     * <code>addReferencesToSign()</code>.
     *
     * @throws WSSecurityException
     */
    public void computeSignature(
            List<javax.xml.crypto.dsig.Reference> referenceList
    ) throws WSSecurityException {
        computeSignature(referenceList, true, null);
    }

    /**
     * Compute the Signature over the references.
     *
     * After references are set this method computes the Signature for them.
     * This method can be called any time after the references were set. See
     * <code>addReferencesToSign()</code>.
     *
     * @throws WSSecurityException
     */
    public void computeSignature(
            List<javax.xml.crypto.dsig.Reference> referenceList,
            boolean prepend,
            Element siblingElement
    ) throws WSSecurityException {
        try {

            //java.security.Key key = WSSecurityUtil.prepareSecretKey(getSignatureAlgorithm(), derivedKeyBytes);

            SecretKeyInterface derivedSecretKeySpec = null;

            try {
                // Создаем производный ключ
                derivedSecretKeySpec = new SecretKeySpec(derivedKeyBytes,
                        (CryptParamsInterface)secretKeySpec.getParams());
            } catch (KeyManagementException e) {
                throw new WSSecurityException(e.getMessage(), e);
            }

            SecretKey key = new GostSecretKey(derivedSecretKeySpec);

            SignatureMethod signatureMethod =
                    signatureFactory.newSignatureMethod(getSignatureAlgorithm(), null);

            SignedInfo signedInfo =
                    signatureFactory.newSignedInfo(c14nMethod, signatureMethod, referenceList);

            sig = signatureFactory.newXMLSignature(
                    signedInfo,
                    keyInfo,
                    null,
                    getWsConfig().getIdAllocator().createId("SIG-", null),
                    null);

            //
            // Figure out where to insert the signature element
            //
            XMLSignContext signContext = null;
            if (prepend) {
                if (siblingElement == null) {
                    siblingElement = (Element)securityHeader.getFirstChild();
                }
                if (siblingElement == null) {
                    signContext = new DOMSignContext(key, securityHeader);
                } else {
                    signContext = new DOMSignContext(key, securityHeader, siblingElement);
                }
            } else {
                signContext = new DOMSignContext(key, securityHeader);
            }

            signContext.putNamespacePrefix(WSConstants.SIG_NS, WSConstants.SIG_PREFIX);
            if (WSConstants.C14N_EXCL_OMIT_COMMENTS.equals(getSigCanonicalization())) {
                signContext.putNamespacePrefix(
                        WSConstants.C14N_EXCL_OMIT_COMMENTS,
                        WSConstants.C14N_EXCL_OMIT_COMMENTS_PREFIX
                );
            }
            signContext.setProperty(STRTransform.TRANSFORM_WS_DOC_INFO, wsDocInfo);
            wsDocInfo.setCallbackLookup(callbackLookup);

            // Add the elements to sign to the Signature Context
            wsDocInfo.setTokensOnContext((DOMSignContext)signContext);
            if (secRef != null && secRef.getElement() != null) {
                WSSecurityUtil.storeElementInContext((DOMSignContext)signContext, secRef.getElement());
            }

            sig.sign(signContext);

            signatureValue = sig.getSignatureValue().getValue();
        } catch (Exception ex) {
            //log.error(ex);
            throw new WSSecurityException(
                    WSSecurityException.FAILED_SIGNATURE, null, null, ex
            );
        }
    }

    /**
     * @return Returns the signatureValue.
     */
    public byte[] getSignatureValue() {
        return signatureValue;
    }
}
