/**
 * $RCSfileCadesAExample.java,v $
 * version $Revision$
 * created 02.07.2018 19:35 by la
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2018.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CAdES;

import CAdES.configuration.Configuration;
import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2012_256;
import CAdES.configuration.container.ISignatureContainer;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;

import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.*;
import java.util.*;

/**
 * Пример создания и усовершенствования CAdES-A.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CadesAExample {

    static {
        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    public static void main(String[] args) {
        JCPInit.initProviders(false);
        if (args[0].equals("sign"))
            CadesAExample.signExample(new Container2012_256(), false);
        else if (args[0].equals("enh_x"))
            CadesAExample.enhanceExample(new Container2012_256(), "CadesXLT1",
                "CadesXLT1Ench", false, VerifyExample.SignatureType.ST_CADES_X_LONG_TYPE_1);
        else if (args[0].equals("enh_a"))
            CadesAExample.enhanceExample(new Container2012_256(), "CadesA", "CadesAEnch",
                false, VerifyExample.SignatureType.ST_CADES_A);
        else if (args[0].equals("ver_a"))
            CadesAExample.verifyExample(new Container2012_256(), "CadesA", false);
        else if (args[0].equals("ver_x"))
            CadesAExample.verifyExample(new Container2012_256(), "CadesXLT1", false);
        else if (args[0].equals("ver_ex"))
            CadesAExample.verifyExample(new Container2012_256(), "CadesXLT1Ench", false);
        else if (args[0].equals("ver_ea"))
            CadesAExample.verifyExample(new Container2012_256(), "CadesAEnch", false);
    }

    /**
     * Создание  CAdES-A подписи.
     *
     * @param container Описание используемого ключевого контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void signExample(ISignatureContainer container, boolean useStream) {

        try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);
            createSignature(configAttached, "CadesXLT1", CAdESType.CAdES_X_Long_Type_1);
            createSignature(configAttached, "CadesA", CAdESType.CAdES_A);

        } catch (Exception e) {
            Configuration.printCAdESException(e);
        }
    }


    /**
     * Создание и проверка CAdES подписи.
     *
     * @param container Описание используемого ключевого контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void enhanceExample(ISignatureContainer container, String
        inFileName, String outFileName, boolean useStream, VerifyExample.SignatureType
        sigType) {

        try {

            String outPath = SimpleConfiguration.getTempFileName(outFileName);
            IConfiguration configAttached = new SimpleConfiguration(container, useStream);

            // Сохраним подпись либо в файл, либо в массив.

            OutputStream newCMSSignedDataBuffer = configAttached.useStream()
                ? new FileOutputStream(outPath) : new ByteArrayOutputStream();

            InputStream cadesCms;

            if (configAttached.useStream()) {
                cadesCms = new FileInputStream(SimpleConfiguration.getTempFileName(inFileName));
            } // if
            else {
                // Читаем подпись из файла.
                byte[] tmp = Array.readFile(SimpleConfiguration.getTempFileName(inFileName));
                cadesCms = new ByteArrayInputStream(tmp);
            } // else

            // Подпись в тесте была совмещенная, потому данные равны null.
            // Предположим, что подписей несколько, тогда лучше указать
            // тип null и положиться на самоопределение типа подписи.

            if (cadesCms instanceof ByteArrayInputStream) {
                if (cadesCms.markSupported()) {
                    cadesCms.mark(0);
                } // if
            } // if

            // Прочитывает cadesCms.

            CAdESSignature cadesSignature = VerifyExample.verifyCAdESSignature(
                cadesCms, configAttached, sigType);

            if (cadesCms instanceof ByteArrayInputStream) {
                cadesCms.reset();
            } // if
            InputStream srcSignedDataStream;

            // Получаем подпись из файла или из массива.
            if (configAttached.useStream()) {

                if (cadesCms instanceof FileInputStream) {

                    FileInputStream tmp = (FileInputStream)cadesCms;
                    tmp.getChannel().position(0);
                    srcSignedDataStream = cadesCms;

                } // if
                else {
                    throw new Exception("Invalid file stream");
                } // else

            } // if
            else {

                if (cadesCms instanceof ByteArrayInputStream) {
                    srcSignedDataStream = cadesCms;
                } // if
                else {
                    throw new Exception("Invalid byte stream");
                } // else

            } // else

            // Исходный подписант.
            CAdESSigner srcSigner = cadesSignature.getCAdESSignerInfo(0);

            srcSigner = srcSigner.enhance(configAttached.getProviderName(),
                    configAttached.getDigestOid(),
                    new LinkedList<>(configAttached.getChain()),
                    configAttached.getTSAAddress(),
                    CAdESType.CAdES_A,
                    configAttached.getUnsignedAttributes());

            // Усовершенствованный подписант.
            SignerInformation enhSigner = srcSigner.getSignerInfo();

            // Список усовершенствованных подписантов.
            SignerInformationStore dstSignerInfoStore =
                new SignerInformationStore(Collections.singletonList(enhSigner));

            // Обновляем исходную подпись с ее начальным списком подписантов
            // на тот же, но с первым подписантом с усовершенствованной подписью.

            CAdESSignature.replaceSigners(srcSignedDataStream, dstSignerInfoStore, newCMSSignedDataBuffer);
            newCMSSignedDataBuffer.close();
            srcSignedDataStream.close();

            InputStream dstSignedData = null;

            // Чтение подписи из файла или массива.
            if (configAttached.useStream()) {
                dstSignedData = new FileInputStream(outPath);
            } // if
            else if (newCMSSignedDataBuffer instanceof ByteArrayOutputStream) {

                byte[] newCMSSignedData = ((ByteArrayOutputStream)newCMSSignedDataBuffer).toByteArray();

                if (outPath != null) {
                    Array.writeFile(outPath, newCMSSignedData);
                } // if

                dstSignedData = new ByteArrayInputStream(newCMSSignedData);

            } // else

            // 3. Проверка усовершенствованной подписи.

            cadesSignature = VerifyExample.verifyCAdESSignature(
                dstSignedData, configAttached, VerifyExample.SignatureType.ST_CADES_A);

            // Configuration.printSignatureInfo(cadesSignature);
            cadesCms.close();
            if (dstSignedData != null) {
                dstSignedData.close();
            }

        } catch (Exception e) {
            // e.printStackTrace();
            Configuration.printCAdESException(e);
        }
    }



    /**
     * Проверка CAdES подписи.
     *
     * @param container Описание используемого ключевого контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void verifyExample(ISignatureContainer container, String
        inFileName, boolean useStream) {

        try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);
            InputStream verifyStream = new FileInputStream(SimpleConfiguration.getTempFileName(inFileName));
            VerifyExample.verifyCAdESSignature(verifyStream, configAttached, VerifyExample.SignatureType.ST_CADES_A);

        } catch (Exception e) {
            Configuration.printCAdESException(e);
        }
    }

    /**
     * Создание CAdES-подписи с двумя подписантами: CAdES-BES и
     * CAdES-X Long Type 1.
     *
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения подписи.
     * @throws Exception
     */
    public static void createSignature(IConfiguration config, String
        outFileName, Integer sigType) throws Exception {

        CAdESSignature cadesSignature = new CAdESSignature(config.detached());

        String outPath = SimpleConfiguration.getTempFileName(outFileName);
        cadesSignature.setCertificateStore(config.getCertificateStore());
        cadesSignature.setCRLStore(config.getCRLStore());

        // Создаем подписанта CAdES-A.
        cadesSignature.addSigner(config.getProviderName(),
                config.getDigestOid(),
                config.getPublicKeyOid(),
                config.getPrivateKey(),
                config.getChain(),
                sigType,
                config.getTSAAddress(),
                false,
                null,
                null,
                config.getCRLs());

        // Сохраним подпись либо в файл, либо в массив.
        OutputStream outSignatureStream = config.useStream()
            ? new FileOutputStream(outPath) : new ByteArrayOutputStream();

        cadesSignature.open(outSignatureStream);
        InputStream dataStream = config.getDataStream();
        SignExample.cadesSignatureUpdate(cadesSignature, dataStream); // хеш

        // Завершаем создание подписи с двумя подписантами.

        cadesSignature.close();
        dataStream.close();
        outSignatureStream.close();

        InputStream dstSignedData = null;

        // Чтение подписи из файла или массива.
        if (config.useStream()) {
            dstSignedData = new FileInputStream(outPath);
        } // if
        else if (outSignatureStream instanceof ByteArrayOutputStream) {

            byte[] newCMSSignedData = ((ByteArrayOutputStream)outSignatureStream).toByteArray();

            if (outPath != null) {
                Array.writeFile(outPath, newCMSSignedData);
            } // if

            dstSignedData = new ByteArrayInputStream(newCMSSignedData);

        } // else

        // 3. Проверка усовершенствованной подписи.

        cadesSignature = VerifyExample.verifyCAdESSignature(
            dstSignedData, config,
            sigType.equals(CAdESType.CAdES_A)
                ? VerifyExample.SignatureType.ST_CADES_A
                : VerifyExample.SignatureType.ST_CADES_X_LONG_TYPE_1
        );

        // Configuration.printSignatureInfo(cadesSignature);

    }

}
