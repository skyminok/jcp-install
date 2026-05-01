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
 * Пример заверения подписи CAdES-BES двумя подписями
 * CAdES-X Long Type 1.
 * 
 * 17/04/2012
 *
 */
public class CountersignatureExample {

    /**
     * Добавление двух заверителей в прочитанную из файда
     * CAdES-подпись.
     *
     * @param inFileName Файл с подписью для заверения.
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения заверенной подписи.
     * @throws Exception
     */
    public static void expandBy2Countersignature(String
        inFileName, IConfiguration config, String outFileName)
        throws Exception {

        InputStream cadesCms;

        if (config.useStream()) {
            cadesCms = new FileInputStream(inFileName);
        } // if
        else {
            // Читаем подпись из файла.
            byte[] tmp = Array.readFile(inFileName);
            cadesCms = new ByteArrayInputStream(tmp);
        } // else

        expandBy2Countersignature(cadesCms, config, outFileName);
        cadesCms.close();

    }

    /**
     * Добавление двух заверителей в прочитанную из файда
     * CAdES-подпись.
     *
     * @param cadesCms Подпись для заверения.
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения заверенной подписи.
     * @throws Exception
     */
    public static void expandBy2Countersignature(InputStream cadesCms,
        IConfiguration config, String outFileName) throws Exception {

        // 1. Загрузка и "проверка" подписи.

        if (cadesCms instanceof ByteArrayInputStream) {
            if (cadesCms.markSupported()) {
                cadesCms.mark(0);
            } // if
        } // if

        // Прочитывает cadesCms.
        CAdESSignature cadesSignature = VerifyExample.verifyCAdESSignature(cadesCms,
            config, VerifyExample.SignatureType.ST_MIXED);

        if (cadesCms instanceof ByteArrayInputStream) {
            cadesCms.reset();
        } // if

        // Список всех подписантов в исходной подписи.
        Collection<SignerInformation> srcSignerInfos = new ArrayList<SignerInformation>();

        for (CAdESSigner signer : cadesSignature.getCAdESSignerInfos()) {
            srcSignerInfos.add(signer.getSignerInfo());
        } // if

        // 2. Заверение подписи.

        // Получаем только первую подпись, которую заверим. Остальных не трогаем.
        CAdESSigner srcSigner = cadesSignature.getCAdESSignerInfo(0);

        // Исключаем эту подпись из исходного списка, т.к. ее место займет подпись с
        // заверителями.
        srcSignerInfos.remove(srcSigner.getSignerInfo());

        // Создаем заверяющую подпись.
        CAdESSignature counterSignature = new CAdESSignature();

        // Добавляем заверяющего подписанта. Последний параметр true, что определяет
        // тип подписанта (заверяющий).
        counterSignature.addSigner(config.getProviderName(),
            config.getDigestOid(),
            config.getPublicKeyOid(),
            config.getPrivateKey(),
            config.getChain(),
            CAdESType.CAdES_X_Long_Type_1,
            config.getTSAAddress(), true,
            config.getSignedAttributes(),
            config.getUnsignedAttributes(),
            config.getCRLs());

        // Сохраняем подпись в файл или массив.
        OutputStream outSignatureStream = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        // Подписываем данные заверяемой подписи.
        counterSignature.open(outSignatureStream);
        counterSignature.update(srcSigner.getSignerInfo().getSignature());
        counterSignature.close();
        outSignatureStream.close();

        InputStream signatureStream = null;

        // Берем подпись из массива или файла.
        if (config.useStream()) {
            signatureStream = new FileInputStream(outFileName);
        } // if
        else if (outSignatureStream instanceof ByteArrayOutputStream) {
            byte[] tmp = ((ByteArrayOutputStream)outSignatureStream).toByteArray();
            signatureStream = new ByteArrayInputStream(tmp);
        } // else

        // Только декодируем подпись, чтобы получить первого заверителя.
        InputStream dataStream = config.detached() ? config.getDataStream() : null;
        CAdESSignature counterSignatureParsed = new CAdESSignature(signatureStream, dataStream, null);

        if (dataStream != null) {
            dataStream.close();
        }
        if (signatureStream != null) {
            signatureStream.close();
        }

        // Получаем единственного заверителя.
        CAdESSigner counterSigner = counterSignatureParsed.getCAdESSignerInfo(0);

        // Добавляем заверителя, например, 2 раза к исходной подписи.
        srcSigner.addCountersigner(counterSigner.getSignerInfo());
        srcSigner.addCountersigner(counterSigner.getSignerInfo());

        // Получаем заверенную подпись.
        SignerInformation newSigner = srcSigner.getSignerInfo();

        // Добавляем ее в исходный список подписей.
        srcSignerInfos.add(newSigner);

        InputStream srcCMSSignedDataStream;

        // Получаем подпись из файла или из массива.
        if (config.useStream()) {

            if (cadesCms instanceof FileInputStream) {

                FileInputStream tmp = (FileInputStream)cadesCms;
                tmp.getChannel().position(0);
                srcCMSSignedDataStream = cadesCms;

            } // if
            else {
                throw new Exception("Invalid file stream");
            } // else

        } // if
        else {

            if (cadesCms instanceof ByteArrayInputStream) {
                srcCMSSignedDataStream = cadesCms;
            } // if
            else {
                throw new Exception("Invalid byte stream");
            } // else

        } // else

        OutputStream newCMSSignedDataBuffer = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        // Обновляем исходную подпись с ее начальным списком подписантов на тот же,
        // но с первым подписантом с заверенной подписью.
        CAdESSignature.replaceSigners(srcCMSSignedDataStream,
            new SignerInformationStore(srcSignerInfos), newCMSSignedDataBuffer);

        srcCMSSignedDataStream.close();
        newCMSSignedDataBuffer.close();

        InputStream dstCMSSignedData = null;

        // Чтение подписи из файла или массива.
        if (config.useStream()) {
            dstCMSSignedData = new FileInputStream(outFileName);
        } // if
        else if (newCMSSignedDataBuffer instanceof ByteArrayOutputStream) {

            byte[] newCMSSignedData = ((ByteArrayOutputStream)newCMSSignedDataBuffer).toByteArray();

            if (outFileName != null) {
                Array.writeFile(outFileName, newCMSSignedData);
            } // if

            dstCMSSignedData = new ByteArrayInputStream(newCMSSignedData);

        } // else

        // 3. Проверка заверенной и заверяющих подписей.

        cadesSignature =
            VerifyExample.verifyCAdESSignature(dstCMSSignedData,
                config, VerifyExample.SignatureType.ST_MIXED);

        // Configuration.printSignatureInfo(cadesSignature);
        if (dstCMSSignedData != null) {
            dstCMSSignedData.close();
        }
        cadesCms.close();

    }

    /**
     * Заверение CAdES подписи.
     *
     * @param container Описание используемого ключевого
     * контейнера заверителя.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void addCountersignatureExample(ISignatureContainer container,
        boolean useStream) {

		try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);

            // Добавляем заверителей без дополнительных аттрибутов.
            expandBy2Countersignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SimpleConfiguration.getTempFileName("countersignature_"));

            // Добавляем заверителей с дополнительными аттрибутами.
            configAttached.setSignedAttributes(Configuration.getSomeSignedAttributes(true, true));
            configAttached.setUnsignedAttributes(Configuration.getSomeUnsignedAttributes(true));
            expandBy2Countersignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SimpleConfiguration.getTempFileName("allAttrs_countersignature_"));

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
        // CountersignatureExample.addCountersignatureExample(new Container2001(), false);
    }

}
