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
import org.apache.ws.security.processor.ReferenceListProcessor;
import org.apache.ws.security.processor.X509Util;
import org.apache.ws.security.str.STRParser;
import org.apache.ws.security.str.SecurityTokenRefSTRParser;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.Key.SecretKeyInterface;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.params.AlgIdSpec;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.params.ParamsInterface;

import javax.crypto.SecretKey;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Своя реализация ReferenceListProcessor (обработка референсов).
 */
public class MyReferenceListProcessor extends ReferenceListProcessor {

    public List<WSSecurityEngineResult> handleToken(
            Element elem,
            RequestData data,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {

        List<WSDataRef> dataRefs = handleReferenceList(elem, data, wsDocInfo);
        WSSecurityEngineResult result =
                new WSSecurityEngineResult(WSConstants.ENCR, dataRefs);
        wsDocInfo.addTokenElement(elem);
        wsDocInfo.addResult(result);
        return java.util.Collections.singletonList(result);
    }

    /**
     * Dereferences and decodes encrypted data elements.
     *
     * @param elem contains the <code>ReferenceList</code> to the encrypted
     *             data elements
     */
    private List<WSDataRef> handleReferenceList(
            Element elem,
            RequestData data,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {

        List<WSDataRef> dataRefs = new ArrayList<WSDataRef>();

        for (Node node = elem.getFirstChild();
             node != null;
             node = node.getNextSibling()
                ) {
            if (Node.ELEMENT_NODE == node.getNodeType()
                    && WSConstants.ENC_NS.equals(node.getNamespaceURI())
                    && "DataReference".equals(node.getLocalName())) {
                String dataRefURI = ((Element) node).getAttribute("URI");
                if (dataRefURI.charAt(0) == '#') {
                    dataRefURI = dataRefURI.substring(1);
                }
                WSDataRef dataRef =
                        decryptDataRefEmbedded(
                                elem.getOwnerDocument(), dataRefURI, data, wsDocInfo);
                dataRefs.add(dataRef);
            }
        }

        return dataRefs;
    }

    /**
     * Decrypt an (embedded) EncryptedData element referenced by dataRefURI.
     */
    private WSDataRef decryptDataRefEmbedded(
            Document doc,
            String dataRefURI,
            RequestData data,
            WSDocInfo wsDocInfo
    ) throws WSSecurityException {

        //
        // Find the encrypted data element referenced by dataRefURI
        //
        Element encryptedDataElement = findEncryptedDataElement(doc, wsDocInfo, dataRefURI);
        //
        // Prepare the SecretKey object to decrypt EncryptedData
        //
        String symEncAlgo = X509Util.getEncAlgo(encryptedDataElement);
        Element keyInfoElement =
                (Element) WSSecurityUtil.getDirectChildElement(
                        encryptedDataElement, "KeyInfo", WSConstants.SIG_NS
                );
        // KeyInfo cannot be null
        if (keyInfoElement == null) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY, "noKeyinfo");
        }

        // Ничего не проверяем
        // Check BSP compliance
        //if (data.getWssConfig().isWsiBSPCompliant()) {
        //    checkBSPCompliance(keyInfoElement, symEncAlgo);
        //}

        //
        // Try to get a security reference token, if none found try to get a
        // shared key using a KeyName.
        //
        Element secRefToken =
                WSSecurityUtil.getDirectChildElement(
                        keyInfoElement, "SecurityTokenReference", WSConstants.WSSE_NS
                )
                ;
        SecretKey symmetricKey = null;
        if (secRefToken == null) {
            // Сюда не попадаем
            // ??? symmetricKey = X509Util.getSharedKey(keyInfoElement, symEncAlgo, data.getCallbackHandler());
        } else {
            // Попадаем сюда. Здесь используется свой
            // MySecurityTokenRefSTRParser!
            STRParser strParser = new MySecurityTokenRefSTRParser();
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(SecurityTokenRefSTRParser.SIGNATURE_METHOD, symEncAlgo);
            strParser.parseSecurityTokenReference(
                    secRefToken, data,
                    wsDocInfo, parameters
            );

            // Сюда попадает 32 байта производного секретного ключа,
            // который можно создать, используя параметры шифрования
            byte[] secretKey = strParser.getSecretKey();

            // Параметры шифрования по умолчанию. Захардкожены тут!
            final AlgIdSpec specParams = new AlgIdSpec(null, null, null,
                CryptParamsSpec.OID_Crypt_VerbaO);
            final ParamsInterface cryptParams = specParams.getCryptParams();

            SecretKeyInterface derivedSecretKeySpec = null;

            try {
                // Создаем секретный ключ
                derivedSecretKeySpec = new SecretKeySpec(secretKey,
                    (CryptParamsInterface)cryptParams);
            } catch (KeyManagementException e) {
                throw new WSSecurityException(e.getMessage(), e);
            }

            // Производный секретный ключ для расшифрования референсов
            symmetricKey = new GostSecretKey(derivedSecretKeySpec); // WSSecurityUtil.prepareSecretKey(symEncAlgo, secretKey);
        }

        return
                decryptEncryptedData(
                        doc, dataRefURI, encryptedDataElement, symmetricKey, symEncAlgo
                );
    }
}
