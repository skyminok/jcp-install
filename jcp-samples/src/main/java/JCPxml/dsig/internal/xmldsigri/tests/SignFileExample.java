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

import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;

import ru.xml.tools.TransformerFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;
import util.ResolveProvider;
import wss4j.config.XmlContainer;
import xades.config.IXAdESConfig;

import javax.xml.XMLConstants;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.CryptoPro.JCPxml.Consts.CONFIG;
import static ru.CryptoPro.JCPxml.Consts.PROPERTY_NAME;

/**
 * Пример подписи XML документа по JSR 105 API.
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
 *
 *  Результат (KeyValue):
 *
 *  <pre><code>
 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
 *   <SignedInfo>
 *     <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
 *     <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411" />
 *     <Reference URI="http://www.w3.org/TR/xml-stylesheet">
 *       <DigestMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr3411" />
 *       <DigestValue>0Eys/LMpFXPyKVArIbEtLbKVhG4M4+t10J12it8vFl8=</DigestValue>
 *     </Reference>
 *   </SignedInfo>
 *   <SignatureValue>
 *     AnJqyBwAqq7cVO5kVB2/V39OtcQEBPDOrS0814bFApbe5kWurC1wscuRoPqoW3LynSzJrRq4Idon
 *     lQkYvDclLw==
 *   </SignatureValue>
 *   <KeyInfo>
 *     <KeyValue>
 *       <GOSTKeyValue>
 *         MGMwHAYGKoUDAgITMBIGByqFAwICIwEGByqFAwICHgEDQwAEQOHLeJqnkLtl9JYeyfbStAwFVci5
 *         TUMy40Uucx1+ce+7UFr2p53t9onUeCLp/zixauWlIaU5TBHXPNcKYNbdoxI=
 *       </GOSTKeyValue>
 *     </KeyValue>
 *   </KeyInfo>
 * </Signature>
 * </code></pre>
 *
 * Результат (X509Data):
 *
 * <pre><code>
 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
 *   <SignedInfo>
 *     <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
 *     <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411" />
 *     <Reference URI="http://www.w3.org/TR/xml-stylesheet">
 *       <DigestMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr3411" />
 *       <DigestValue>0Eys/LMpFXPyKVArIbEtLbKVhG4M4+t10J12it8vFl8=</DigestValue>
 *     </Reference>
 *   </SignedInfo>
 *   <SignatureValue>
 *     azKP+ZHtjuH3uTrjqNfYUy9h4ebrzSSE0q6XI7mhB3j1NgNFVfJMpzmIqByDymd/j9qAzfmn5vj4
 *     0yyBrXtLqw==
 *   </SignatureValue>
 *   <KeyInfo>
 *     <X509Data>
 *       <X509Certificate>
 *	        MIIC4zCCApKgAwIBAgIKFVWsWwACAAGNCjAIBgYqhQMCAgMwZTEgMB4GCSqGSIb3DQEJARYRaW5m
 *			b0BjcnlwdG9wcm8ucnUxCzAJBgNVBAYTAlJVMRMwEQYDVQQKEwpDUllQVE8tUFJPMR8wHQYDVQQD
 *			ExZUZXN0IENlbnRlciBDUllQVE8tUFJPMB4XDTExMTAyODE2NDM1NFoXDTE0MTAwNDA3MDk0MVow
 *			NzELMAkGA1UEBhMCUlUxEjAQBgNVBAoTCUNyeXB0b1BybzEUMBIGA1UEAwwLYWZldm1hX2dvc3Qw
 *			YzAcBgYqhQMCAhMwEgYHKoUDAgIjAQYHKoUDAgIeAQNDAARArdmGxyoSVperAUc6d0TiGnC9ilgi
 *			C+EPxul3htCFdQ/zQ7z7vPX9/3Xt/Lfb+lzVhlRU4W00B5CTKrseoziFaaOCAU4wggFKMA4GA1Ud
 *			DwEB/wQEAwIGwDAdBgNVHQ4EFgQULv5fF45Y2vgOHFnY60i0UFWrW8UwHwYDVR0jBBgwFoAUbY9e
 *			BdlfrJEXlB6VmgUwODd6ECowVQYDVR0fBE4wTDBKoEigRoZEaHR0cDovL3d3dy5jcnlwdG9wcm8u
 *			cnUvQ2VydEVucm9sbC9UZXN0JTIwQ2VudGVyJTIwQ1JZUFRPLVBSTygyKS5jcmwwgaAGCCsGAQUF
 *			BwEBBIGTMIGQMDMGCCsGAQUFBzABhidodHRwOi8vd3d3LmNyeXB0b3Byby5ydS9vY3NwbmMvb2Nz
 *			cC5zcmYwWQYIKwYBBQUHMAKGTWh0dHA6Ly93d3cuY3J5cHRvcHJvLnJ1L0NlcnRFbnJvbGwvcGtp
 *			LXNpdGVfVGVzdCUyMENlbnRlciUyMENSWVBUTy1QUk8oMikuY3J0MAgGBiqFAwICAwNBAAtCRkEv
 *			I9yqSy4xWNO3+ektITt0kPNCbc9b76j5qy44hO694/OJBBsP1cjr7eRfFemzhVkAuKm1NiCWWmUt
 *			sgk=
 *       </X509Certificate>
 *     </X509Data>
 *   </KeyInfo>
 * </Signature>
 * </code></pre>
 */
