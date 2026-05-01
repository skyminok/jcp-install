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

import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESSignerXLT1;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Пример проверки CAdES подписи. Проверяет подписи,
 * созданные в примере SignExample.
 * 
 * 17/04/2012
 *
 */
public class VerifyExample {

    /**
     * Типы попдисей для проверки. Например, если задан
     * ST_CADES_X_LONG_TYPE_1, то подавать цепочку
     * сертификатов необязательно.
     * ST_CADES_BES - проверяем подпись(и) этого типа. Необходима цепочка
     * сертификатов.
     * ST_CADES_X_LONG_TYPE_1 - проверяем подпись(и) данного типа. Цепочка
     * сертификатов необязательна.
     * ST_MIXED - подпись состоит из нескольких подписей разных типов. Лучше
     * подать цепочку сертификатов.
     */
    public enum SignatureType {ST_CADES_BES, ST_CADES_X_LONG_TYPE_1, ST_CADES_A, ST_MIXED};

    /**
     * Проверка CAdES-подписи, прочитанной из файла.
     *
     * @param inFileName Файл с подписью.
     * @param config Конфигурация подписи.
     * @param signatureType Тип подписи при проверке.
     * @throws Exception
     */
    public static CAdESSignature verifyCAdESSignature(String inFileName,
        IConfiguration config, SignatureType signatureType) throws Exception {

        InputStream cadesCmsStream;

        if (config.useStream()) {
            cadesCmsStream = new FileInputStream(inFileName);
        } // if
        else {
            // Читаем подпись из файла.
            byte[] tmp = Array.readFile(inFileName);
            cadesCmsStream = new ByteArrayInputStream(tmp);
        } // else

        CAdESSignature cAdESSignature = verifyCAdESSignature(
            cadesCmsStream, config, signatureType);

        cadesCmsStream.close();
        return cAdESSignature;

    }

    /**
     * Проверка CAdES-подписи.
     *
     * @param cadesCmsStream Подпись для проверки.
     * @param config Конфигурация подписи.
     * @param signatureType Тип подписи при проверке.
     * @throws Exception
     */
    public static CAdESSignature verifyCAdESSignature(InputStream cadesCmsStream,
        IConfiguration config, SignatureType signatureType) throws Exception {

        InputStream dataStream = config.getDataStream();

        // Подпись в тесте была совмещенная, потому данные равны null.
        // Предположим, что подписей несколько, тогда лучше указать
        // тип null и положиться на самоопределение типа подписи.
        CAdESSignature cadesSignature = new CAdESSignature(cadesCmsStream,
            config.detached() ? dataStream : null, null);

        // Если известно, что в SignedData подписи есть сертификаты (т.е.
        // config.getCertificateStore() != null), то не станем передавать
        // сертификаты извне. Аналогично с CRL.
        cadesSignature.verify(
            signatureType.equals(SignatureType.ST_CADES_X_LONG_TYPE_1) ||
            signatureType.equals(SignatureType.ST_CADES_A)
            ? Collections.<X509Certificate>emptySet()
            : ( config.getCertificateStore() != null ? null : new HashSet<X509Certificate>(config.getChain()) ),
              ( config.getCRLStore()         != null ? null : config.getCRLs() )
        );

        dataStream.close();
        // Configuration.printSignatureInfo(cadesSignature);

        CAdESSigner[] signers = cadesSignature.getCAdESSignerInfos();
        // printCAdESSignersInfo(signers);

        return cadesSignature;
    }

    /**
     * Вывод информации о подписантах.
     *
     * @param signers Список подписантов.
     * @throws Exception
     */
    public static void printCAdESSignersInfo(CAdESSigner[] signers)
        throws Exception {

        for (int i = 0; i < signers.length; i++) {

            CAdESSigner signer = signers[i];
            if (signer instanceof CAdESSignerXLT1) {

                CAdESSignerXLT1 cAdESSignerXLT1 = (CAdESSignerXLT1) signer;
                System.out.println("Check timestamps #" + i + ":");

                TimeStampToken signTimestamp = cAdESSignerXLT1.getEarliestValidSignatureTimeStampToken();
                if (signTimestamp == null) {
                    throw new Exception("Signature timestamp is null");
                } // if

                TimeStampToken cdsCTimestamp = cAdESSignerXLT1.getEarliestValidCAdESCTimeStampToken();
                if (cdsCTimestamp == null) {
                    throw new Exception("CAdES-C timestamp is null");
                } // if

                List<TimeStampToken> signatureTimeStampTokens = cAdESSignerXLT1.getSignatureTimestampTokens();
                if (signatureTimeStampTokens == null) {
                    throw new Exception("Signature timestamp list is null");
                } // if

                int sz = signatureTimeStampTokens.size();
                if (sz != 1) {
                    throw new Exception("It is weird... Size of signature timestamp " +
                        "list is more than 1 (" + sz + ")");
                } // if

                List<TimeStampToken> cadesCTimeStampTokens = cAdESSignerXLT1.getCAdESCTimestampTokens();
                if (cadesCTimeStampTokens == null) {
                    throw new Exception("CAdES-C timestamp list is null");
                } // if

                sz = cadesCTimeStampTokens.size();
                if (sz != 1) {
                    throw new Exception("It is weird... Size of CAdES-C timestamp " +
                        "list is more than 1 (" + sz + ")");
                } // if

            } // if

        } // for

    }

    /**
     * Проверка CAdES подписи.
     *
     * @param container Описание используемого ключевого контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void verifyExample(ISignatureContainer container, boolean useStream) {

		try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);
            IConfiguration configDetached = new SimpleConfiguration(container, true, useStream);

            // Проверяем подпись без дополнительных пользовательских аттрибутов.
			verifyCAdESSignature(SimpleConfiguration.getTempFileName(null),
                configAttached, SignatureType.ST_MIXED);

            // Проверяем подпись с дополнительными пользовательскими подписываемыми
            // аттрибутами.
            verifyCAdESSignature(SimpleConfiguration.getTempFileName("signedAttrs_det_"),
                configDetached, SignatureType.ST_MIXED);

            // Проверяем подпись с дополнительными пользовательскими неподписываемыми
            // аттрибутами, а также сертификатами, вложенными в SignedData.
            configAttached.setCertificateStore(new CollectionStore(configAttached.getChainHolder()));
            verifyCAdESSignature(SimpleConfiguration.getTempFileName("unsignedAttrs_certs_"),
                configAttached, SignatureType.ST_MIXED);

            // Проверяем подпись с дополнительными пользовательскими подписываемыми
            // и неподписываемыми аттрибутами, а также сертификатами м СОС, вложенными
            // в SignedData.
            configDetached.setCertificateStore(new CollectionStore(configDetached.getChainHolder()));
            configDetached.setCRLStore(new CollectionStore(configDetached.getCRLsHolder()));
            verifyCAdESSignature(SimpleConfiguration.getTempFileName("allAttrs_det_certs_crls_"),
                configDetached, SignatureType.ST_MIXED);

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
        // VerifyExample.verifyExample(new Container2001(), false);
    }
}
