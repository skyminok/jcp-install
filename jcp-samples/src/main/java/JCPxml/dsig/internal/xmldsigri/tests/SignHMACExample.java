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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCPxml.Consts;
import ru.xml.tools.TransformerFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.XMLConstants;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Пример подписи на симметричном ключе.
 */
public class SignHMACExample {

    private static final String PATH = "/PatientRecord/Account";
    private static final String ID = "acct";

    private static enum SignatureType {
        SIGN_BY_ID,
        SIGN_BY_PATH,
        SIGN_WHOLE_DOCUMENT
    };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            usage();
            return;
        } // if

        String inputFile = args[0];
        String outputFile = args[1];
        String provider = args[2];

        JCPInit.initProviders(provider.equalsIgnoreCase(
            DefaultProvider.JCSP_PROVIDER_NAME));

        SignatureType sigType = SignatureType.SIGN_WHOLE_DOCUMENT;
        if (args.length >= 4) {

            if ("id".equals(args[3])) {
                sigType = SignatureType.SIGN_BY_ID;
            } // if
            else if ("path".equals(args[3])) {
                sigType = SignatureType.SIGN_BY_PATH;
            } // else

        } // if

        // Декодируем документ.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        dbFactory.setNamespaceAware(true);
        Document doc;

        try (FileInputStream is = new FileInputStream(inputFile)) {
            doc = dbFactory.newDocumentBuilder().parse(is);
        }

        // Провайдер XMLDSigRI.
        String providerName = "ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI";

        final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance(
            "DOM", (Provider) Class.forName(providerName).newInstance());

        Node nodeToSign = null;
        Node sigParent = null;
        String referenceURI = null;
        XPathExpression expr = null;
        NodeList nodes;
        List transforms = null;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        switch (sigType) {

            case SIGN_BY_ID:
                expr = xpath.compile(String.format("//*[@id='%s']", ID));
                nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                if (nodes.getLength() == 0) {
                    System.out.println("Can't find node with id: " + ID);
                    return;
                } // if

                nodeToSign = nodes.item(0);
                sigParent = nodeToSign.getParentNode();
                referenceURI = "#" + ID;
                /*
                     * This is not needed since the signature is alongside the signed element, not enclosed in it.
                        transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                        (TransformParameterSpec) null));
                */
                break;

            case SIGN_BY_PATH:

                // Ищем узел для подписи по PATH
                expr = xpath.compile(PATH);
                nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                if (nodes.getLength() < 1) {
                    System.out.println("Invalid document, can't find node by PATH: " + PATH);
                    return;
                } // if

                nodeToSign = nodes.item(0);
                sigParent = nodeToSign.getParentNode();
                referenceURI = ""; // Пустая строка означает весь документ.
                transforms = new ArrayList<Transform>() {{
                    add(sigFactory.newTransform(Transform.XPATH, new XPathFilterParameterSpec(PATH)));
                    add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
                }};

                break;

            default:

                sigParent = doc.getDocumentElement();
                referenceURI = ""; // Пустая строка означает весь документ.
                transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                    (TransformParameterSpec) null));
                break;
        }

        // Ссылка на подписываемые данные с указанием алгоритма хеширования.
        Reference ref = sigFactory.newReference(referenceURI,
            sigFactory.newDigestMethod(Consts.URN_GOST_DIGEST, null),
            transforms, null, null);

        // Создаем объект SignedInfo с указанием алгоритма подписи.
        SignedInfo signedInfo = sigFactory.newSignedInfo(
            sigFactory.newCanonicalizationMethod(
            CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null),
            sigFactory.newSignatureMethod("urn:ietf:params:xml:ns:cpxmlsec:algorithms:hmac-gostr3411", null),
            Collections.singletonList(ref));

        // JCP не имеет реализации GOST28147, вместо него нужен Crypto.
        KeyGenerator kg = KeyGenerator.getInstance("GOST28147",
            provider.equalsIgnoreCase(JCP.PROVIDER_NAME) ? "Crypto" : provider);
        SecretKey secretKey = kg.generateKey();
        KeySelector keySelector = KeySelector.singletonKeySelector(secretKey);

        KeyInfo keyInfo = null;

        // Создаем DOMSignContext и задаем закрытый ключ, а также родительский
        // элемент подписи XMLSignature.
        DOMSignContext dsc = new DOMSignContext(secretKey, sigParent);
        dsc.setProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider", provider);

        // Создаем XMLSignature.
        XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);

        // Подписываем.
        signature.sign(dsc);

        // Вывод результата.
        try (OutputStream os = new FileOutputStream(outputFile)) {
            Transformer trans = TransformerFactoryHelper.newInstance().newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(os));
        }

        // Ищем элемент Signature.
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        } // if

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
            (Provider) Class.forName(providerName).newInstance());

        // Проверяем все подписи.
        for (int i = 0; i < nl.getLength(); i++) {

            // Создаем DOMValidateContext и задаем KeySelector для поиска
            // KeyValue или X509Data в контексте документа.
            DOMValidateContext valContext = new DOMValidateContext(keySelector, nl.item(i));
            valContext.setProperty("org.jcp.xml.dsig.internal.dom.SignatureProvider", provider);

            // Разбор XMLSignature.
            signature = fac.unmarshalXMLSignature(valContext);

            // Проверка подписи XMLSignature.
            boolean coreValidity = signature.validate(valContext);

            // Вывод статуса проверки.
            if (coreValidity == false) {

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
        } // for
    }

    private static void usage() {
        System.out.println("Usage: java SignHMACExample <inputFile> <outputFile> <provider name> [id|path|whole]");
    }
}
