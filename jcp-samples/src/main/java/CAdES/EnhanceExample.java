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

import org.bouncycastle.tsp.TimeStampToken;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESSignerXLT1;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Пример усовершенствования подписи CAdES-BES до
 * CAdES-X Long Type 1.
 * 
 * 17/04/2012
 *
 */
public class EnhanceExample {

    /**
     * Усовершенствование CAdES-BES подписи до CAdES-X Long Type 1.
     *
     * @param inFileName Исходный файл с CAdES-BES подписью.
     * @param config Конфигурация подписи.
     * @param outFileName Файл с усовершенствованной подписью.
     * @throws Exception
     */
    public static void enhanceSignature(String inFileName,
        IConfiguration config, String outFileName) throws Exception {

        InputStream cadesCms;

        if (config.useStream()) {
            cadesCms = new FileInputStream(inFileName);
        } // if
        else {
            // Читаем подпись из файла.
            byte[] tmp = Array.readFile(inFileName);
            cadesCms = new ByteArrayInputStream(tmp);
        } // else

        enhanceSignature(cadesCms, config, outFileName);
        cadesCms.close();

    }

    /**
     * Усовершенствование CAdES-BES подписи до CAdES-X Long Type 1.
     *
     * @param cadesCms Подпись для усовершенствования.
     * @param config Конфигурация подписи.
     * @param outFileName Файл с усовершенствованной подписью.
     * @throws Exception
     */
    public static void enhanceSignature(InputStream cadesCms,
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
        CAdESSigner[] cAdESSigners = cadesSignature.getCAdESSignerInfos();

        for (CAdESSigner signer : cAdESSigners) {
            srcSignerInfos.add(signer.getSignerInfo());
        } // for

        // 2. Усовершенствование подписи.

        InputStream srcSignedDataStream;

        // Получаем подпись из файла или из массива.
        if (config.useStream()) {

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

        // Получаем только первую CAdES-BES подпись, усовершенствуем ее.
        // Остальных не трогаем.
        CAdESSigner srcSigner = cadesSignature.getCAdESSignerInfo(0);

        // Исключаем ее из исходного списка, т.к. ее место займет усовершенствованная
        // подпись.
        srcSignerInfos.remove(srcSigner.getSignerInfo());

        // Усовершенствуем CAdES-BES до CAdES-X Long Type 1.
        srcSigner = srcSigner.enhance(config.getProviderName(),
            config.getDigestOid(),
            new LinkedList<X509Certificate>(config.getChain()),
            config.getTSAAddress(),
            CAdESType.CAdES_X_Long_Type_1,
            config.getUnsignedAttributes());

        if (srcSigner instanceof CAdESSignerXLT1) {

            // System.out.println("CAdES signer: X Long Type 1");
            CAdESSignerXLT1 cAdESSignerXLT1 = (CAdESSignerXLT1) srcSigner;

            if (cAdESSignerXLT1.getEarliestValidCAdESCTimeStampToken() == null) {
                throw new Exception("Invalid cAdESC-timestamp value");
            }

            if (cAdESSignerXLT1.getEarliestValidSignatureTimeStampToken() == null) {
                throw new Exception("Invalid signing-timestamp value");
            }

            List<TimeStampToken> cadesCTimeStampTokens =
                cAdESSignerXLT1.getCAdESCTimestampTokens();

            if (cadesCTimeStampTokens == null || cadesCTimeStampTokens.size() != 1) {
                throw new Exception("Invalid cAdESC-timestamp count");
            }

            List<TimeStampToken> signatureTimeStampTokens =
                cAdESSignerXLT1.getSignatureTimestampTokens();

            if (signatureTimeStampTokens == null || signatureTimeStampTokens.size() != 1) {
                throw new Exception("Invalid signing-timestamp count");
            }

        } // if
        else {
            throw new Exception("Enhancement failed!");
        } // else

        // Подписант усовершенствованной подписи.
        SignerInformation enhSigner = srcSigner.getSignerInfo();

        // Добавляем его в исходный список подписантов.
        srcSignerInfos.add(enhSigner);

        // Список подписантов.
        SignerInformationStore dstSignerInfoStore =
            new SignerInformationStore(srcSignerInfos);

        OutputStream newCMSSignedDataBuffer = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        // Обновляем исходную подпись c ее начальным списком подписантов на тот же,
        // но с первым подписантом с усовершенствованной подписью.
        CAdESSignature.replaceSigners(srcSignedDataStream, dstSignerInfoStore, newCMSSignedDataBuffer);
        srcSignedDataStream.close();
        newCMSSignedDataBuffer.close();

        InputStream dstSignedData = null;

        // Чтение подписи из файла или массива.
        if (config.useStream()) {
            dstSignedData = new FileInputStream(outFileName);
        } // if
        else if (newCMSSignedDataBuffer instanceof ByteArrayOutputStream) {

            byte[] newCMSSignedData = ((ByteArrayOutputStream)newCMSSignedDataBuffer).toByteArray();

            if (outFileName != null) {
                Array.writeFile(outFileName, newCMSSignedData);
            } // if

            dstSignedData = new ByteArrayInputStream(newCMSSignedData);

        } // else

        // 3. Проверка усовершенствованной подписи.

        cadesSignature =
            VerifyExample.verifyCAdESSignature(dstSignedData,
                config, VerifyExample.SignatureType.ST_CADES_X_LONG_TYPE_1);

        // Configuration.printSignatureInfo(cadesSignature);
        cadesCms.close();
        if (dstSignedData != null) {
            dstSignedData.close();
        }

    }

    /**
     * Усовершенствование CAdES подписи.
     *
     * @param container Описание используемого ключевого
     * контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void enhanceSignatureExample(ISignatureContainer container, boolean useStream) {

		try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);

            // Усовершенствуем без дополнительных аттрибутов.
			enhanceSignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SimpleConfiguration.getTempFileName("enhanced_"));

            // Усовершенствуем с дополнительными неподписываемыми аттрибутами.
            configAttached.setUnsignedAttributes(Configuration.getSomeUnsignedAttributes(true));
            enhanceSignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SimpleConfiguration.getTempFileName("unsignedAttrs_enhanced_"));
		
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
        // EnhanceExample.enhanceSignatureExample(new Container2001(), false);
    }

}