public class SignFileExample {

    /**
     * Тип контейнера JCSP по умолчанию.
     */
    private static final String JCSP_DEFAULT_STORE_TYPE = /*Platform.isWindows() ? "REGISTRY" : */ ResolveProvider.ALTERNATIVE_HD_IMAGE;

    private static final String PATH = "/PatientRecord/Account";
    private static final String ID = "acct";
    private static final String ID_NAME = "id";

    public static enum SignatureType { SIGN_BY_ID, SIGN_BY_PATH, SIGN_WHOLE_DOCUMENT };
    public static enum SignatureMethodType { SIGN_WITH_KEY, SIGN_WITH_CERT };

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            usage();
            throw new Exception("Invalid usage");
        } // if

        String inputFile  = args[0];
        String outputFile = args[1];
        String provider   = args[2];
        String method     = args[3];

        //JCPInit.initProviders(provider.equalsIgnoreCase(
          //  DefaultProvider.JCSP_PROVIDER_NAME));

        SignatureMethodType signatureMethodType = SignatureMethodType.SIGN_WITH_KEY;

        if (method.equalsIgnoreCase("CERT")) {
            signatureMethodType = SignatureMethodType.SIGN_WITH_CERT;
        } // if

        SignatureType sigType = SignatureType.SIGN_WHOLE_DOCUMENT;

        if (args.length >= 5) {

            if ("id".equalsIgnoreCase(args[4])) {
                sigType = SignatureType.SIGN_BY_ID;
            } // if
            else if ("path".equalsIgnoreCase(args[4])) {
                sigType = SignatureType.SIGN_BY_PATH;
            } // else

        } // if

        XmlContainer.KeyType keyType = XmlContainer.KeyType.kt2001;

        if (args.length >= 6) {

            if ("short".equalsIgnoreCase(args[5])) {
                keyType = XmlContainer.KeyType.kt2012_256;
            } // if
            else if ("long".equalsIgnoreCase(args[5])) {
                keyType = XmlContainer.KeyType.kt2012_512;
            } // else

        }

        byte[] xmlFileData = Array.readFile(inputFile);
        main0(xmlFileData, sigType, provider, keyType, signatureMethodType, outputFile);

    }

    /**
     * Подпись данных.
     *
     * @param xmlFileData Содержимое XML файла.
     * @param sigType Способ подписи.
     * @param provider Имя провайдера подписи.
     * @param keyType Алгоритм ключа.
     * @param signatureMethodType Информация о подписанте.
     * @param outputFile Имя фйла с подписью.
     * @throws Exception
     */
    public static void main0(byte[] xmlFileData, SignatureType sigType,
        String provider, XmlContainer.KeyType keyType, SignatureMethodType
        signatureMethodType, String outputFile) throws Exception {

        // BasicConfigurator.configure(); для log4j
        org.apache.xml.security.Init.init();
        JCPXMLDSigInit.init();

        // Декодируем документ.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        dbFactory.setNamespaceAware(true);
        Document doc = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlFileData));

        // Провайдер XMLDSigRI.
        String providerName = "ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI";

        final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance(
            "DOM", (Provider) Class.forName(providerName).newInstance());

        Node nodeToSign = null;
        Node sigParent   = null;
        String referenceURI = null;
        XPathExpression expr = null;
        NodeList nodes;
        List transforms = null;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        switch (sigType) {

            case SIGN_BY_ID:
                // u:Id
                expr = xpath.compile(String.format("//*[@" + ID_NAME + "='%s']", ID));
                nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                if (nodes.getLength() == 0) {
                    throw new Exception("Can't find node with " + ID_NAME + ": " + ID);
                } // if

                nodeToSign = nodes.item(0);
                sigParent = nodeToSign.getParentNode();
                referenceURI = "#" + ID;
                //
                //     * This is not needed since the signature is alongside the signed element, not enclosed in it.
                //        transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                //        (TransformParameterSpec) null));
                //
                break;

            case SIGN_BY_PATH:

                // Ищем узел для подписи по PATH
                expr = xpath.compile(PATH);
                nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                if (nodes.getLength() < 1) {
                    throw new Exception("Invalid document, can't find node by PATH: " + PATH);
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
                /*
                transforms = Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                    (TransformParameterSpec) null));
                */
                transforms = new ArrayList<Transform>() {{
                    add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
                    add(sigFactory.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null));
                }};
                break;
        }

        KeyStore keyStore = KeyStore.getInstance(
            provider.equalsIgnoreCase(JCP.PROVIDER_NAME) ? JCP.HD_STORE_NAME : JCSP_DEFAULT_STORE_TYPE);
        keyStore.load(null, null);

        IXAdESConfig container = XmlContainer.createContainer(keyType);

        // Получаем ключ подписи.
        PrivateKey privateKey;

        if (provider.equalsIgnoreCase(ResolveProvider.ALTERNATIVE_PROVIDER)) {

            JCPProtectionParameter parameter = new JCPProtectionParameter(
                container.getSignatureContainer().getPassword());

            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(
                container.getSignatureContainer().getAlias(), parameter);

            privateKey = entry.getPrivateKey();

        } // if
        else {
            privateKey = (PrivateKey) keyStore.getKey(
                container.getSignatureContainer().getAlias(),
                    container.getSignatureContainer().getPassword());
        } // else

        // Сертификат для помещения в X509Data (KeyInfo).
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(
            container.getSignatureContainer().getAlias());

        /*
        List<Transform> transformList = new ArrayList<Transform>();
        Transform transformC14N =
            sigFactory.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
        transformList.add(transformC14N);
        */

        // Ссылка на подписываемые данные с указанием алгоритма хеширования.
        Reference ref = sigFactory.newReference(referenceURI,
            sigFactory.newDigestMethod(container.getDigestMethod(), null),
            transforms, null, null);

        // Создаем объект SignedInfo с указанием алгоритма подписи.
        SignedInfo signedInfo = sigFactory.newSignedInfo(
                sigFactory.newCanonicalizationMethod(
                CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                sigFactory.newSignatureMethod(container.getSignatureMethod(), null),
                Collections.singletonList(ref)
        );

        KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
        KeyInfo keyInfo = null;

        if (signatureMethodType == SignatureMethodType.SIGN_WITH_KEY) {

            // Создаем KeyValue, содержащий ГОСТ PublicKey.
            KeyValue keyValue = keyInfoFactory.newKeyValue(cert.getPublicKey());

            // Создаем KeyInfo и добавляем KeyValue в него.
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));
        } // if
        else {
            // Создаем X509Data, содержащий сертификат.
            X509Data x509d = keyInfoFactory.newX509Data(Collections.singletonList(cert));

            // Создаем KeyInfo и добавляем X509Data в него.
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509d));
        } // else

        // Создаем DOMSignContext и задаем закрытый ключ, а также родительский
        // элемент подписи XMLSignature.
        DOMSignContext dsc = new DOMSignContext(privateKey, sigParent);

        //if (sigType == SignatureType.SIGN_BY_ID && nodeToSign != null) {
        //    dsc.setIdAttributeNS((Element) nodeToSign, null, ID_NAME);
        //} // if

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

    }

    private static void usage() {
        System.out.println("Usage: java SignFileExample <inputFile> <outputFile> <provider name> <method> [id|path|whole] <key_type> [old|short|long]");
    }

}
