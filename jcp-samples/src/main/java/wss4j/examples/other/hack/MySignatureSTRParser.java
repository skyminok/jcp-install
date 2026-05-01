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
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.*;
import org.apache.ws.security.processor.Processor;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.OpenSAMLUtil;
import org.apache.ws.security.str.BSPEnforcer;
import org.apache.ws.security.str.SignatureSTRParser;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.xml.namespace.QName;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Своя реализация SignatureSTRParser.
 * Изменения при проверке подписи на симметричном ключе (DKT).
 */
public class MySignatureSTRParser extends SignatureSTRParser {

    private X509Certificate[] certs;

    private byte[] secretKey;

    private PublicKey publicKey;

    private Principal principal;

    private boolean trustedCredential;

    /**
     * Parse a SecurityTokenReference element and extract credentials.
     *
     * @param strElement The SecurityTokenReference element
     * @param data the RequestData associated with the request
     * @param wsDocInfo The WSDocInfo object to access previous processing results
     * @param parameters A set of implementation-specific parameters
     * @throws org.apache.ws.security.WSSecurityException
     */
    public void parseSecurityTokenReference(
            Element strElement,
            RequestData data,
            WSDocInfo wsDocInfo,
            Map<String, Object> parameters
    ) throws WSSecurityException {
        boolean bspCompliant = true;
        Crypto crypto = data.getSigCrypto();
        if (data.getWssConfig() != null) {
            bspCompliant = data.getWssConfig().isWsiBSPCompliant();
        }
        SecurityTokenReference secRef = new SecurityTokenReference(strElement, bspCompliant);
        //
        // Here we get some information about the document that is being
        // processed, in particular the crypto implementation, and already
        // detected BST that may be used later during dereferencing.
        //
        String uri = null;
        if (secRef.containsReference()) {
            uri = secRef.getReference().getURI();
            if (uri.charAt(0) == '#') {
                uri = uri.substring(1);
            }
        } else if (secRef.containsKeyIdentifier()) {
            uri = secRef.getKeyIdentifierValue();
        }

        WSSecurityEngineResult result = wsDocInfo.getResult(uri);
        if (result != null) {
            // Попадаем сюда!
            processPreviousResult(result, secRef, data, parameters, bspCompliant);
        } else if (secRef.containsReference()) {
            Reference reference = secRef.getReference();
            // Try asking the CallbackHandler for the secret key
            secretKey = getSecretKeyFromToken(uri, reference.getValueType(), data);
            principal = new CustomTokenPrincipal(uri);

            if (secretKey == null) {
                Element token =
                        secRef.getTokenElement(strElement.getOwnerDocument(), wsDocInfo, data.getCallbackHandler());
                QName el = new QName(token.getNamespaceURI(), token.getLocalName());
                if (el.equals(WSSecurityEngine.BINARY_TOKEN)) {
                    Processor proc = data.getWssConfig().getProcessor(WSSecurityEngine.BINARY_TOKEN);
                    List<WSSecurityEngineResult> bstResult =
                            proc.handleToken(token, data, wsDocInfo);
                    BinarySecurity bstToken =
                            (BinarySecurity)bstResult.get(0).get(WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN);
                    if (bspCompliant) {
                        BSPEnforcer.checkBinarySecurityBSPCompliance(secRef, bstToken);
                    }
                    certs = (X509Certificate[])bstResult.get(0).get(WSSecurityEngineResult.TAG_X509_CERTIFICATES);
                    secretKey = (byte[])bstResult.get(0).get(WSSecurityEngineResult.TAG_SECRET);
                    principal = (Principal)bstResult.get(0).get(WSSecurityEngineResult.TAG_PRINCIPAL);
                } else if (el.equals(WSSecurityEngine.SAML_TOKEN)
                        || el.equals(WSSecurityEngine.SAML2_TOKEN)) {
                    Processor proc = data.getWssConfig().getProcessor(WSSecurityEngine.SAML_TOKEN);
                    //
                    // Just check to see whether the token was processed or not
                    //
                    Element processedToken =
                            secRef.findProcessedTokenElement(
                                    strElement.getOwnerDocument(), wsDocInfo,
                                    data.getCallbackHandler(), uri, secRef.getReference().getValueType()
                            );
                    AssertionWrapper assertion = null;
                    if (processedToken == null) {
                        List<WSSecurityEngineResult> samlResult =
                                proc.handleToken(token, data, wsDocInfo);
                        assertion =
                                (AssertionWrapper)samlResult.get(0).get(
                                        WSSecurityEngineResult.TAG_SAML_ASSERTION
                                );
                    } else {
                        assertion = new AssertionWrapper(processedToken);
                        assertion.parseHOKSubject(data, wsDocInfo);
                    }
                    if (bspCompliant) {
                        BSPEnforcer.checkSamlTokenBSPCompliance(secRef, assertion);
                    }
                    SAMLKeyInfo keyInfo = assertion.getSubjectKeyInfo();
                    X509Certificate[] foundCerts = keyInfo.getCerts();
                    if (foundCerts != null) {
                        certs = new X509Certificate[]{foundCerts[0]};
                    }
                    secretKey = keyInfo.getSecret();
                    principal = createPrincipalFromSAML(assertion);
                } else if (el.equals(WSSecurityEngine.ENCRYPTED_KEY)) {
                    if (bspCompliant) {
                        BSPEnforcer.checkEncryptedKeyBSPCompliance(secRef);
                    }
                    Processor proc = data.getWssConfig().getProcessor(WSSecurityEngine.ENCRYPTED_KEY);
                    List<WSSecurityEngineResult> encrResult =
                            proc.handleToken(token, data, wsDocInfo);
                    secretKey =
                            (byte[])encrResult.get(0).get(WSSecurityEngineResult.TAG_SECRET);
                    principal = new CustomTokenPrincipal(token.getAttribute("Id"));
                }
            }
        } else if (secRef.containsX509Data() || secRef.containsX509IssuerSerial()) {
            X509Certificate[] foundCerts = secRef.getX509IssuerSerial(crypto);
            if (foundCerts != null) {
                certs = new X509Certificate[]{foundCerts[0]};
            }
        } else if (secRef.containsKeyIdentifier()) {
            if (secRef.getKeyIdentifierValueType().equals(SecurityTokenReference.ENC_KEY_SHA1_URI)) {
                if (bspCompliant) {
                    BSPEnforcer.checkEncryptedKeyBSPCompliance(secRef);
                }
                String id = secRef.getKeyIdentifierValue();
                secretKey =
                        getSecretKeyFromToken(id, SecurityTokenReference.ENC_KEY_SHA1_URI, data);
                principal = new CustomTokenPrincipal(id);
            } else if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())
                    || WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(secRef.getKeyIdentifierValueType())) {
                AssertionWrapper assertion =
                        SAMLUtil.getAssertionFromKeyIdentifier(
                                secRef, strElement, data, wsDocInfo
                        );
                if (bspCompliant) {
                    BSPEnforcer.checkSamlTokenBSPCompliance(secRef, assertion);
                }
                SAMLKeyInfo samlKi =
                        SAMLUtil.getCredentialFromSubject(assertion, data,
                                wsDocInfo, bspCompliant);
                X509Certificate[] foundCerts = samlKi.getCerts();
                if (foundCerts != null) {
                    certs = new X509Certificate[]{foundCerts[0]};
                }
                secretKey = samlKi.getSecret();
                publicKey = samlKi.getPublicKey();
                principal = createPrincipalFromSAML(assertion);
            } else {
                parseBSTKeyIdentifier(secRef, crypto, wsDocInfo, data, bspCompliant);
            }
        } else {
            throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY,
                    "unsupportedKeyInfo",
                    new Object[]{strElement.toString()}
            );
        }

        if (certs != null && principal == null) {
            principal = certs[0].getSubjectX500Principal();
        }
    }

    /**
     * Process a previous security result
     */
    private void processPreviousResult(
            WSSecurityEngineResult result,
            SecurityTokenReference secRef,
            RequestData data,
            Map<String, Object> parameters,
            boolean bspCompliant
    ) throws WSSecurityException {
        int action = ((Integer)result.get(WSSecurityEngineResult.TAG_ACTION)).intValue();
        if (WSConstants.UT_NOPASSWORD == action || WSConstants.UT == action) {
            if (bspCompliant) {
                BSPEnforcer.checkUsernameTokenBSPCompliance(secRef);
            }
            UsernameToken usernameToken =
                    (UsernameToken)result.get(WSSecurityEngineResult.TAG_USERNAME_TOKEN);

            usernameToken.setRawPassword(data);
            if (usernameToken.isDerivedKey()) {
                secretKey = usernameToken.getDerivedKey();
            } else {
                int keyLength = ((Integer)parameters.get(SECRET_KEY_LENGTH)).intValue();
                secretKey = usernameToken.getSecretKey(keyLength);
            }
            principal = usernameToken.createPrincipal();
        } else if (WSConstants.BST == action) {
            if (bspCompliant) {
                BinarySecurity token =
                        (BinarySecurity)result.get(
                                WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN
                        );
                BSPEnforcer.checkBinarySecurityBSPCompliance(secRef, token);
            }
            certs =
                    (X509Certificate[])result.get(WSSecurityEngineResult.TAG_X509_CERTIFICATES);
            secretKey = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
            Boolean validatedToken =
                    (Boolean)result.get(WSSecurityEngineResult.TAG_VALIDATED_TOKEN);
            if (validatedToken.booleanValue()) {
                trustedCredential = true;
            }
        } else if (WSConstants.ENCR == action) {
            if (bspCompliant) {
                BSPEnforcer.checkEncryptedKeyBSPCompliance(secRef);
            }
            secretKey = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
            String id = (String)result.get(WSSecurityEngineResult.TAG_ID);
            principal = new CustomTokenPrincipal(id);
        } else if (WSConstants.SCT == action) {
            secretKey = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
            SecurityContextToken sct =
                    (SecurityContextToken)result.get(
                            WSSecurityEngineResult.TAG_SECURITY_CONTEXT_TOKEN
                    );
            principal = new CustomTokenPrincipal(sct.getIdentifier());
        } else if (WSConstants.DKT == action) {
            DerivedKeyToken dkt =
                    (DerivedKeyToken)result.get(WSSecurityEngineResult.TAG_DERIVED_KEY_TOKEN);
            int keyLength = dkt.getLength();
            if (keyLength <= 0) {
                String algorithm = (String)parameters.get(SIGNATURE_METHOD);
                keyLength = WSSecurityUtil.getKeyLength(algorithm);
            }
            byte[] secret = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
            // Запоминаем 32 байта для создания производного ключа
            // как есть, чтобы можно было передать его в виде массива.
            // Используется при проверке подписи на симметричном ключе.
            secretKey = secret; // dkt.deriveKey(keyLength, secret);
            principal = dkt.createPrincipal();
            ((WSDerivedKeyTokenPrincipal)principal).setSecret(secret);
        } else if (WSConstants.ST_UNSIGNED == action || WSConstants.ST_SIGNED == action) {
            AssertionWrapper assertion =
                    (AssertionWrapper)result.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
            if (bspCompliant) {
                BSPEnforcer.checkSamlTokenBSPCompliance(secRef, assertion);
            }
            SAMLKeyInfo keyInfo = assertion.getSubjectKeyInfo();
            if (keyInfo == null) {
                throw new WSSecurityException(
                        WSSecurityException.FAILURE, "invalidSAMLsecurity"
                );
            }
            X509Certificate[] foundCerts = keyInfo.getCerts();
            if (foundCerts != null) {
                certs = new X509Certificate[]{foundCerts[0]};
            }
            secretKey = keyInfo.getSecret();
            publicKey = keyInfo.getPublicKey();
            principal = createPrincipalFromSAML(assertion);
        }
    }

    /**
     * Get the Secret Key from a CallbackHandler
     * @param id The id of the element
     * @param type The type of the element (can be null)
     * @return A Secret Key
     * @throws WSSecurityException
     */
    private byte[] getSecretKeyFromToken(
            String id,
            String type,
            RequestData data
    ) throws WSSecurityException {
        if (id.charAt(0) == '#') {
            id = id.substring(1);
        }
        WSPasswordCallback pwcb =
                new WSPasswordCallback(id, null, type, WSPasswordCallback.SECRET_KEY, data);
        try {
            Callback[] callbacks = new Callback[]{pwcb};
            if (data.getCallbackHandler() != null) {
                data.getCallbackHandler().handle(callbacks);
                return pwcb.getKey();
            }
        } catch (Exception e) {
            throw new WSSecurityException(
                    WSSecurityException.FAILURE,
                    "noPassword",
                    new Object[] {id},
                    e
            );
        }

        return null;
    }

    /**
     * Get the X509Certificates associated with this SecurityTokenReference
     * @return the X509Certificates associated with this SecurityTokenReference
     */
    public X509Certificate[] getCertificates() {
        return certs;
    }

    /**
     * Get the Principal associated with this SecurityTokenReference
     * @return the Principal associated with this SecurityTokenReference
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * Get the PublicKey associated with this SecurityTokenReference
     * @return the PublicKey associated with this SecurityTokenReference
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Get the Secret Key associated with this SecurityTokenReference
     * @return the Secret Key associated with this SecurityTokenReference
     */
    public byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Get whether the returned credential is already trusted or not. This is currently
     * applicable in the case of a credential extracted from a trusted HOK SAML Assertion,
     * and a BinarySecurityToken that has been processed by a Validator. In these cases,
     * the SignatureProcessor does not need to verify trust on the credential.
     * @return true if trust has already been verified on the returned Credential
     */
    public boolean isTrustedCredential() {
        return trustedCredential;
    }

    /**
     * A method to create a Principal from a SAML Assertion
     * @param assertion An AssertionWrapper object
     * @return A principal
     */
    private Principal createPrincipalFromSAML(
            AssertionWrapper assertion
    ) {
        SAMLTokenPrincipal principal = new SAMLTokenPrincipal(assertion);
        String confirmMethod = null;
        List<String> methods = assertion.getConfirmationMethods();
        if (methods != null && methods.size() > 0) {
            confirmMethod = methods.get(0);
        }
        if (OpenSAMLUtil.isMethodHolderOfKey(confirmMethod) && assertion.isSigned()) {
            trustedCredential = true;
        }
        return principal;
    }

    /**
     * Parse the KeyIdentifier for a BinarySecurityToken
     */
    private void parseBSTKeyIdentifier(
            SecurityTokenReference secRef,
            Crypto crypto,
            WSDocInfo wsDocInfo,
            RequestData data,
            boolean bspCompliant
    ) throws WSSecurityException {
        if (bspCompliant) {
            BSPEnforcer.checkBinarySecurityBSPCompliance(secRef, null);
        }
        String valueType = secRef.getKeyIdentifierValueType();
        if (WSConstants.WSS_KRB_KI_VALUE_TYPE.equals(valueType)) {
            secretKey =
                    getSecretKeyFromToken(secRef.getKeyIdentifierValue(), valueType, data);
            if (secretKey == null) {
                byte[] keyBytes = secRef.getSKIBytes();
                List<WSSecurityEngineResult> resultsList =
                        wsDocInfo.getResultsByTag(WSConstants.BST);
                for (WSSecurityEngineResult bstResult : resultsList) {
                    BinarySecurity bstToken =
                            (BinarySecurity)bstResult.get(WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN);
                    byte[] tokenDigest = WSSecurityUtil.generateDigest(bstToken.getToken());
                    if (Arrays.equals(tokenDigest, keyBytes)) {
                        secretKey = (byte[])bstResult.get(WSSecurityEngineResult.TAG_SECRET);
                        principal = (Principal)bstResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                        break;
                    }
                }
            } else {
                principal = new CustomTokenPrincipal(secRef.getKeyIdentifierValue());
            }
        } else {
            X509Certificate[] foundCerts = secRef.getKeyIdentifier(crypto);
            if (foundCerts != null) {
                certs = new X509Certificate[]{foundCerts[0]};
            }
        }
    }

}
