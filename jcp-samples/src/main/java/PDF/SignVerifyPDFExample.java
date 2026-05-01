/**
 * $RCSfileSignPDFExample.java,v $
 * version $Revision: 36379 $
 * created 01.09.2014 11:07 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 * Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package PDF;

import java.io.*;

import java.security.*;
import java.security.cert.CRL;
import java.security.cert.Certificate;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.pdf.*;

import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.tsp.TimeStampToken;

import ru.CryptoPro.Crypto.CryptoProvider;

import ru.CryptoPro.JCP.JCP;

import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Пример подписи и проверки PDF документа.
 * Используется пропатченный itextpdf версии 5.5.5
 * с патчем.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class SignVerifyPDFExample {

    /**
     * Исходный PDF документ.
     */
    private static final String IN_PDF_FILE =
        System.getProperty("user.dir")
            + File.separator
            + "data"
            + File.separator
            + "PDF"
            + File.separator
            + "source.pdf";

    /**
     * Папка для сохранения подписанных PDF документов.
     */
    private static final String OUT_PDF_FILE =
        System.getProperty("user.dir")
        + File.separator
        + "temp"
        + File.separator;

    private static final int CA_KEY_USAGE = GostCertificateRequest.CENTER_DEFAULT;
    private static final int USER_KEY_USAGE = GostCertificateRequest.DIGITAL_SIGNATURE | GostCertificateRequest.NON_REPUDIATION;
    private static final int[][] USER_EKU = {GostCertificateRequest.INTS_PKIX_EMAIL_PROTECTION, new int[] {1, 3, 6, 1, 5, 5, 7, 3, 3}};

    public interface PdfSignatureCallback {
        void processSignature(String digestOid, String encryptionOid);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

        JCPInit.initProviders(false);
        // ГОСТ Р 34.10-2001
        //
        // sign(
        //     genKeyPair(JCP.GOST_EL_DH_NAME, CryptoProvider.PROVIDER_NAME),
        //     JCP.GOST_DIGEST_NAME,
        //     JCP.GOST_EL_SIGN_NAME,
        //     JCP.PROVIDER_NAME,
        //     "CN=exc_2001, C=RU",
        //     root,
        //     IN_PDF_FILE,
        //     OUT_PDF_FILE + "signed.2001.pdf",
        //     "Crypto-Pro LLC", "Test signature (2001)",
        //     false,
        //     Certificates.HTTP_ADDRESS
        // );
        //
        // verify(OUT_PDF_FILE + "signed.2001.pdf", null, null, JCP.PROVIDER_NAME);

        // ГОСТ Р 34.10-2012 (256)

        sign(
            genKeyPair(JCP.GOST_DH_2012_256_NAME, CryptoProvider.PROVIDER_NAME),
            genKeyPair(JCP.GOST_DH_2012_256_NAME, CryptoProvider.PROVIDER_NAME),
            JCP.GOST_DIGEST_2012_256_NAME,
            JCP.GOST_SIGN_2012_256_NAME,
            JCP.PROVIDER_NAME,
            "CN=exc_2012_256, C=RU",
            IN_PDF_FILE,
            OUT_PDF_FILE + "signed.2012_256.pdf",
            "Crypto-Pro LLC", "Test signature (2012-256)",
            false,
            null
        );

        verify(OUT_PDF_FILE + "signed.2012_256.pdf", null, null, JCP.PROVIDER_NAME);

        // ГОСТ Р 34.10-2012 (512)

        sign(
            genKeyPair(JCP.GOST_DH_2012_512_NAME, CryptoProvider.PROVIDER_NAME),
            genKeyPair(JCP.GOST_DH_2012_512_NAME, CryptoProvider.PROVIDER_NAME),
            JCP.GOST_DIGEST_2012_512_NAME,
            JCP.GOST_SIGN_2012_512_NAME,
            JCP.PROVIDER_NAME,
            "CN=exc_2012_512, C=RU",
            IN_PDF_FILE,
            OUT_PDF_FILE + "signed.2012_512.pdf",
            "Crypto-Pro LLC", "Test signature (2012-512)",
            false,
            null
        );

        verify(OUT_PDF_FILE + "signed.2012_512.pdf", null, null, JCP.PROVIDER_NAME);

	}

    /**
     * Генерация ключевой пары.
     *
     * @param keyAlgName Алгоритм ключа.
     * @param provider Имя провайдера.
     * @return ключевая пара.
     * @throws Exception
     */
    public static KeyPair genKeyPair(String keyAlgName, String provider) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyAlgName, provider);
        return kpg.generateKeyPair();
    }

    /**
     * Создание сертификата.
     *
     * @param ownPair Клиентская ключевая пара.
     * @param rootPair Ключевая пара подписи сертификата. Может быть null.
     * @param signAlgName Алгоритм подписи.
     * @param signProvider Имя провайдера.
     * @param dnName Имя субъекта сертификата.
     * @param keyUsage Назначение ключа.
     * @param eku Список политик для расширенного использования ключа.
     * @return сертификат.
     * @throws Exception
     */
    public static Certificate createCertificate(KeyPair ownPair, KeyPair rootPair, String signAlgName,
        String signProvider, String dnName, int keyUsage, int[][] eku) throws Exception {

        GostCertificateRequest request = new GostCertificateRequest(signProvider);
        final String keyAlgorithm = ownPair.getPrivate().getAlgorithm();
        request.init(keyAlgorithm);

        request.setKeyUsage(keyUsage);
        if (eku != null) {
            for (int[] e : eku) {
                request.addExtKeyUsage(e);
            }
        }
        request.setPublicKeyInfo(ownPair.getPublic());
        request.setSubjectInfo(dnName);

        KeyPair kp = rootPair != null ? new KeyPair(null, rootPair.getPrivate()) : ownPair;
        byte[] encodedCert = request.getEncodedSelfCert(kp, dnName, signAlgName);
        return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(encodedCert));

    }

    /**
     * Подпись PDF документа.
     *
     * @param ownPair Ключевая пара.
     * @param rootPair Ключевая пара для подписи сертификата.
     * @param hashAlgorithm Алгоритм хеширования.
     * @param signAlgName Алгоритм подписи.
     * @param signProvider Провайдер хеширования и подписи.
     * @param dnName DN для создания сертификата.
     * @param fileToSign Исходный PDF документ.
     * @param signedFile Подписанный PDF документ.
     * @param location Адрес.
     * @param reason Описание.
     * @param isCAdES True, если подпись формата CAdES.
     * @param chain Созданная цепочка сертификатов. Может быть null.
     * @throws Exception
     */
    public static void sign(KeyPair ownPair, KeyPair rootPair,
        String hashAlgorithm, String signAlgName, String
        signProvider, String dnName, String fileToSign, String
        signedFile, String location, String reason, boolean
        isCAdES, Certificate[] chain) throws Exception {

        sign(ownPair, rootPair, hashAlgorithm, signAlgName, signProvider,
            dnName, fileToSign, signedFile, location, reason,
                false, isCAdES, chain);

    }

    /**
     * Подпись PDF документа.
     *
     * @param keyPair Ключевая пара.
     * @param chain Созданная цепочка сертификатов. Может быть null.
     * @param hashAlgorithm Алгоритм хеширования.
     * @param signAlgName Алгоритм подписи.
     * @param signProvider Провайдер хеширования и подписи.
     * @param dnName DN для создания сертификата.
     * @param fileToSign Исходный PDF документ.
     * @param signedFile Подписанный PDF документ.
     * @param append True, если добавляется еще одна подпись.
     * @param location Адрес.
     * @param reason Описание.
     * @param isCAdES True, если подпись формата CAdES.
     * @param chain Созданная цепочка сертификатов. Может быть null.
     * @throws Exception
     */
    public static void sign(KeyPair keyPair, KeyPair rootPair,
        String hashAlgorithm, String signAlgName, String
        signProvider, String dnName, String fileToSign, String
        signedFile, String location, String reason, boolean append,
        boolean isCAdES, Certificate[] chain) throws Exception {

        Certificate root = createCertificate(rootPair, null, signAlgName, signProvider, dnName, CA_KEY_USAGE, null);
        Certificate signer = createCertificate(keyPair, rootPair, signAlgName, signProvider, dnName, USER_KEY_USAGE, USER_EKU);

        signer.verify(root.getPublicKey(), signProvider);

        if (chain != null && chain.length == 2) {
            chain[0] = signer;
            chain[1] = root;
        }
        else {
            chain = new Certificate[2];
            chain[0] = signer;
            chain[1] = root;
        }

        sign(keyPair.getPrivate(), hashAlgorithm, signProvider,
            chain, fileToSign, signedFile, location, reason,
                append, isCAdES);

    }

	/**
	 * Подпись PDF документа.
	 *
     * @param privateKey Ключ подписи.
     * @param hashAlgorithm Алгоритм хеширования.
     * @param signProvider Провайдер хеширования и подписи.
     * @param chain Цепочка сертификатов.
	 * @param fileToSign Исходный PDF документ.
	 * @param signedFile Подписанный PDF документ.
     * @param append True, если добавляется еще одна подпись.
     * @param location Адрес.
     * @param reason Описание.
     * @param isCAdES True, если подпись формата CAdES.
	 * @throws Exception
	 */
	public static void sign(PrivateKey privateKey, String hashAlgorithm,
        String signProvider, Certificate[] chain, String fileToSign,
        String signedFile, String location, String reason, boolean append,
        boolean isCAdES) throws Exception {

		PdfReader reader = new PdfReader(fileToSign);
		FileOutputStream fout = new FileOutputStream(signedFile);

		PdfStamper stp = append
            ? PdfStamper.createSignature(reader, fout, '\0', null, true)
            : PdfStamper.createSignature(reader, fout, '\0');

		PdfSignatureAppearance sap = stp.getSignatureAppearance();

        sap.setCertificate(chain[0]);
		sap.setReason(reason);
		sap.setLocation(location);

        // в сэмплах КриптоПро используется патченная версия itext
        PdfSignature dic = new PdfSignature(PdfName.ADOBE_CryptoProPDF,
            isCAdES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);

        dic.setReason(sap.getReason());
        dic.setLocation(sap.getLocation());
        dic.setSignatureCreator(sap.getSignatureCreator());
        dic.setContact(sap.getContact());
        dic.setDate(new PdfDate(sap.getSignDate())); // time-stamp will over-rule this

        sap.setCryptoDictionary(dic);
        int estimatedSize = 8192;

        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
        exc.put(PdfName.CONTENTS, new Integer(estimatedSize * 2 + 2));

        sap.preClose(exc);

        PdfPKCS7 sgn = new PdfPKCS7(privateKey, chain, hashAlgorithm, signProvider, new BouncyCastleDigest(), false);
        InputStream data = sap.getRangeStream();

        MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        byte[] hash = DigestAlgorithms.digest(data, md);

        Calendar cal = Calendar.getInstance();

        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, null, null,
            isCAdES ? MakeSignature.CryptoStandard.CADES : MakeSignature.CryptoStandard.CMS);

        sgn.update(sh, 0, sh.length);

        byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal, null, null, null,
            isCAdES ? MakeSignature.CryptoStandard.CADES : MakeSignature.CryptoStandard.CMS);

        if (estimatedSize < encodedSig.length) {
            throw new IOException("Not enough space");
        } // if

        byte[] paddedSig = new byte[estimatedSize];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);

        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));

        sap.close(dic2);
		stp.close();

		fout.close();
		reader.close();

	}

    /**
     * Проверка подписи PDF документа.
     *
     * @param fileToVerify PDF документ для проверки.
     * @param trustStore Хранилище с корневыми сертификатами.
     * @param crl CRL для проверки цепочки сертификатов.
     * @param provider Имя провайдера для проверки подписи.
     * @return количество подписей.
     * @throws Exception
     */
    public static int verify(String fileToVerify, KeyStore trustStore, CRL crl, String provider) throws Exception {
        return verify(fileToVerify, trustStore, crl, provider, null);
    }

    /**
     * Проверка подписи PDF документа.
     *
     * @param fileToVerify PDF документ для проверки.
     * @param trustStore Хранилище с корневыми сертификатами.
     * @param crl CRL для проверки цепочки сертификатов.
     * @param provider Имя провайдера для проверки подписи.
     * @return количество подписей.
     * @throws Exception
     */
    public static int verify(String fileToVerify, KeyStore trustStore, CRL crl, String provider,
        PdfSignatureCallback pdfSignatureCallback) throws Exception {

        List<CRL> crlList = null;

        if (crl != null) {
            crlList = new ArrayList<CRL>(1);
            crlList.add(crl);
        } // if

        PdfReader checker = new PdfReader(fileToVerify);
        AcroFields af = checker.getAcroFields();

        ArrayList<String> signatureNames = af.getSignatureNames();

        if (signatureNames.isEmpty()) {
            throw new Exception("Signatures not found.");
        } // if

        for (String signatureName : signatureNames) {

            // System.out.println("Signature: " + signatureName);
            PdfPKCS7 pk = af.verifySignature(signatureName, provider);

            boolean verified = pk.verify();

            if (!verified) {
                throw new Exception("Invalid signature: " + signatureName);
            } // if

            if (pdfSignatureCallback != null) {
                pdfSignatureCallback.processSignature(pk.getDigestAlgorithmOid(), pk.getDigestEncryptionAlgorithmOid());
            } // if

            // System.out.println("Signer certificate: " + pk.getSigningCertificate().getSubjectDN());

            Calendar calendar = pk.getSignDate();
            // System.out.println("Signature date: " + calendar);

            X509Certificate[] pkc = (X509Certificate[]) pk.getSignCertificateChain();
            TimeStampToken ts = pk.getTimeStampToken();

            if (ts != null) {
                boolean imprint = pk.verifyTimestampImprint();
                // System.out.println("Timestamp imprint verified: " + imprint);
                calendar = pk.getTimeStampDate();
                // System.out.println("Timestamp date: " + calendar);
            } // if

            System.out.println("Document wasn't modified.");

            if (trustStore != null) {

                List<VerificationException> fails = CertificateVerification
                    .verifyCertificates(pkc, trustStore, crlList, calendar);

                if (fails.isEmpty()) {
                    // System.out.println("Certificates verified against the key store");
                } // if
                else {
                    System.err.println("Certificate validation failed: ");
                    for (VerificationException fail : fails) {
                        fail.printStackTrace();
                    } // for
                    throw new Exception("Validation failed.");
                } // else

            } // if

        } // for

        return signatureNames.size();

    }

}
