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
import org.apache.ws.security.components.crypto.CryptoType;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.processor.EncryptedKeyProcessor;
import org.apache.ws.security.processor.ReferenceListProcessor;
import org.apache.ws.security.processor.X509Util;
import org.apache.ws.security.str.EncryptedKeySTRParser;
import org.apache.ws.security.str.STRParser;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.crypto.SecretKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Своя реализация EncryptedKeyProcessor (хранение зашифрованного
 * секретного ключа с ссылкой на сертификат шифрования).
 */
public class MyEncryptedKeyProcessor extends EncryptedKeyProcessor {

    public List<WSSecurityEngineResult> handleToken(
            Element elem,
            RequestData data,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {

        if (data.getDecCrypto() == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noDecCryptoFile");
        }
        if (data.getCallbackHandler() == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCallback");
        }

        //
        // lookup xenc:EncryptionMethod, get the Algorithm attribute to determine
        // how the key was encrypted. Then check if we support the algorithm
        //
        String encryptedKeyTransportMethod = X509Util.getEncAlgo(elem);
        if (encryptedKeyTransportMethod == null) {
            throw new WSSecurityException(
                    WSSecurityException.UNSUPPORTED_ALGORITHM, "noEncAlgo"
            );
        }
        //if (data.getWssConfig().isWsiBSPCompliant()) {
        //    checkBSPCompliance(elem, encryptedKeyTransportMethod);
        //}

        // Ничего не расшифровываем, сохраняем зашифрованный
        // секретный ключ в виде массива, потому что передать
        // чистый SecretKey не можем.

        //Cipher cipher = WSSecurityUtil.getCipherInstance(encryptedKeyTransportMethod);
        //
        // Now lookup CipherValue.
        //
        Element tmpE =
                WSSecurityUtil.getDirectChildElement(
                        elem, "CipherData", WSConstants.ENC_NS
                );

        Element xencCipherValue = null;
        if (tmpE != null) {
            xencCipherValue =
                    WSSecurityUtil.getDirectChildElement(tmpE, "CipherValue", WSConstants.ENC_NS);
        }

        if (xencCipherValue == null) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noCipher");
        }

        X509Certificate[] certs =
                getCertificatesFromEncryptedKey(elem, data, data.getDecCrypto(), wsDocInfo);

        try {
            //PrivateKey privateKey = data.getDecCrypto().getPrivateKey(certs[0], data.getCallbackHandler());
            //cipher.init(Cipher.UNWRAP_MODE, privateKey);
        } catch (Exception ex) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, ex);
        }

        List<String> dataRefURIs = getDataRefURIs(elem);

        byte[] encryptedEphemeralKey = null;
        //byte[] decryptedBytes = null;
        SecretKey decryptedSecretKey = null;
        try {
            encryptedEphemeralKey = getDecodedBase64EncodedData(xencCipherValue);
            /*decryptedBytes*/
            //decryptedSecretKey =
            //    (SecretKey) cipher.unwrap(encryptedEphemeralKey, null, Cipher.SECRET_KEY); //doFinal(encryptedEphemeralKey);
        } catch (IllegalStateException ex) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK, null, null, ex);
        } catch (Exception ex) {
            //decryptedBytes = getRandomKey(dataRefURIs, elem.getOwnerDocument(), wsDocInfo);
        }

        List<WSDataRef> dataRefs =
                decryptDataRefs(dataRefURIs, elem.getOwnerDocument(),
                        wsDocInfo, /*decryptedBytes*/decryptedSecretKey);

        // Записываем сюда зашифрованный секретный ключ (wrapped key)
        WSSecurityEngineResult result = new WSSecurityEngineResult(
                WSConstants.ENCR,
                /*decryptedBytes*/null,
                encryptedEphemeralKey,
                dataRefs,
                certs
        );

        result.put(
                WSSecurityEngineResult.TAG_ENCRYPTED_KEY_TRANSPORT_METHOD,
                encryptedKeyTransportMethod
        );

        result.put(WSSecurityEngineResult.TAG_ID, elem.getAttribute("Id"));
        wsDocInfo.addResult(result);
        wsDocInfo.addTokenElement(elem);

        return java.util.Collections.singletonList(result);
    }

    /**
     * @return the Certificate(s) corresponding to the public key reference in the
     * EncryptedKey Element
     */
    private X509Certificate[] getCertificatesFromEncryptedKey(
            Element xencEncryptedKey,
            RequestData data,
            Crypto crypto,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {
        Element keyInfo =
                WSSecurityUtil.getDirectChildElement(
                        xencEncryptedKey, "KeyInfo", WSConstants.SIG_NS
                );
        if (keyInfo != null) {
            Element strElement = null;
            if (data.getWssConfig().isWsiBSPCompliant()) {
                int result = 0;
                Node node = keyInfo.getFirstChild();
                while (node != null) {
                    if (Node.ELEMENT_NODE == node.getNodeType()) {
                        result++;
                        strElement = (Element)node;
                    }
                    node = node.getNextSibling();
                }
                if (result != 1) {
                    throw new WSSecurityException(
                            WSSecurityException.INVALID_SECURITY, "invalidDataRef"
                    );
                }
            } else {
                strElement =
                        WSSecurityUtil.getDirectChildElement(
                                keyInfo,
                                SecurityTokenReference.SECURITY_TOKEN_REFERENCE,
                                WSConstants.WSSE_NS
                        );
            }
            if (strElement == null) {
                throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY, "noSecTokRef"
                );
            }
            STRParser strParser = new EncryptedKeySTRParser();
            strParser.parseSecurityTokenReference(strElement, data, wsDocInfo, null);

            X509Certificate[] certs = strParser.getCertificates();
            if (certs == null || certs.length < 1 || certs[0] == null) {
                throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "noCertsFound",
                        new Object[] {"decryption (KeyId)"}
                );
            }
            return certs;
        } else if (!data.getWssConfig().isWsiBSPCompliant()
                && crypto.getDefaultX509Identifier() != null) {
            String alias = crypto.getDefaultX509Identifier();
            CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
            cryptoType.setAlias(alias);
            X509Certificate[] certs = crypto.getX509Certificates(cryptoType);
            if (certs == null || certs.length < 1 || certs[0] == null) {
                throw new WSSecurityException(
                        WSSecurityException.FAILURE,
                        "noCertsFound",
                        new Object[] {"decryption (KeyId)"}
                );
            }
            return certs;
        } else {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noKeyinfo");
        }
    }

    /**
     * Find the list of all URIs that this encrypted Key references
     */
    private List<String> getDataRefURIs(Element xencEncryptedKey) {
        // Lookup the references that are encrypted with this key
        Element refList =
                WSSecurityUtil.getDirectChildElement(
                        xencEncryptedKey, "ReferenceList", WSConstants.ENC_NS
                );
        List<String> dataRefURIs = new LinkedList<String>();
        if (refList != null) {
            for (Node node = refList.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (Node.ELEMENT_NODE == node.getNodeType()
                        && WSConstants.ENC_NS.equals(node.getNamespaceURI())
                        && "DataReference".equals(node.getLocalName())) {
                    String dataRefURI = ((Element) node).getAttribute("URI");
                    if (dataRefURI.charAt(0) == '#') {
                        dataRefURI = dataRefURI.substring(1);
                    }
                    dataRefURIs.add(dataRefURI);
                }
            }
        }
        return dataRefURIs;
    }

    /**
     * Decrypt all data references
     */
    private List<WSDataRef> decryptDataRefs(
            List<String> dataRefURIs, Document doc, WSDocInfo docInfo, /*byte[] decryptedBytes*/ SecretKey key
    ) throws WSSecurityException {
        //
        // At this point we have the decrypted session (symmetric) key. According
        // to W3C XML-Enc this key is used to decrypt _any_ references contained in
        // the reference list
        if (dataRefURIs == null || dataRefURIs.isEmpty()) {
            return null;
        }
        List<WSDataRef> dataRefs = new ArrayList<WSDataRef>();
        for (String dataRefURI : dataRefURIs) {
            WSDataRef dataRef = decryptDataRef(doc, dataRefURI, docInfo, /*decryptedBytes*/key);
            dataRefs.add(dataRef);
        }
        return dataRefs;
    }

    /**
     * Decrypt an EncryptedData element referenced by dataRefURI
     */
    private WSDataRef decryptDataRef(
            Document doc,
            String dataRefURI,
            WSDocInfo docInfo,
            /*byte[] decryptedData*/SecretKey key
    ) throws WSSecurityException {

        //
        // Find the encrypted data element referenced by dataRefURI
        //
        Element encryptedDataElement =
                ReferenceListProcessor.findEncryptedDataElement(doc, docInfo, dataRefURI);
        //
        // Prepare the SecretKey object to decrypt EncryptedData
        //
        String symEncAlgo = X509Util.getEncAlgo(encryptedDataElement);

        //SecretKey symmetricKey =
        //        WSSecurityUtil.prepareSecretKey(symEncAlgo, decryptedData);

        // Передаем секретный ключ сразу
        return ReferenceListProcessor.decryptEncryptedData(
                doc, dataRefURI, encryptedDataElement, /*symmetricKey*/key, symEncAlgo
        );
    }

    /**
     * Method getDecodedBase64EncodedData
     *
     * @param element
     * @return a byte array containing the decoded data
     * @throws WSSecurityException
     */
    private static byte[] getDecodedBase64EncodedData(Element element) throws WSSecurityException {
        StringBuilder sb = new StringBuilder();
        Node node = element.getFirstChild();
        while (node != null) {
            if (Node.TEXT_NODE == node.getNodeType()) {
                sb.append(((Text) node).getData());
            }
            node = node.getNextSibling();
        }
        String encodedData = sb.toString();
        return Base64.decode(encodedData);
    }
}
