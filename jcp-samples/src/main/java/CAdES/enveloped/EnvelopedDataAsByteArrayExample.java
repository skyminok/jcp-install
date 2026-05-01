/**
 * $RCSfileSignEvelopedCMSExample.java,v $
 * version $Revision: 36379 $
 * created 23.07.2014 16:34 by afevma
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

import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Пример создания подписи формата Enveloped CMS из массива данных,
 * составленного из строки (plain text) на разных алгоритмах.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class EnvelopedDataAsByteArrayExample implements IEnvelopedData {

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
     * Используется key_transport.
     *
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_exchange(boolean transport) throws Exception {

        // System.out.println("*** Exchange group ***");

        //
        // envelope(new SimpleConfiguration(new ServerContainer2001(), false), transport);
        //
        envelope(new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

    }

    /**
     * Выполнение группы заданий на ключах подписи.
     * Используется key_agreement.
     *
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void main_group_signature(boolean transport) throws Exception {

        // System.out.println("*** Signature group ***");

        //
        // envelope(new SimpleConfiguration(new ServerSigContainer2001(), false), transport);
        //

        envelope(new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);
    }

    /**
     * Выполнение набора примеров.
     *
     * @param recipientConfig Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void envelope(IConfiguration recipientConfig,
        boolean transport) throws Exception {
        encryptDecrypt(DATA, false, recipientConfig, transport);
    }

    /**
     * Пример зашифрования данных в Enveloped CMS и расшифрования.
     *
     * @param data Данные для зашифрования.
     * @param isCms True, если data - это CMS.
     * @param recipientConfig Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @return расшифрованное сообщение.
     * @throws Exception
     */
    public static byte[] encryptDecrypt(byte[] data, boolean isCms,
        IConfiguration recipientConfig, boolean transport) throws Exception {

        byte[] enveloped = encryptAsByteArray(recipientConfig, data, transport);
        // System.out.println("ENV = " + (new Encoder()).encode(enveloped));

        byte[] decrypted = decryptAsByteArray(enveloped, recipientConfig);
        if (!Array.compare(decrypted, data)) {
            throw new Exception("Decryption failed, source data and decrypted data are not equal");
        } // if

        if (!isCms) {
            // System.out.println("Data: " + new String(data));
            // System.out.println("Decrypted byte data: " + new String(decrypted));
        } // if

        return decrypted;
    }

    /**
     * Зашифрование данных в формате Enveloped CMS.
     *
     * @param recipientConfig Конфигурация получателя.
     * @param data Данные для зашифрования.
     * @param transport True, если следует использовать key_transport.
     * Иначе применяется key_agreement. Рекомендуется использовать
     * key_transport.
     * @return Enveloped CMS.
     * @throws Exception
     */
    private static byte[] encryptAsByteArray(IConfiguration recipientConfig,
        byte[] data, boolean transport) throws Exception {

        ByteArrayOutputStream envelopedByteArrayOutStream = new ByteArrayOutputStream();
        EnvelopedSignature signature = new EnvelopedSignature();

        if (transport) {
            signature.addKeyTransRecipient(recipientConfig.getCertificate());
        } // if
        else {
            signature.addKeyAgreeRecipient(recipientConfig.getCertificate());
        } // else

        signature.open(envelopedByteArrayOutStream);
        signature.update(data);

        signature.close();
        return envelopedByteArrayOutStream.toByteArray();

    }

    /**
     * Расшифрование Enveloped CMS.
     *
     * @param enveloped Enveloped CMS.
     * @param recipientConfig Конфигурация получателя.
     * @return расшифрованные данные.
     * @throws Exception
     */
    private static byte[] decryptAsByteArray(byte[] enveloped,
        IConfiguration recipientConfig) throws Exception {

        ByteArrayOutputStream decryptedByteDataStream = new ByteArrayOutputStream();
        EnvelopedSignature signature = new EnvelopedSignature(new ByteArrayInputStream(enveloped));

        signature.decrypt(recipientConfig.getCertificate(),
            recipientConfig.getPrivateKey(), decryptedByteDataStream);

        return decryptedByteDataStream.toByteArray();

    }

}
