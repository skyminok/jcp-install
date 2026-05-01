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
package JCPxml.dsig.internal.xmldsigri.tests;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.xml.tools.XmlFeatureHelper;

import javax.xml.XMLConstants;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

/**
 * Пример проверки подписанного XML документа по JSR 105 API. Ключ (сертификат)
 * для проверки подписи должен находиться в KeyValue (X509Data) узла KeyInfo.
 * Исходный XML документ:
 *
 * <?xml version="1.0"?>
 *  <PatientRecord>
 *      <Name>John Doe</Name>
 *      <Account id="acct">123456</Account>
 *      <Visit date="10pm March 10, 2002">
 *      <Diagnosis>Broken second metacarpal</Diagnosis>
 *      </Visit>
 *  </PatientRecord>
 */
public class ValidateXMLSig {

    public enum SignatureMethodType { SIGN_WITH_KEY, SIGN_WITH_CERT };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("Usage: java ValidateXMLSig <fileName> <provider name> <method>");
            throw new Exception("Invalid usage");
        } // if

        String provider = args[1];
        String method   = args[2];

        JCPInit.initProviders(provider.equalsIgnoreCase(
            DefaultProvider.JCSP_PROVIDER_NAME));

        org.apache.xml.security.Init.init();
        JCPXMLDSigInit.init();

        SignatureMethodType signatureMethodType = SignatureMethodType.SIGN_WITH_KEY;

        if (method.equalsIgnoreCase("CERT")) {
            signatureMethodType = SignatureMethodType.SIGN_WITH_CERT;
        } // if


        byte[] signedXmlData = Array.readFile(args[0]);
        main0(signedXmlData, signatureMethodType, provider);

    }

    /**
     * Проверка подписи XML файла.
     *
     * @param signedXmlData Подписанный XML.
     * @param signatureMethodType Информация о подписанте.
     * @param provider Провайдер подписи.
     * @throws Exception
     */
    public static void main0(byte[] signedXmlData, SignatureMethodType
        signatureMethodType, String provider) throws Exception {

        // BasicConfigurator.configure();  для log4j

        JCPXMLDSigInit.init();

        // Загружаем документ для проверки.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        dbFactory.setNamespaceAware(true);
        Document doc = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(signedXmlData));

        // Ищем элемент Signature.
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        } // if

        // Создаем DOM XMLSignatureFactory для разбора документа с XMLSignature.
        String providerName = "ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI";

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
            (Provider) Class.forName(providerName).newInstance());

        // Проверяем все подписи.
        for (int i = 0; i < nl.getLength(); i++) {

            // Создаем DOMValidateContext и задаем KeySelector для поиска
            // KeyValue или X509Data в контексте документа.
            DOMValidateContext valContext = new DOMValidateContext(
                signatureMethodType == SignatureMethodType.SIGN_WITH_KEY
                ? new KeyValueKeySelector() : new X509CertificateSelector(),
                nl.item(i));

            valContext.setProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider", provider);

            // Разбор XMLSignature.
            XMLSignature signature = fac.unmarshalXMLSignature(valContext);

            // Проверка подписи XMLSignature.
            boolean coreValidity = signature.validate(valContext);

            // Вывод статуса проверки.
            if (!coreValidity) {

                System.out.println(String.format("Signature %s failed core validation", i));

                boolean sv = signature.getSignatureValue().validate(valContext);
                System.out.println(String.format("Signature %s validation status: %s", i, sv));

                // Првоерка статуса каждого Reference.
                Iterator it = signature.getSignedInfo().getReferences().iterator();

                for (int j = 0; it.hasNext(); j++) {

                    boolean refValid = ((Reference) it.next()).validate(valContext);
                    System.out.println(String.format("Signature %s ref['%s'] validity status: %s",
                         i, j, refValid));
                } // for
            } // if
            else {
                System.out.println(String.format("Signature %s passed core validation", i));
            } // else

            if (!coreValidity) {
                throw new Exception("Invalid signature detected");
            } // if

        } // for
    }

    /**
     * KeySelector извлекает открытый ключ из элемента KeyValue.
     * NOTE: если алгоритм ключа не соответствует алгоритму подписи,
     * то открытый ключ не получим.
     */
    private static class KeyValueKeySelector extends KeySelector {

        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose,
            AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {

            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            } // if

            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();

            for (int i = 0; i < list.size(); i++) {

                XMLStructure xmlStructure = (XMLStructure) list.get(i);

                if (xmlStructure instanceof KeyValue) {

                    PublicKey pk = null;

                    try {
                        pk = ((KeyValue)xmlStructure).getPublicKey();
                    }
                    catch (KeyException ke) {
                        throw new KeySelectorException(ke);
                    }

                    // Проверка алгоритмов.
                    if (X509CertificateSelector.algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKeySelectorResult(pk);
                    } // if
                } // if
            }
            throw new KeySelectorException("No KeyValue element found!");
        }

    }

    /**
     * Класс для хранения открытого ключа для проверки подписи.
     */
    private static class SimpleKeySelectorResult implements KeySelectorResult {

        private PublicKey pk;

        SimpleKeySelectorResult(PublicKey pk) {
            this.pk = pk;
        }

        public Key getKey() {
            return pk;
        }
    }

    /**
     * X509CertificateSelector возвращает открытый ключ из элемента
     * X509Certificate(X509Data).
     * NOTE: если алгоритм ключа не соответствует алгоритму подписи,
     * то открытый ключ не получим.
     */
    public static class X509CertificateSelector extends KeySelector {

        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose,
            AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {

            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            } // if

            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();

            for (int i = 0; i < list.size(); i++) {

                XMLStructure xmlStructure = (XMLStructure) list.get(i);

                if (xmlStructure instanceof X509Data) {

                    X509Data data = (X509Data)xmlStructure;
                    X509Certificate cert = (X509Certificate) data.getContent().get(0);
                    PublicKey pk = cert.getPublicKey();

                    // Проверка алгоритмов.
                    if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        // System.out.println("Verify by certificate #" + cert.getSerialNumber().toString(16)
                        //     + " " + cert.getSubjectDN());
                        return new SimpleKeySelectorResult(pk);
                    } // if
                } // if
            } // for

            throw new KeySelectorException("No KeyValue element found!");

        }

        /**
         * Функция проверки алгоритма ключа.
         *
         * @param algURI Алгоритм подписи.
         * @param algName Алгоритм ключа.
         * @return True, если алгоритмы сопостовимы.
         */
        static boolean algEquals(String algURI, String algName) {

            if (algName.equalsIgnoreCase("DSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
                return true;
            } // if
            else if (algName.equalsIgnoreCase("RSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
                return true;
            } // else
            else if
            (
                ( algName.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME)
                ) &&
                ( algURI.equalsIgnoreCase(ru.CryptoPro.JCPxml.Consts.URI_GOST_SIGN) ||
                  algURI.equalsIgnoreCase(ru.CryptoPro.JCPxml.Consts.URN_GOST_SIGN)
                )
            ) {
                return true;
            } // else
            else if (algName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) &&
                algURI.equalsIgnoreCase(ru.CryptoPro.JCPxml.Consts.URN_GOST_SIGN_2012_256)) {
                return true;
            } // else
            else if (algName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) &&
                algURI.equalsIgnoreCase(ru.CryptoPro.JCPxml.Consts.URN_GOST_SIGN_2012_512)) {
                return true;
            } // else
            else {
                return false;
            } // else
        }
    }

}
