/**
 * $RCSfileJCPEnvelopedCMSAsStreamExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 11:01 by afevma
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
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

/**
 * Пример создания и расшифрования подписи Enveloped CMS из
 * Signed CMS в потоке с помощью провайдера JCP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPEnvelopedCMSAsStreamExample implements IEnvelopedData {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        main_group_exchange(TEMP_DIR, true);
        main_group_signature(TEMP_DIR, false);
    }

    /**
     * Выполнение группы заданий на ключах обмена.
     *
     * @param tmpDir Папка для результатов.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_exchange(String tmpDir, boolean transport) throws Exception {

        // System.out.println("*** Exchange group ***");

        envelope(null, tmpDir, ".2012_256.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(null, tmpDir, ".2012_512.cms",
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, tmpDir, ".2012_256.mag.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, tmpDir, ".2012_512.mag.cms",
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, tmpDir, ".2012_256.kuz.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, tmpDir, ".2012_512.kuz.cms",
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, tmpDir, ".2012_256.mag_m.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, tmpDir, ".2012_512.mag_m.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, tmpDir, ".2012_256.kuz_m.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, tmpDir, ".2012_512.kuz_m.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);


    }

    /**
     * Выполнение группы заданий на ключах подписи.
     *
     * @param tmpDir Папка для результатов.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_signature(String tmpDir,
        boolean transport) throws Exception {

        // System.out.println("*** Signature group ***");

        envelope(null, tmpDir, ".2012_256.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(null, tmpDir, ".2012_512.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, tmpDir, ".2012_256.mag.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, tmpDir, ".2012_512.mag.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, tmpDir, ".2012_256.kuz.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, tmpDir, ".2012_512.kuz.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, tmpDir, ".2012_256.mag_m.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, tmpDir, ".2012_512.mag_m.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, tmpDir, ".2012_256.kuz_m.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, tmpDir, ".2012_512.kuz_m.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение набора тестов.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param tempPath Папка для сохранения результатов.
     * @param fileExt Расширение создаваемого файла
     * @param signerConfig Конфигурация подписанта.
     * @param recipientConfig Конфигурация получателя.
     * @throws Exception
     */
    public static void envelope(EncryptionKeyAlgorithm alg, String tempPath,
        String fileExt, IConfiguration signerConfig, IConfiguration recipientConfig,
        boolean transport) throws Exception {

        // --- Зашифрование/расшифрование и проверка Signed CMS ---

        String signedCmsFile = SIGNED_CMS_FILE + fileExt;

        // Создание Signed CMS
        EnvelopedCMSAsByteArrayExample.createCMS(
            signerConfig, DATA, tempPath, signedCmsFile);

        // Зашифрование Signed CMS в Enveloped CMS и расшифрование снова (в файл)
        String decryptedCMSFile = JCPEnvelopedDataAsStreamExample
            .encryptDecrypt(alg, recipientConfig, tempPath, signedCmsFile,
                    tempPath, true, transport);

        // Проверка расшифрованной Signed CMS
        byte[] signedCms = Array.readFile(decryptedCMSFile);

        EnvelopedCMSAsByteArrayExample.verifyCMS(
            signerConfig.getProviderName(), signerConfig, signedCms);

    }

    /**
     * Выполнение набора тестов.
     *
     * @param recipientConfig Конфигурация получателя.
     * @throws Exception
     */
    public static void decrypt(String enveloped, String decrypted, IConfiguration recipientConfig) throws Exception {

        // Зашифрование Signed CMS в Enveloped CMS и расшифрование снова (в файл)
        JCPEnvelopedDataAsStreamExample
                .decryptAsStream(recipientConfig, enveloped,
                        decrypted);


    }

}
