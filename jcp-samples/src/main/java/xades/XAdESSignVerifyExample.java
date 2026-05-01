package xades;

import org.w3c.dom.Document;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.XAdES.DataObjectFormat;
import ru.CryptoPro.XAdES.DataObjects;
import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import util.ResolveProvider;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.XMLUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;

public class XAdESSignVerifyExample {
    private static final String CRL_FOLDER = System.getProperty("user.dir") + File.separator + "data" + File.separator +
        "CRLS" + File.separator;
    private static final IXAdESConfig XADES_CONFIG = XAdESConfig.Default.CONFIG_2012_S;
    private static final Integer[] XADES_TYPE = new Integer[] {XAdESType.XAdES_T};
    private static final String TSA_URL = "http://testca2012.cryptopro.ru/tspservice";
    private static final String WORK_DIR = System.getProperty("user.dir") + File.separator + "temp" + File.separator;
    private static final boolean ADD_CERTIFIACTE_CHAIN = false;
    private static final ITransform[] TRANSFORMS = new ITransform[] {new EnvelopedTransform()};


    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
        JCPInit.initProviders(false);

        //Подготовим сертификаты
        final KeyStore keyStore = KeyStore.getInstance(XADES_CONFIG.getKeyStoreType(), XADES_CONFIG.getDefaultProvider());
        keyStore.load(null, null);
        final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(XADES_CONFIG.getSignatureContainer().getAlias());
        X509Certificate[] x509Certificates0 = new X509Certificate[1];
        x509Certificates0[0] = certificate;
        Set<X509Certificate> certificates = new HashSet<>(Arrays.asList(x509Certificates0));

        //Подготовим списки отозванных сертификатов
        Collection storeCertifiacteCRL = new ArrayList();
        XMLUtility.loadIntermediateCertificateAndCrlList(
                CRL_FOLDER + "root.crl", true, storeCertifiacteCRL);
        Set<X509CRL> cRLs = new HashSet<X509CRL>(storeCertifiacteCRL);

        //Подготовим документ
        final Document document = XAdESSignVerify.parseFile(XAdESExample.XML_DOC.getBytes("UTF-8"));

        //Подписываемый документ №0
        String referenceUri0 = "#acct";

        //Подписываемый документ №1
        String referenceUri1 = "#bank";

        //Загрузка контейнера
        Certificate[] chain = keyStore.getCertificateChain(
                XADES_CONFIG.getSignatureContainer().getAlias());

        PrivateKey privateKey;
        privateKey = (PrivateKey) keyStore.getKey(
                XADES_CONFIG.getSignatureContainer().getAlias(),
                XADES_CONFIG.getSignatureContainer().getPassword());

        //Подпись
        DataObjectFormat dataObjectFormat = new DataObjectFormat("text/plain",null, null, "UTF-8");
        final DataObjects dataObjects = new DataObjects();
        dataObjects.addUri(referenceUri0, dataObjectFormat);
        dataObjects.addUri(referenceUri1, dataObjectFormat);

        if (TRANSFORMS != null) {
            for (ITransform transform : TRANSFORMS) {
                dataObjects.addTransform(transform);
            }
        }

        X509Certificate[] x509Certificates = new X509Certificate[chain.length + certificates.size()];
        System.arraycopy(chain, 0, x509Certificates, 0, chain.length);

        if (!certificates.isEmpty()) {

            System.arraycopy(certificates.toArray(new X509Certificate[certificates.size()]),
                    0, x509Certificates, chain.length, certificates.size());

        } // if

        // Создание подписи.
        final XAdESSignature xAdESSignature = new XAdESSignature();

        // Добавление подписей.
        for (Integer type : XADES_TYPE) {

            xAdESSignature.addSigner(XADES_CONFIG.getDefaultProvider(),
                    JCP.GOST_DIGEST_OID, JCP.GOST_EL_KEY_OID,
                    null, privateKey, Arrays.asList(x509Certificates), ADD_CERTIFIACTE_CHAIN,
                    type, TSA_URL, cRLs);

        } // for

        final OutputStream outputStream = (WORK_DIR != null)
                ? new FileOutputStream(WORK_DIR + "/xades.xml")
                : new ByteArrayOutputStream();

        xAdESSignature.open(outputStream);
        xAdESSignature.update(document.getDocumentElement(), dataObjects);

        xAdESSignature.close();
        outputStream.close();
        System.out.println("XAdES signature completed.");

        //проверка
        final XAdESSignature xmlAdvancedSignature = new XAdESSignature(
                document.getDocumentElement(), XADES_TYPE[0]);

        if (!XADES_TYPE[0].equals(XAdESType.XAdES_X_Long_Type_1)) {
            xmlAdvancedSignature.verify(certificates, cRLs);
        } // if
        else {
            xmlAdvancedSignature.verify(null);
        } // else

        System.out.println("XAdES verification completed.");
    }

}
