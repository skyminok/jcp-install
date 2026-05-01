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
package CAdES;

import CAdES.configuration.Configuration;
import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2001;
import CAdES.configuration.container.ISignatureContainer;

import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Пример добавления другого подписанта в подпись
 * CAdES-BES.
 *
 * 22/01/2013
 *
 */
public class AddOtherSignerExample {

    static {
        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    /**
     * Добавление одного подписанта в существующую подпись.
     *
     * @param inFileName Файл с CAdES-подписью.
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения расширенной подписи.
     * @throws Exception
     */
    public static void addOtherSignerToExistingSignature(String
        inFileName, IConfiguration config, String outFileName) throws Exception {

        InputStream existSign;

        if (config.useStream()) {
            existSign = new FileInputStream(inFileName);
        } // if
        else {
            // Читаем существующую подпись из файла.
            byte[] tmp = Array.readFile(inFileName);
            existSign = new ByteArrayInputStream(tmp);
        } // else

        addOtherSignerToExistingSignature(existSign, config, outFileName);
        existSign.close();
    }

    /**
     * Добавление одного подписанта в существующую подпись.
     *
     * @param existSign Подпись для добавления нового подписанта.
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения расширенной подписи.
     * @throws Exception
     */
    public static void addOtherSignerToExistingSignature(InputStream
        existSign, IConfiguration config, String outFileName) throws Exception {

        // 1. Декодирование и проверка исходной подписи с одним-двумя
        // подписантами.

        if (existSign instanceof ByteArrayInputStream) {
            if (existSign.markSupported()) {
                existSign.mark(0);
            } // if
        } // if

        // Прочитывает existSign.
        CAdESSignature existCadesSignature =
            VerifyExample.verifyCAdESSignature(existSign,
                config, VerifyExample.SignatureType.ST_MIXED);

        if (existSign instanceof ByteArrayInputStream) {
            existSign.reset();
        } // if

        // 2. Получение списка подписантов (1-2 шт.) из исходной
        // подписи.

        // Список всех подписантов в исходной подписи.
        Collection<SignerInformation> existSignerInfos =
            new ArrayList<SignerInformation>();

        for (CAdESSigner signer : existCadesSignature.getCAdESSignerInfos()) {
            existSignerInfos.add(signer.getSignerInfo());
        } // for

        // 3. Создаем новую подпись с одним подписантом, которого потом
        // добавим к исходным подписантам. Данные для подписи - те же, на
        // которых создавались исходные подписанты.

        CAdESSignature otherSignerCadesSignature = new CAdESSignature(config.detached());

        // Создаем нового подписанта CAdES-X Long Type 1.
        otherSignerCadesSignature.addSigner(config.getProviderName(),
            config.getDigestOid(),
            config.getPublicKeyOid(),
            config.getPrivateKey(),
            config.getChain(),
            CAdESType.CAdES_X_Long_Type_1,
            config.getTSAAddress(),
            false,
            null,
            null,
            config.getCRLs()
        );

        // Сохраняем подпись в файл или массив.
        OutputStream outSignatureStream = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        // Подписываем те же данные, что и подписанты из существующей подписи.
        otherSignerCadesSignature.open(outSignatureStream);
        InputStream dataStream = config.getDataStream();
        SignExample.cadesSignatureUpdate(otherSignerCadesSignature, dataStream); // хеш

        dataStream.close();
        otherSignerCadesSignature.close();
        outSignatureStream.close();

        // 4. Обновляем список из 1-2 исходных подписантов на новый с новым
        // подписантом.

        InputStream signatureStream = null;

        // Берем подпись из массива или файла.
        if (config.useStream()) {
            signatureStream = new FileInputStream(outFileName);
        } // if
        else if (outSignatureStream instanceof ByteArrayOutputStream) {
            byte[] tmp = ((ByteArrayOutputStream)outSignatureStream).toByteArray();
            signatureStream = new ByteArrayInputStream(tmp);
        } // else

        // Только декодируем подпись, чтобы получить первого подписанта.
        dataStream = config.detached() ? config.getDataStream() : null;
        CAdESSignature otherSignerCAdESSignatureParsed = new CAdESSignature(signatureStream, dataStream, null);

        if (dataStream != null) {
            dataStream.close();
        }
        if (signatureStream != null) {
            signatureStream.close();
        }

        CAdESSigner otherSigner = otherSignerCAdESSignatureParsed.getCAdESSignerInfo(0);

        // Добавляем нового подписанта в список подписантов.
        existSignerInfos.add(otherSigner.getSignerInfo());

        InputStream existCMSSignedDataStream;

        // Получаем подпись из файла или из массива.
        if (config.useStream()) {

            if (existSign instanceof FileInputStream) {

                FileInputStream tmp = (FileInputStream)existSign;
                tmp.getChannel().position(0);
                existCMSSignedDataStream = existSign;

            } // if
            else {
                throw new Exception("Invalid file stream");
            } // else

        } // if
        else {

            if (existSign instanceof ByteArrayInputStream) {
                existCMSSignedDataStream = existSign;
            } // if
            else {
                throw new Exception("Invalid byte stream");
            } // else

        } // else

        OutputStream newCMSSignedDataBuffer = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        // Обновляем исходную подпись c ее начальным списком из 1-2 подписантов
        // на тот же, но + новый подписант. Т.е. стало подписантов на одного больше.
        CAdESSignature.replaceSigners(existCMSSignedDataStream,
            new SignerInformationStore(existSignerInfos), newCMSSignedDataBuffer);

        existCMSSignedDataStream.close();
        newCMSSignedDataBuffer.close();

        InputStream newCMSSignedDataStream = null;

        // Чтение подписи из файла или массива.
        if (config.useStream()) {
            newCMSSignedDataStream = new FileInputStream(outFileName);
        } // if
        else if (newCMSSignedDataBuffer instanceof ByteArrayOutputStream) {

            byte[] newCMSSignedData = ((ByteArrayOutputStream)newCMSSignedDataBuffer).toByteArray();

            if (outFileName != null) {
                Array.writeFile(outFileName, newCMSSignedData);
            } // if

            newCMSSignedDataStream = new ByteArrayInputStream(newCMSSignedData);

        } // else

        // 5. Проверим новую подпись из 2-3 подписантов.

        // Подпись в тесте была совмещенная, потому данные равны null. Предположим, что
        // подписей несколько, тогда лучше указать тип null и положиться на самоопределение
        // типа подписи.
        CAdESSignature cadesSignature =
            VerifyExample.verifyCAdESSignature(newCMSSignedDataStream,
                config, VerifyExample.SignatureType.ST_MIXED);

        // Configuration.printSignatureInfo(cadesSignature);

        if (newCMSSignedDataStream != null) {
            newCMSSignedDataStream.close();
        }
        existSign.close();

    }

    /**
     * Добавление другого подписанта в CAdES подпись.
     *
     * @param container Описание используемого ключевого
     * контейнера другого подписанта.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void addOtherSignerExample(ISignatureContainer container, boolean useStream) {

        try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);

            // Подписант без дополнительных пользовательских аттрибутов.
            addOtherSignerToExistingSignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SimpleConfiguration.getTempFileName("addOtherSigner_"));

        } catch (Exception e) {
            Configuration.printCAdESException(e);
        }
    }

    /**
     * @param args
     * @deprecated
     */
    public static void main(String[] args) {
        JCPInit.initProviders(false);
        // addOtherSignerExample(new Container2001(), false);
    }

}
