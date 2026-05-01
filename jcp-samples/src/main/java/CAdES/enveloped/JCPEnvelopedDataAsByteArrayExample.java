/**
 * $RCSfileJCPEnvelopedDataAsByteArrayExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 11:03 by afevma
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
import CAdES.configuration.container.ServerContainer2012_256;
import CAdES.configuration.container.ServerContainer2012_512;
import CAdES.configuration.container.ServerSigContainer2012_256;
import CAdES.configuration.container.ServerSigContainer2012_512;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Пример создания и расшифрования подписи Enveloped CMS
 * из строки с помощью провайдера JCP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPEnvelopedDataAsByteArrayExample implements IEnvelopedData{

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

        envelope(null, new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(null, new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaMagma, new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaKuznechik, new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, new SimpleConfiguration(new ServerContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, new SimpleConfiguration(new ServerContainer2012_512(), false), transport);

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

        envelope(null, new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(null, new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaMagma, new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaMagma, new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechik, new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaKuznechik, new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaMagmaMac, new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);

        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, new SimpleConfiguration(new ServerSigContainer2012_256(), false), transport);
        envelope(EncryptionKeyAlgorithm.ekaKuznechikMac, new SimpleConfiguration(new ServerSigContainer2012_512(), false), transport);

    }

    /**
     * Выполнение набора примеров.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param recipientConfig Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @throws Exception
     */
    public static void envelope(EncryptionKeyAlgorithm alg,
        IConfiguration recipientConfig, boolean transport)
        throws Exception {
        encryptDecrypt(alg, DATA, false, recipientConfig, transport);
    }

    /**
     * Пример зашифрования данных в Enveloped CMS и расшифрования.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param data Данные для зашифрования.
     * @param isCms True, если data - это CMS.
     * @param recipientConfig Конфигурация получателя.
     * @param transport True, если следует использовать key_transport.
     * Иначе приеняется key_agreement. Рекомендуется использовать
     * key_transport.
     * @return расшифрованное сообщение.
     * @throws Exception
     */
    public static byte[] encryptDecrypt(EncryptionKeyAlgorithm alg, byte[] data,
        boolean isCms, IConfiguration recipientConfig, boolean transport) throws
        Exception {

        byte[] enveloped = encryptAsByteArray(alg, recipientConfig, data, transport);
        byte[] decrypted = decryptAsByteArray(alg, enveloped, recipientConfig);

        if (!Array.compare(decrypted, data)) {
            throw new Exception("Decryption failed, source data and decrypted data are not equal");
        } // if

        // if (!isCms) {
        //     System.out.println("Data: " + new String(data));
        //     System.out.println("Decrypted byte data: " + new String(decrypted));
        // } // if

        return decrypted;
    }

    /**
     * Зашифрование данных в формате Enveloped CMS.
     *
     * @param alg Алгоритм ключа шифрования.
     * @param recipientConfig Конфигурация получателя.
     * @param data Данные для зашифрования.
     * @param transport True, если следует использовать key_transport.
     * Иначе применяется key_agreement. Рекомендуется использовать
     * key_transport.
     * @return Enveloped CMS.
     * @throws Exception
     */
    private static byte[] encryptAsByteArray(EncryptionKeyAlgorithm alg,
        IConfiguration recipientConfig, byte[] data, boolean transport)
        throws Exception {

        ByteArrayOutputStream envelopedByteArrayOutStream = new ByteArrayOutputStream();
        EnvelopedSignature signature = new EnvelopedSignature(alg);

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
     * @param alg Алгоритм ключа шифрования.
     * @param enveloped Enveloped CMS.
     * @param recipientConfig Конфигурация получателя.
     * @return расшифрованные данные.
     * @throws Exception
     */
    private static byte[] decryptAsByteArray(EncryptionKeyAlgorithm alg,
        byte[] enveloped, IConfiguration recipientConfig) throws Exception {

        ByteArrayOutputStream decryptedByteDataStream = new ByteArrayOutputStream();
        EnvelopedSignature signature = new EnvelopedSignature(new ByteArrayInputStream(enveloped));

        signature.decrypt(recipientConfig.getCertificate(),
            recipientConfig.getPrivateKey(), decryptedByteDataStream);

        return decryptedByteDataStream.toByteArray();

    }

}
