/**
 * $RCSfileEnvelopedCMSAsStreamExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 9:50 by afevma
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

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

/**
 * Пример создания подписи формата Enveloped CMS из потока данных файла
 * (Signed CMS) на разных алгоритмах.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class EnvelopedCMSAsStreamExample implements IEnvelopedData {

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

        //
        // envelope(tmpDir, ".2001.cms",
        //     new SimpleConfiguration(new EnvContainer2001(), false),
        //         new SimpleConfiguration(new ServerContainer2001(), false),
        //             transport);
        //

        envelope(tmpDir, ".2012_256.cms",
                new SimpleConfiguration(new EnvContainer2012_256(), false),
                new SimpleConfiguration(new ServerContainer2012_256(), false),
                transport);

        envelope(tmpDir, ".2012_512.cms",
                new SimpleConfiguration(new EnvContainer2012_512(), false),
                new SimpleConfiguration(new ServerContainer2012_512(), false),
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
    public static void main_group_signature(String tmpDir, boolean transport) throws Exception {

        // System.out.println("*** Signature group ***");

        //
        // envelope(tmpDir, ".2001.cms",
        //     new SimpleConfiguration(new Container2001(), false),
        //         new SimpleConfiguration(new ServerSigContainer2001(), false),
        //             transport);
        //

        envelope(tmpDir, ".2012_256.cms",
                new SimpleConfiguration(new ClientSigContainer2012_256(), false),
                new SimpleConfiguration(new ServerSigContainer2012_256(), false),
                transport);

        envelope(tmpDir, ".2012_512.cms",
                new SimpleConfiguration(new ClientSigContainer2012_512(), false),
                new SimpleConfiguration(new ServerSigContainer2012_512(), false),
                transport);

    }

    /**
     * Выполнение набора тестов.
     *
     * @param tempPath Папка для сохранения результатов.
     * @param fileExt Расширение создаваемого файла
     * @param signerConfig Конфигурация подписанта.
     * @param recipientConfig Конфигурация получателя.
     * @throws Exception
     */
    public static void envelope(String tempPath, String fileExt,
        IConfiguration signerConfig, IConfiguration recipientConfig,
        boolean transport) throws Exception {

        // --- Зашифрование/расшифрование и проверка Signed CMS ---

        String signedCmsFile = SIGNED_CMS_FILE + fileExt;

        // Создание Signed CMS
        EnvelopedCMSAsByteArrayExample.createCMS(
            signerConfig, DATA, tempPath, signedCmsFile);

        // Зашифрование Signed CMS в Enveloped CMS и расшифрование снова (в файл)
        String decryptedCMSFile = EnvelopedDataAsStreamExample
            .encryptDecrypt(recipientConfig, tempPath, signedCmsFile,
                tempPath, true, transport);

        // Проверка расшифрованной Signed CMS
        byte[] signedCms = Array.readFile(decryptedCMSFile);

        EnvelopedCMSAsByteArrayExample.verifyCMS(
            signerConfig.getProviderName(),
            signerConfig, signedCms
        );

    }

}
