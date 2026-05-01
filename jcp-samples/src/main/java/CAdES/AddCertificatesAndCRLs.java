package CAdES;

import CAdES.configuration.SimpleConfiguration;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.util.CollectionStore;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DirList;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;

public class AddCertificatesAndCRLs {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @exception Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        main0("unixCadesTestCerts2012-Usr256", "c2",
        "unixCadesTestCerts2012-Usr512", "c3",
                SimpleConfiguration.CRL_PATH, JCP.PROVIDER_NAME, JCP.HD_STORE_NAME);
    }

    public static void main0(final String containerAlias0,  final String containerPassword0,
            final String containerAlias1,  final String containerPassword1, final String crlPath1,
            final String providerName, final String keyStoreType) throws Exception {

        System.setProperty("com.ibm.security.enableCRLDP", "true");

        //Создание первой подписи
        byte[] data0 = new byte[0];
        byte[] sign0 = createSignatureWithCRL(containerAlias0, containerPassword0, null, providerName, keyStoreType, data0);

        //Создание второй подписи с СОС
        byte[] data1 = new byte[0];
        byte[] sign1 = createSignatureWithCRL(containerAlias1, containerPassword1, crlPath1, providerName, keyStoreType, data1);

        //Добавляем подписантов, сертификаты и СОС из первой подписи во вторую
        byte[] sign2 = mergeSignerCertAndCrl(sign0, data0, sign1, data1);

        //Проверка подписи
        CAdESSignature cAdESSignature2 = new CAdESSignature(sign2, data0, CAdESType.CAdES_BES);
        cAdESSignature2.verify(null);
    }

    /**
     * Создание CAdES подписи с СОС.
     *
     * @param containerAlias Псевдоним контейнера.
     * @param containerPassword Пароль от контейнера.
     * @param crlPath Путь до файла СОС. null, если СОС не добавляем.
     * @param providerName Название провайдера.
     * @param keyStoreType Тип keyStore.
     * @param dataToSign Данные для создания подписи.
     * @return Подпись.
     * @throws Exception
     */
    public static byte[] createSignatureWithCRL(final String containerAlias,  final String containerPassword,
        final String crlPath, final String providerName, final String keyStoreType, byte[] dataToSign) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(keyStoreType, providerName);
        keyStore.load(null, null);
        PrivateKey key = (PrivateKey)keyStore.getKey(containerAlias, containerPassword.toCharArray());
        Certificate[] certs = keyStore.getCertificateChain(containerAlias);
        X509Certificate[] xCerts = new X509Certificate[certs.length];
        System.arraycopy(certs, 0, xCerts, 0, certs.length);

        Collection<X509CertificateHolder> certHolders = new ArrayList<X509CertificateHolder>(1);
        certHolders.add(new X509CertificateHolder(xCerts[0].getEncoded()));

        CAdESSignature cAdESSignature = new CAdESSignature();
        CollectionStore store = new CollectionStore(certHolders);
        cAdESSignature.setCertificateStore(store);
        ByteArrayOutputStream signature = new ByteArrayOutputStream();

        if(crlPath != null) {
            X509CRL crl = DirList.getCRLByInputStream(Files.newInputStream(Paths.get(crlPath)));
            Collection<X509CRLHolder> crlHolders = new ArrayList<>(1);
            crlHolders.add(new X509CRLHolder(crl.getEncoded()));
            CollectionStore crlStore = new CollectionStore(crlHolders);
            cAdESSignature.setCRLStore(crlStore);
        }

        cAdESSignature.addSigner(providerName, key, Arrays.asList(xCerts), CAdESType.CAdES_BES, null, false);
        cAdESSignature.open(signature);
        cAdESSignature.update(dataToSign);
        cAdESSignature.close();
        signature.close();
        return signature.toByteArray();
    }

    /**
     * Добавляем подписантов, сертификаты и СОС из подписи №1 в подпись №0.
     *
     * @param sign0 Подпись №0.
     * @param data0 Данные, использовавшиеся для подписи №0.
     * @param sign1 Подпись №1.
     * @param data1 Данные, использовавшиеся для подписи №1.
     * @return Подпись №0 с добавленными подписантами, сертификатами и СОС из подписи №1.
     * @throws Exception
     */
    public static byte[] mergeSignerCertAndCrl(byte[] sign0, byte[] data0, byte[] sign1, byte[] data1) throws Exception {

        //Чтение подписантов, сертификатов и СОС из первых двух подписей
        Collection<SignerInformation> signerInfos = new ArrayList<>();
        Collection<X509CRLHolder> crlHolders = new ArrayList<>();
        Collection<X509CertificateHolder> certHolders = new ArrayList<>();

        CAdESSignature cAdESSignature0 = new CAdESSignature(sign0, data0, CAdESType.CAdES_BES);
        for(Object entry : cAdESSignature0.getCrlStore()) {
            crlHolders.add((X509CRLHolder)entry);
        }
        for (CAdESSigner signer : cAdESSignature0.getCAdESSignerInfos()) {
            signerInfos.add(signer.getSignerInfo());
        }
        for(Object entry : cAdESSignature0.getCertificateStore()) {
            certHolders.add((X509CertificateHolder)entry);
        }

        CAdESSignature cAdESSignature1 = new CAdESSignature(sign1,data1,CAdESType.CAdES_BES);
        for(Object entry : cAdESSignature1.getCrlStore()) {
            crlHolders.add((X509CRLHolder)entry);
        }
        for (CAdESSigner signer : cAdESSignature1.getCAdESSignerInfos()) {
            signerInfos.add(signer.getSignerInfo());
        }
        for(Object entry : cAdESSignature1.getCertificateStore()) {
            certHolders.add((X509CertificateHolder)entry);
        }

        //Добавляем новый список подписантов в подпись №0
        SignerInformationStore signerInformationStore = new SignerInformationStore(signerInfos);
        ByteArrayOutputStream sign0NewSignersOutputStream = new ByteArrayOutputStream();
        ByteArrayInputStream sign0InputStream = new ByteArrayInputStream(sign0);

        CAdESSignature.replaceSigners(sign0InputStream,signerInformationStore, sign0NewSignersOutputStream);

        sign0InputStream.close();
        byte[] sign0NewSigners = sign0NewSignersOutputStream.toByteArray();
        sign0NewSignersOutputStream.close();

        //Добавляем новый список сертификатов и СОС в подпись №0
        ByteArrayInputStream sign0NewSignersInputStream = new ByteArrayInputStream(sign0NewSigners);
        ByteArrayOutputStream sign0ResultOutputStream = new ByteArrayOutputStream();
        CollectionStore storeCerts = new CollectionStore(certHolders);
        CollectionStore storeCRL= new CollectionStore(crlHolders);

        CAdESSignature.replaceCertificatesAndCRLs(sign0NewSignersInputStream, storeCerts, storeCRL,
            null, sign0ResultOutputStream);

        sign0NewSignersInputStream.close();
        sign0ResultOutputStream.close();
        return sign0ResultOutputStream.toByteArray();
    }
}
