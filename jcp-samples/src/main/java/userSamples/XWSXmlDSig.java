/**
 * $RCSfile$
 * version $Revision$
 * created 28.02.2005 21:10:08 by avsh
 * last modified $Date$ by $Author$
 *  (C) ООО Крипто-Про 2004-2005.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Пример XML подписи.
 */

public class XWSXmlDSig {
private String signMethod;      //метод подписи
private String digestMethod;    //метод получения хеша
private String transform;       //метод преобразования для получения хеша
private X509Certificate cert;   //сертификат открытого ключа, соответствующий закрытому (для включения в состав
// документа и обеспечения возможности проверки)
private PrivateKey privateKey;  //закрытый ключ на котором будеит осуществляться подпись

//методы для задания и получения соответствующих атрибутов

public String getSignMethod() {
    return signMethod;
}

public String getDigestMethod() {
    return signMethod;
}

public void setSignMethod(String SM) {
    signMethod = SM;
}

public void setdigestMethod(String DM) {
    digestMethod = DM;
}

public void SetCertificate(X509Certificate Cer) {
    cert = Cer;
    //смотрим содержимое сертификата и в соответствии с ним выставляем алгоритмы хеширования и подписи
    // обозначен алгоритм подписи RSA он не обязательно должен использовать хеш SHA1
    if (algEquals(cert.getPublicKey().getAlgorithm(),
            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1)) {
        signMethod = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
        digestMethod = Constants.ALGO_ID_DIGEST_SHA1;
    }
    if (algEquals(cert.getPublicKey().getAlgorithm(),
            XMLSignature.ALGO_ID_SIGNATURE_DSA)) {
        signMethod = XMLSignature.ALGO_ID_SIGNATURE_DSA;
        digestMethod = Constants.ALGO_ID_DIGEST_SHA1;
    }
}

public X509Certificate getCertificate() {
    return cert;
}

public PrivateKey getPrivateKey() {
    return privateKey;
}

public void setPrivateKey(PrivateKey pKey) {
    privateKey = pKey;
}

public XWSXmlDSig() {// по-умолчанию считается что алгоритм подписи RSA алгоритм хеширования SHA1 а метод
    // преобразования для получения хеша ENVELOPED
    signMethod = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
    digestMethod = Constants.ALGO_ID_DIGEST_SHA1;
    transform = Transforms.TRANSFORM_C14N_WITH_COMMENTS;
}

static {
    ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit.init();
}

// метод подисывающий объект

public Document SignObject() throws Exception {

    DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
    dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    dbFactory.setNamespaceAware(true);

    Document doc = dbFactory.newDocumentBuilder().newDocument();

    XMLSignature sig = new XMLSignature(doc, "", signMethod);
    doc.appendChild(sig.getElement()); // подцепляем подпись


    ObjectContainer obj = new ObjectContainer(doc);
    Element anElement = doc.createElement(
            "InsideObject");   // создаем внутри подписи подписываемый объект

    anElement.appendChild(doc.createTextNode("A text in a box"));
    obj.appendChild(anElement);

    String Id = "TheFirstObject";

    obj.setId(Id);
    sig.appendObject(obj);


    Transforms transforms = new Transforms(doc);
    transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
    transforms.addTransform(transform);

    sig.addDocument("", transforms,
            digestMethod);  // добавление ref (пустая => на текущий д-т)


    sig.addKeyInfo(cert);

    sig.sign(privateKey);
    return doc;
}

// метод проверки подписи документа

public static boolean Validate(Document doc)
        throws TransformerException, XMLSecurityException {

    boolean valid;
    Element nscontext = doc.createElementNS(null, "namespaceContext");

    nscontext.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:" + "ds".trim(), Constants.SignatureSpecNS);

    Element sigElement = (Element) XPathAPI.selectSingleNode(doc,
            "//ds:Signature[1]", nscontext);

    XMLSignature signature = new XMLSignature(sigElement, "");
    KeyInfo ki = signature.getKeyInfo();

    if (ki != null) {

        if (ki.containsX509Data()) {
            System.out.println("Could find a X509Data element in the KeyInfo");
        }

        X509Certificate cert = signature.getKeyInfo().getX509Certificate();
        if (cert != null) {

            valid = signature.checkSignatureValue(cert);

            System.out.println("The XML signature  is "
                    + (valid
                    ? "valid (good)"
                    : "invalid !!!!! (bad)"));

        } else {

            System.out.println("Did not find a Certificate");
            PublicKey pk = signature.getKeyInfo().getPublicKey();

            if (pk != null) {

                System.out.println(
                        "I try to verify the signature using the public key: "
                                + pk);

                valid = signature.checkSignatureValue(pk);

                System.out.println("The XML signature is "
                        + (valid
                        ? "valid (good)"
                        : "invalid !!!!! (bad)"));

            } else {
                System.out.println(
                        "Did not find a public key, so I can't check the signature");
                return false;
            }
        }

    } else {
        System.out.println("Did not find a KeyInfo");
        return false;
    }

    return valid;
}


static boolean algEquals(String algURI, String algName) {
    if (algName.equalsIgnoreCase("DSA") &&
            algURI.equalsIgnoreCase(XMLSignature.ALGO_ID_SIGNATURE_DSA)) {
        return true;
    } else if (algName.equalsIgnoreCase("RSA") &&
            algURI.equalsIgnoreCase(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1)) {
        return true;
    } else if (algName.equalsIgnoreCase("1.2.643.2.2.20") &&
            algURI.equalsIgnoreCase(
                    "http://www.w3.org/2001/04/xmldsig-more#gostr34101994-gostr3411")) {
        return true;
    } else {
        return false;
    }
}
}

