/**
 * $RCSfileJCPEnvelopedCMSAsByteArrayExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 10:49 by afevma
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
import CAdES.configuration.container.*;
import CMS_samples.CMSSign;
import CMS_samples.CMSVerify;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Пример создания и расшифрования подписи Enveloped CMS из
 * Signed CMS с помощью провайдера JCP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPEnvelopedCMSAsByteArrayExample implements IEnvelopedData {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        main_group_exchange(true);
        main_group_signature(false);
    }

    /**
     * Выполнение группы заданий на ключах обмена.
     *
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_exchange(boolean transport) throws Exception {

        // System.out.println("*** Exchange group ***");

        envelope(
                null,
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(
                null,
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagma,
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagma,
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechik,
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechik,
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagmaMac,
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagmaMac,
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechikMac,
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechikMac,
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение группы заданий на ключах подписи.
     *
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_signature(boolean transport) throws Exception {

        // System.out.println("*** Signature group ***");

        envelope(
                null,
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(
                null,
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagma,
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagma,
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechik,
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechik,
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagmaMac,
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaMagmaMac,
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechikMac,
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(
                EncryptionKeyAlgorithm.ekaKuznechikMac,
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение набора примеров.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param signerCfg Конфигурация подписанта.
     * @param recipientCfg Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void envelope(EncryptionKeyAlgorithm alg,
        IConfiguration signerCfg, IConfiguration recipientCfg,
        boolean transport) throws Exception {

        // --- Зашифрование/расшифрование и проверка Signed CMS ---

        // Создание Signed CMS
        byte[] signedCMS = createCMS(signerCfg, DATA, null, null);

        // Зашифрование Signed CMS в Enveloped CMS и расшифрование снова
        byte[] decryptedCMS = JCPEnvelopedDataAsByteArrayExample
            .encryptDecrypt(alg, signedCMS, true, recipientCfg, transport);

        // Проверка расшифрованной Signed CMS
        verifyCMS(signerCfg.getProviderName(), signerCfg, decryptedCMS);

    }

    /**
     * Создание подписи Signed CMS в массив с возможностью записи в файл.
     *
     * @param signerConfig Конфигурация подписанта.
     * @param data Данные для подписи.
     * @param signedCmsFilePath Путь к файлу signedCmsFile.
     * @param signedCmsFile Файл для сохранения Signed CMS.
     * Может быть null.
     * @return Signed CMS.
     * @throws Exception
     */
    public static byte[] createCMS(IConfiguration signerConfig,
        byte[] data, String signedCmsFilePath, String signedCmsFile)
        throws Exception {

        // attached
        byte[] signedCms = CMSSign.createCMSEx(
            data,
            new PrivateKey[]{signerConfig.getPrivateKey()},
            new Certificate[]{signerConfig.getCertificate()},
            null,
            false,
            signerConfig.getProviderName()
        );

        if (signedCmsFilePath != null && signedCmsFile != null) {
            String file = new File(signedCmsFilePath, signedCmsFile).getAbsolutePath();
            Array.writeFile(file, signedCms);
        } // if

        return signedCms;
    }

    /**
     * Проверка подписи Signed CMS из массива.
     *
     * @param provider Провайдер подписи.
     * @param signerConfig Конфигурация подписанта.
     * @param signedCMS Подпись Signed CMS.
     * @throws Exception
     */
    public static void verifyCMS(String provider, IConfiguration
        signerConfig, byte[] signedCMS) throws Exception {

        CMSVerify.CMSVerifyEx(
            signedCMS,
            null,
            null,
            provider
        );

    }

}
