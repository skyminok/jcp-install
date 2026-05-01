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
package CMS_samples;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.util.Store;
import ru.CryptoPro.CAdES.tools.verifier.*;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Пример поточной совмещенной/отделенной подписи и проверки файла (УЭК ЭДО)
 * большого размера. Файл читается порциями. Используется криптопровайдер
 * Bouncycastle с переопределением ГОСТ алгоритмов.
 *
 * 24/07/2012
 *
 */
public class LargeFileTest {

    /**
     * Адиас ключа.
     */
    private static final String ALIAS = "bukin_exch";
    /**
     * Пароль к контейнеру.
     */
    private static final char[] PASSWORD = "Pass1234".toCharArray();
    /**
     * Входящий файл с данными.
     */
    private static final String DATA_FILE = "C:\\large.file";
    /**
     * Исходящий файл с подписью (совмещенная).
     */
    private static final String SIGNATURE_FILE = "C:\\attached.signature";
    /**
     * Флаг, что подпись совмещенная.
     */
    private static final boolean attached = true;
    /**
     * Размер буфера для чтения файла с данными.
     */
    private static final int BUFFER_SIZE = 8 * 1024 * 1024;

    public static void main(String[] args) throws Exception {

        // I. Подготовка.

        // Баунсикасловский провайдер.
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // Переопределяем алгоритмы на Крипто-Про.
        org.bouncycastle.cms.CMSConfig.setSigningDigestAlgorithmMapping(JCP.GOST_DIGEST_OID, JCP.GOST_DIGEST_NAME);
        org.bouncycastle.cms.CMSConfig.setSigningEncryptionAlgorithmMapping(JCP.GOST_EL_DH_OID, JCP.GOST_EL_DH_NAME);
        org.bouncycastle.cms.CMSConfig.setSigningEncryptionAlgorithmMapping(JCP.GOST_EL_KEY_OID, JCP.GOST_EL_DEGREE_NAME);

        JCPInit.initProviders(false);

        // Грузим ключ и сертификат.
        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        // Первый в списке сертификатов - сертификат ключа подписи.
        PrivateKey privKey = (PrivateKey)keyStore.getKey(ALIAS, PASSWORD);
        Certificate[] certChain = keyStore.getCertificateChain(ALIAS);

        // II. Создание подписи.

        long startTime = Calendar.getInstance().getTimeInMillis();

        // Поточный генератор.
        CMSSignedDataStreamGenerator signGen = new CMSSignedDataStreamGenerator();

        ContentSigner contentSigner = new GostContentSignerProvider(
            privKey, JCP.PROVIDER_NAME);

        SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(
            new GostDigestCalculatorProvider(privKey, JCP.PROVIDER_NAME)).build(
                contentSigner, (X509Certificate) certChain[0]);

        // Добавляем подписанта.
        signGen.addSignerInfoGenerator(signerInfoGenerator);

        ArrayList certList = new ArrayList();
        for ( int i = 0; i < certChain.length;i++) {
            certList.add(certChain[i]);
        }

        // Добавляемые в подпись сертификаты. У нас один сертификат.
        Store certStore = new JcaCertStore(certList);
        signGen.addCertificates(certStore);

        // Файловый поток для сохранения подписи и данных.
        FileOutputStream signatureFile = new FileOutputStream(SIGNATURE_FILE);

        // Готовим совмещенную подпись.
        OutputStream signatureOutStream = signGen.open(signatureFile, attached);

        // Входящие данные для подписи.
        FileInputStream fIn = new FileInputStream(DATA_FILE);
        FileChannel fInChannel = fIn.getChannel();

        // Буфер для чтения файла.
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        // Читаем файл частями и пишем в поток для хеширования и дальнейшей подписи.
        while ( fInChannel.read(buffer) != -1 ) {

            buffer.flip();

            byte[] byteBufferArray = new byte[buffer.remaining()];
            buffer.get(byteBufferArray);

            signatureOutStream.write(byteBufferArray);
            buffer.clear();
        }

        fInChannel.close();
        fIn.close();

        // Тут сработает генератор.
        signatureOutStream.close();
        signatureFile.close();

        System.out.println("Создали подпись: OK (" + (Calendar.getInstance().getTimeInMillis() - startTime) + " мс)");

        // III. Проверка подписи.

        startTime = Calendar.getInstance().getTimeInMillis();

        // Файловый поток читаемой подписи.
        FileInputStream fInSig = new FileInputStream(SIGNATURE_FILE);

        DigestCalculatorProvider digestCalculatorProvider =
            new GostDigestCalculatorProvider(privKey, JCP.PROVIDER_NAME);

        CMSSignedDataParser parser = null;

        // Декодируем подпись.
        if (attached) {
            parser = new CMSSignedDataParser(digestCalculatorProvider, fInSig);
        }
        else {
            try(FileInputStream is = new FileInputStream(DATA_FILE)) {
                CMSTypedStream dataStream = new CMSTypedStream(is);
                parser = new CMSSignedDataParser(digestCalculatorProvider, dataStream, fInSig);
            }
        }

        parser.getSignedContent().drain();

        // Список подписантов.
        SignerInformationStore signers = parser.getSignerInfos();

        // Список сертификатов для проверки подписи.
        Store cs = parser.getCertificates();
        Collection signerInfos = signers.getSigners();

        Iterator it = signerInfos.iterator();

        while (it.hasNext()) {

            // Получаем подписанта и соответствующий ему сертификат.
            X509Certificate cert = null;
            SignerInformation nextSigner = (SignerInformation)it.next();
            Collection certCollection = cs.getMatches(nextSigner.getSID());

            if (certCollection.isEmpty() ) {
                break;
            }
            else {
                Iterator certIt = certCollection.iterator();
                cert = (X509Certificate)certIt.next();
            }

            final SignerInformationVerifier signerVerifier = new SignerInformationVerifier(
                new GostCMSSignatureAlgorithmNameGenerator(),
                new GostSignatureAlgorithmIdentifierFinder(),
                new GostContentVerifierProvider(cert, JCP.PROVIDER_NAME),
                new GostDigestCalculatorProvider(privKey, JCP.PROVIDER_NAME));

            // Проверяем подпись.
            // Можно проверить в CSP так:
            // csptest -cmssfsign -verify -in "C:\attached.signature" -my УЦ -cades_disable
            if (nextSigner.verify(signerVerifier)) {
                System.out.println("ЭЦП проверена открытым ключом сертификата: " + cert.getSubjectDN());
            }

        }

        System.out.println("Проверили подпись: OK ( " + (Calendar.getInstance().getTimeInMillis() - startTime) + " мс)");

    }
}
