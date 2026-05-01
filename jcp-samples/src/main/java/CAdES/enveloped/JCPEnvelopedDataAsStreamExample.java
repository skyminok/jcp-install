/**
 * $RCSfileJCPEnvelopedDataAsStreamExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 11:07 by afevma
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
package CAdES.enveloped;

import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.ServerContainer2012_256;
import CAdES.configuration.container.ServerContainer2012_512;
import CAdES.configuration.container.ServerSigContainer2012_256;
import CAdES.configuration.container.ServerSigContainer2012_512;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.*;

/**
 * Пример создания и расшифрования подписи Enveloped CMS
 * из строки в потоке с помощью провайдера JCP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPEnvelopedDataAsStreamExample implements IEnvelopedData {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        main_group_exchange(DATA_DIR, TEMP_DIR, true);
        main_group_signature(DATA_DIR, TEMP_DIR, false);
    }

    /**
     * Выполнение группы заданий на ключах обмена.
     *
     * @param dataDir Папка с данными.
     * @param tmpDir Папка для результатов.
     * @throws Exception
     */
    public static void main_group_exchange(String dataDir, String tmpDir,
        boolean transport) throws Exception {

        // System.out.println("*** Exchange group ***");

        envelope(null, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(null, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение группы заданий на ключах подписи.
     *
     * @param dataDir Папка с данными.
     * @param tmpDir Папка для результатов.
     * @throws Exception
     */
    public static void main_group_signature(String dataDir, String tmpDir,
        boolean transport) throws Exception {

        // System.out.println("*** Signature group ***");

        envelope(null, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(null, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma,dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac,dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, dataDir, tmpDir,
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение набора примеров.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param dataPath Папка с данными.
     * @param tempPath Папка для сохранения результатов.
     * @param recipientConfig Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void envelope(EncryptionKeyAlgorithm alg, String dataPath,
       String tempPath, IConfiguration recipientConfig, boolean transport)
       throws Exception {
        // --- Зашифрование/расшифрование строки ---
        encryptDecrypt(alg, recipientConfig, dataPath, DATA_FILE,
            tempPath, true, transport);
    }

    /**
     * Пример зашифрования данных в Enveloped CMS и расшифрования.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param recipient Конфигурация получателя.
     * @param dataPath Папка с данными.
     * @param dataFile Файл с данными для зашифрования.
     * @return путь к файлу с расшифрованным сообщением.
     * @param tempPath Папка для сохранения результатов.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static String encryptDecrypt(EncryptionKeyAlgorithm alg,
        IConfiguration recipient, String dataPath, String dataFile,
        String tempPath, boolean compareArrays, boolean transport)
        throws Exception {

        String sourceFile = new File(dataPath, dataFile).getAbsolutePath();

        String envelopedFile = new File(tempPath, dataFile +
            (transport ? ".trans" : ".agree") + ".enveloped").getAbsolutePath();

        String decryptedFile = new File(tempPath, dataFile +
            (transport ? ".trans" : ".agree") + ".decrypted").getAbsolutePath();

        // System.out.println("Source file: " + sourceFile);
        // System.out.println("Enveloped file: " + envelopedFile);
        // System.out.println("Decrypted file: " + decryptedFile);

        encryptAsStream(alg, recipient, sourceFile, envelopedFile, transport);
        decryptAsStream(recipient, envelopedFile, decryptedFile);

        // Большие файлы нежелательно так сравнивать, но в примере
        // сделаем, чтобы проверить
        if (compareArrays) {

            byte[] sourceData = Array.readFile(sourceFile);
            byte[] decryptedData = Array.readFile(decryptedFile);

            if (!Array.compare(decryptedData, sourceData)) {
                throw new Exception("Decryption failed, source data and decrypted data are not equal");
            } // if

        } // if

        return decryptedFile;
    }

    /**
     * Зашифрование данных в формате Enveloped CMS.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param recipientConfig Конфигурация получателя.
     * @param dataFile Файл с данными для зашифрования.
     * @param envelopedFile Файл для записи в него Enveloped CMS.
     * @throws Exception
     */
    private static void encryptAsStream(EncryptionKeyAlgorithm alg,
        IConfiguration recipientConfig,  String dataFile, String
        envelopedFile, boolean transport) throws Exception {

        InputStream dataFileStream = new FileInputStream(dataFile);
        OutputStream envelopedFileOutStream = new FileOutputStream(envelopedFile);

        EnvelopedSignature signatureStream = new EnvelopedSignature(alg);

        if (transport) {
            signatureStream.addKeyTransRecipient(recipientConfig.getCertificate());
        } // if
        else {
            signatureStream.addKeyAgreeRecipient(recipientConfig.getCertificate());
        } // else

        signatureStream.open(envelopedFileOutStream);

        // Чтение данных
        final int bufferSize = 1024 * 1024 * 16;
        byte[] buffer = new byte[bufferSize];
        int read = dataFileStream.read(buffer);

        // Шифрование данных
        while (read > 0) {
            signatureStream.update(buffer, 0, read);
            Array.clear(buffer);
            read = dataFileStream.read(buffer, 0, bufferSize);
        } // while

        signatureStream.close();
        dataFileStream.close();
        envelopedFileOutStream.close();

    }

    /**
     * Расшифрование Enveloped CMS.
     *
     * @param recipientConfig Конфигурация получателя.
     * @param envelopedFile Файл для чтения из него Enveloped CMS.
     * @param decryptedFile Файл для записи расшифрованных данных.
     * @throws Exception
     */
    public static void decryptAsStream(
        IConfiguration recipientConfig,  String envelopedFile, String
        decryptedFile) throws Exception {

        InputStream encryptedDataFile = new FileInputStream(envelopedFile);
        FileOutputStream outDataFile = new FileOutputStream(decryptedFile);

        EnvelopedSignature signatureStream = new EnvelopedSignature(encryptedDataFile);

        InputStream decryptedDataStream = signatureStream.decrypt(
            recipientConfig.getCertificate(), recipientConfig.getPrivateKey());

        // Чтение данных
        final int bufferSize = 1024 * 1024 * 16;
        byte[] buffer = new byte[bufferSize];
        int read = decryptedDataStream.read(buffer);

        // Чтение из потока и запись в файл
        while (read > 0) {
            outDataFile.write(buffer, 0, read);
            Array.clear(buffer);
            read = decryptedDataStream.read(buffer, 0, bufferSize);
        } // while

        decryptedDataStream.close();
        encryptedDataFile.close();
        outDataFile.close();

    }

}
