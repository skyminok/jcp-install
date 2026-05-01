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
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.util.CollectionStore;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.*;

/**
 * Пример создания CAdES подписи.
 *
 * 17/04/2012
 *
 */
public class SignExample {

    static {
        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    /**
     * Хеширование потока данных.
     *
     * @param cAdESSignature Объект класса CAdESSignature.
     * @param dataStream Поток данных.
     * @throws Exception
     */
    public static void cadesSignatureUpdate(CAdESSignature cAdESSignature,
        InputStream dataStream) throws Exception {

        final int buffer_size = 1024*1024;
        byte[] buffer = new byte[buffer_size];
        int read;

        while ( (read = dataStream.read(buffer, 0, buffer_size)) > 0 ) {
            cAdESSignature.update(buffer, 0, read);
        } // while

    }

    /**
     * Создание CAdES-подписи с двумя подписантами: CAdES-BES и
     * CAdES-X Long Type 1.
     *
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения подписи.
     * @throws Exception
     */
    public static void createMixedSignatureWith2Signers(IConfiguration config, String outFileName) throws Exception {
        InputStream signatureStream = createMixedSignatureWith2SignersAsStream(config, outFileName);
        signatureStream.close();
    }

    /**
     * Создание CAdES-подписи с двумя подписантами: CAdES-BES и
     * CAdES-X Long Type 1.
     *
     * @param config Конфигурация подписи.
     * @param outFileName Файл для сохранения подписи.
     * @return подпись.
     * @throws Exception
     */
    public static InputStream createMixedSignatureWith2SignersAsStream(IConfiguration
        config, String outFileName) throws Exception {

        CAdESSignature cadesSignature = new CAdESSignature(config.detached());

        cadesSignature.setCertificateStore(config.getCertificateStore());
        cadesSignature.setCRLStore(config.getCRLStore());

        // Создаем подписанта CAdES-BES.
        cadesSignature.addSigner(config.getProviderName(),
            config.getDigestOid(),
            config.getPublicKeyOid(),
            config.getPrivateKey(),
            config.getChain(),
            CAdESType.CAdES_BES,
            null,
            false,
            config.getSignedAttributes(),
            config.getUnsignedAttributes(),
            config.getCRLs());

        // Создаем подписанта CAdES-X Long Type 1.
        cadesSignature.addSigner(config.getProviderName(),
            config.getDigestOid(),
            config.getPublicKeyOid(),
            config.getPrivateKey(),
            config.getChain(),
            CAdESType.CAdES_X_Long_Type_1,
            config.getTSAAddress(),
            false,
            null,
            null,
            config.getCRLs());

        // Сохраним подпись либо в файл, либо в массив.
        OutputStream outSignatureStream = config.useStream()
            ? new FileOutputStream(outFileName) : new ByteArrayOutputStream();

        cadesSignature.open(outSignatureStream);
        InputStream dataStream = config.getDataStream();
        cadesSignatureUpdate(cadesSignature, dataStream); // хеш

        // Завершаем создание подписи с двумя подписантами.
        cadesSignature.close();
        dataStream.close();
        outSignatureStream.close();

        CAdESSigner[] signers = cadesSignature.getCAdESSignerInfos();
        for (int i = 0; i < signers.length; i++) {

            CAdESSigner signer = signers[i];

            // Только ему могут подаваться атрибуты (см. выше).
            if (signer.getSignatureType().equals(CAdESType.CAdES_BES)) {

                AttributeTable cdsAttrs = signer.getSignerSignedAttributes();
                if (config.getSignedAttributes() != null) {
                    if (config.getSignedAttributes().size() != cdsAttrs.size()) {
                        throw new Exception("Invalid count of signed attributes in " +
                            "CAdES signature # " + i);
                    } // if
                } // if
                else {
                    if (cdsAttrs != null) {
                        throw new Exception("Count of signed attributes must be null " +
                            "in CAdES signature # " + i);
                    } // if
                } // else

                cdsAttrs = signer.getSignerUnsignedAttributes();
                if (config.getUnsignedAttributes() != null) {
                    if (config.getUnsignedAttributes().size() != cdsAttrs.size()) {
                        throw new Exception("Invalid count of unsigned attributes in " +
                            "CAdES signature # " + i);
                    } // if
                } // if
                else {
                    if (cdsAttrs != null) {
                        throw new Exception("Count of unsigned attributes must be null " +
                            "in CAdES signature # " + i);
                    } // if
                } // else

            } // if

        } // for

        InputStream signatureStream;

        // Если это массив, сохраним и снова прочтем.
        if (!config.useStream() && outSignatureStream instanceof ByteArrayOutputStream) {

            byte[] cadesCms = ((ByteArrayOutputStream)outSignatureStream).toByteArray();

            if (outFileName != null) {
                Array.writeFile(outFileName, cadesCms);
            } // if

            // Подпись.
            signatureStream = new ByteArrayInputStream(cadesCms);

        } // if
        else {
            // Читаем подпись.
            signatureStream = new FileInputStream(outFileName);
        } // else

        return signatureStream;
    }

	/**
    * Создание CAdES подписи.
    *
    * @param container Описание используемого ключевого контейнера.
    * @param useStream True, если следует использовать поток данных и подписи.
    */
    public static void signExample(ISignatureContainer container, boolean useStream) {

		try {

            IConfiguration configAttached = new SimpleConfiguration(container, useStream);
            IConfiguration configDetached = new SimpleConfiguration(container, true, useStream);

            // Подпись без дополнительных пользовательских аттрибутов.
            createMixedSignatureWith2Signers(configAttached,
                SimpleConfiguration.getTempFileName(null));

            // Подпись с дополнительными пользовательскими подписываемыми
            // аттрибутами.
            configDetached.setSignedAttributes(Configuration.getSomeSignedAttributes(true, true));
            createMixedSignatureWith2Signers(configDetached,
                SimpleConfiguration.getTempFileName("signedAttrs_det_"));

            // Подпись с дополнительными пользовательскими неподписываемыми
            // аттрибутами, а также сертификатами, вложенными в SignedData.
            configAttached.setUnsignedAttributes(Configuration.getSomeUnsignedAttributes(true));
            configAttached.setCertificateStore(new CollectionStore(configAttached.getChainHolder()));
            createMixedSignatureWith2Signers(configAttached,
                SimpleConfiguration.getTempFileName("unsignedAttrs_certs_"));

            // Подпись с дополнительными пользовательскими подписываемыми
            // и неподписываемыми аттрибутами, а также сертификатами м СОС,
            // вложенными в SignedData.
            configDetached.setSignedAttributes(Configuration.getSomeUnsignedAttributes(true));
            configDetached.setCertificateStore(new CollectionStore(configDetached.getChainHolder()));
            configDetached.setCRLStore(new CollectionStore(configDetached.getCRLsHolder()));
            createMixedSignatureWith2Signers(configDetached,
                SimpleConfiguration.getTempFileName("allAttrs_det_certs_crls_"));

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
        // SignExample.signExample(new Container2001(), false);
    }
}
