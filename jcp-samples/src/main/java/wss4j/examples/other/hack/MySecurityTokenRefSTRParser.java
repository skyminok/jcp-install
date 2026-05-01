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
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.DerivedKeyToken;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.processor.Processor;
import org.apache.ws.security.saml.SAMLKeyInfo;
import org.apache.ws.security.saml.SAMLUtil;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.str.BSPEnforcer;
import org.apache.ws.security.str.SecurityTokenRefSTRParser;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Своя реализация SecurityTokenRefSTRParser.
 */
public class MySecurityTokenRefSTRParser extends SecurityTokenRefSTRParser {

    private byte[] secretKey;

    /**
     * Get the Secret Key associated with this SecurityTokenReference
     * @return the Secret Key associated with this SecurityTokenReference
     */
    public byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Parse a SecurityTokenReference element and extract credentials.
     *
     * @param strElement The SecurityTokenReference element
     * @param data the RequestData associated with the request
     * @param wsDocInfo The WSDocInfo object to access previous processing results
     * @param parameters A set of implementation-specific parameters
     * @throws WSSecurityException
     */
    public void parseSecurityTokenReference(
            Element strElement,
            RequestData data,
            WSDocInfo wsDocInfo,
            Map<String, Object> parameters
    ) throws WSSecurityException {
        boolean bspCompliant = true;
        WSSConfig config = data.getWssConfig();
        if (config != null) {
            bspCompliant = config.isWsiBSPCompliant();
        }

        SecurityTokenReference secRef = new SecurityTokenReference(strElement, bspCompliant);

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
            processPreviousResult(result, secRef, data, parameters, wsDocInfo, bspCompliant);
        } else if (secRef.containsReference()) {
            Reference reference = secRef.getReference();
            // Try asking the CallbackHandler for the secret key
            secretKey = getSecretKeyFromToken(uri, reference.getValueType(), data);
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
                    secretKey = (byte[])bstResult.get(0).get(WSSecurityEngineResult.TAG_SECRET);
                }
            }
            if (secretKey == null) {
                throw new WSSecurityException(
                        WSSecurityException.FAILED_CHECK, "unsupportedKeyId", new Object[] {uri}
                );
            }
        } else if (secRef.containsKeyIdentifier()) {
            String valueType = secRef.getKeyIdentifierValueType();
            if (WSConstants.WSS_SAML_KI_VALUE_TYPE.equals(valueType)
                    || WSConstants.WSS_SAML2_KI_VALUE_TYPE.equals(valueType)) {
                AssertionWrapper assertion =
                        SAMLUtil.getAssertionFromKeyIdentifier(
                                secRef, strElement,
                                data, wsDocInfo
                        );
                secretKey =
                        getSecretKeyFromAssertion(assertion, secRef, data, wsDocInfo, bspCompliant);
            } else if (WSConstants.WSS_KRB_KI_VALUE_TYPE.equals(valueType)) {
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
                            break;
                        }
                    }
                }
                if (secretKey == null) {
                    throw new WSSecurityException(
                            WSSecurityException.FAILED_CHECK, "unsupportedKeyId", new Object[] {uri}
                    );
                }
            } else {
                if (bspCompliant && SecurityTokenReference.ENC_KEY_SHA1_URI.equals(valueType)) {
                    BSPEnforcer.checkEncryptedKeyBSPCompliance(secRef);
                }
                secretKey =
                        getSecretKeyFromToken(
                                secRef.getKeyIdentifierValue(), secRef.getKeyIdentifierValueType(), data
                        );
                if (secretKey == null) {
                    throw new WSSecurityException(
                            WSSecurityException.FAILED_CHECK, "unsupportedKeyId", new Object[] {uri}
                    );
                }
            }
        } else {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, "noReference");
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
            WSDocInfo wsDocInfo,
            boolean bspCompliant
    ) throws WSSecurityException {
        int action = ((Integer)result.get(WSSecurityEngineResult.TAG_ACTION)).intValue();
        if (WSConstants.ENCR == action) {
            if (bspCompliant) {
                BSPEnforcer.checkEncryptedKeyBSPCompliance(secRef);
            }
            secretKey = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
        } else if (WSConstants.DKT == action) {
            DerivedKeyToken dkt =
                    (DerivedKeyToken)result.get(WSSecurityEngineResult.TAG_DERIVED_KEY_TOKEN);
            byte[] secret =
                    (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
            String algorithm = (String)parameters.get(SIGNATURE_METHOD);
            // Запоминаем 32 байта для создания производного ключа
            // как есть, чтобы можно было передать его в виде массива
            secretKey = secret; // dkt.deriveKey(WSSecurityUtil.getKeyLength(algorithm), secret);
        } else if (WSConstants.ST_UNSIGNED == action || WSConstants.ST_SIGNED == action) {
            AssertionWrapper assertion =
                    (AssertionWrapper)result.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
            secretKey =
                    getSecretKeyFromAssertion(assertion, secRef, data, wsDocInfo, bspCompliant);
        } else if (WSConstants.SCT == action || WSConstants.BST == action) {
            secretKey = (byte[])result.get(WSSecurityEngineResult.TAG_SECRET);
        }
    }

    /**
     * Get a SecretKey from a SAML Assertion
     */
    private byte[] getSecretKeyFromAssertion(
            AssertionWrapper assertion,
            SecurityTokenReference secRef,
            RequestData data,
            WSDocInfo wsDocInfo,
            boolean bspCompliant
    ) throws WSSecurityException {
        if (bspCompliant) {
            BSPEnforcer.checkSamlTokenBSPCompliance(secRef, assertion);
        }
        SAMLKeyInfo samlKi =
                SAMLUtil.getCredentialFromSubject(assertion, data, wsDocInfo, bspCompliant);
        if (samlKi == null) {
            throw new WSSecurityException(
                    WSSecurityException.FAILED_CHECK, "invalidSAMLToken", new Object[] {"No Secret Key"}
            );
        }
        return samlKi.getSecret();
    }

    /**
     * Get the Secret Key from a CallbackHandler
     * @param id The id of the element
     * @param type The type of the element (may be null)
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

}
