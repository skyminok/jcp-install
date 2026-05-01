/**
 * $RCSfileDigitalMarkingExample.java,v $
 * version $Revision: 36379 $
 * created 10.04.2020 16:06 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2020.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package digital_marking;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Encoder;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This example shows how to create a signature with
 * type of CAdES-signature (CAdES-BES) using GOST
 * 34.10-2012 (256). It is appropriate for integration
 * with
 * https://chestnyznak.ru/en/
 *
 * Signer's certificate must be imported into the
 * chestnyznak system through the web-interface.
 *
 * API mentioned bellow offers processing of a
 * uuid-data pair:
 * one should pass necessary data to sign(), get
 * a valid signature and send it as base64 to the
 * server with uuid (as uuid-signature).
 *
 * The root certificate of the signer certificate
 * chain must be installed to the trust store
 * JRE/lib/security/cacerts.
 *
 * Пример CAdES-подписи (CAdES-BES) с использованием
 * ГОСТ 34.10-2012 (256). Подходит для интеграции с
 * https://честныйзнак.рф
 *
 * Сертификат подписанта должен быть зарегистрирован
 * в системе честныйзнак через web-интерфейс.
 *
 * Описанное ниже API предалагает обработку uuid-data
 * пары:
 * нужно передать необходимые данные в sign(), получить
 * подпись и в виде base64 отправить ее на сервер вместе
 * с uuid (как uuid-signature).
 *
 * Корневой сертификат цепочки сертификатов подписанта
 * должен быть установлен в хранилище доверенных
 * сертификатов JRE/lib/security/cacerts.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class DigitalMarkingExample {

    /**
     * The function produces a signature with type of
     * CAdES-BES and uses a key with algorithm GOST
     * 34.10-2012 (256) and signature algorithm GOST
     * 2012 (256).
     *
     * Функция создает подпись с типом CAdES-BES и
     * использует ключ на алгоритме ГОСТ 34.10-2012
     * (256) и алгоритм подписи ГОСТ 2012 (256).
     *
     * @param alias Key alias. Алиас ключа.
     * @param password Key password. Пароль к ключу.
     * @param data Data to be signed. Данные для подписи.
     * @return plain signature. plain-подпись.
     * @throws Exception
     */
    public static byte[] sign(String alias, char[] password,
        byte[] data) throws Exception {

        // Loading keys.
        //
        // Загрузка ключей.

        KeyStore keyStore = KeyStore.getInstance(
            JCP.HD_STORE_NAME,
            JCP.PROVIDER_NAME
        );

        keyStore.load(null, null);

        // Reading the key and the certificates.
        //
        // Чтение ключа.

        PrivateKey privateKey = (PrivateKey)
            keyStore.getKey(alias, password);

        // Reading certificate(s) from the key container.
        // Чтение сертификатов из ключевого контейнера.
        //
        // Let's suppose that a certificate chain looks like:
        // Предположим, что цепочка сертификатов подписи представляет
        // собой последовательность:
        //
        // Client -> CA1 -> Root
        //
        // where
        // где
        //
        // Client - signer certificate (сертификат подписи)
        // CA1 - intermediate certificate of CA1 (промежуточный сертификат УЦ CA1)
        // Root - trusted certificate (корневой сертификат).
        //
        // The root certificate of the signer certificate chain must be
        // installed to the trust store JRE/lib/security/cacerts:
        // При этом корневой сертификат цепочки сертификатов подписанта
        // Root должен быть установлен в хранилище доверенных сертификатов
        // JRE/lib/security/cacerts:
        //
        // "C:\Program Files\Java\jre7\bin\keytool.exe" -importcert -file root.cer -alias test_root -keystore "C:\Program Files\Java\jre7\lib\security\cacerts"
        //
        // Key container can contain one (Client) or more certificates
        // (e.g. a certificate chain with or without Root).
        // В ключевом контейнере может быть как один сертификат (Client),
        // так и полная цепочка (вместе с Root или без него).
        //
        // This example supposes adding a full certificate chain to the
        // signature with type of CAdES-BES. It is strictly recommended
        // to install a certificate chain into the key container using
        // JCP or CSP ControlPane.
        // Данный пример предполагает добавление в подпись формата CAdES-BES
        // целой цепочки, поэтому настоятельно рекомендуется установить в
        // ключевой контейнер всю цепочку сертификатов, например, в панели
        // JCP или CSP.
        //
        // If the key container does not contain a certificate chain (e.g.
        // the key container contains a Client only), there are 2 ways to
        // add a certificate chain to the signature:
        // 1) use any well-known method to get a CA1 (and Root) like a file or
        // a byte array and save it, then add it as X609Certificate to the
        // 'certificates' after Client (Client must be the first), then pass
        // 'certificates' to addSigner();
        // 2) if network access is available and Client (and CA1) contains
        // an AIA-extension with the address of it's issuer then the following
        // settings can be used:
        // Если цепочки сертификатов в контейнере нет (т.е. в ключевом контейнере
        // находится только Client), то возможны 2 варианта, как добавить цепочку
        // в подпись:
        // 1) любым известным способом получить CA1 (и Root) - в виде файла
        // или массива байтов, а затем передать их в виде X609Certificate в
        // certificates вслед за сертификатом Client (Client должен быть первым),
        // то есть добавить в список и передать в addSigner().
        // 2) если есть доступ в сеть и сертификат Client (и CA1) содержит в
        // AIA-расширении адрес сертификата издавшего его УЦ, то можно задать
        // следующие настройки в коде:
        //
        // System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
        // System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true");
        //
        // if enableAIAcaIssuers equals true then if any certificate from the
        // certificate chain has not been found (e.g. CA1) inside the 'certificates'
        // then it will be downloaded according to the link from the AIA-extension
        // (e.g. from Client)if such link exists. It's important to know that not
        // all the certificates have AIA with references to it's issuers.
        // enableAIAcaIssuers, равный true, означает, что если сертификат из
        // цепочки не найден в certificates, например, не найден CA1, то он
        // будет скачан по ссылке из AIA-расширения сертификата Client, если
        // таковая ссылка в нем имеется. Учтите, не все сертификаты содержат
        // AIA со ссылками на сертификаты своих издателей.
        //
        // The root certificate is not required to be added to the 'certificates'
        // for addSigner(), because it will be got from the trust store (cacerts)
        // during chain building.
        // Корневой сертификат в списке certificates, передаваемом в addSigner(),
        // необязателен, т.к. будет получен из хранилища доверенных сертификатов
        // cacerts в ходе построения цепочки сертификатов.
        //
        // Chain validation is done using CRL, it supposes getting and checking
        // the certificate status. CRL can be:
        // 1) passed to addSigner() as a set of X509CRL. That validation will
        // be executed without calls to network.
        // 2) downloaded from network if the program has an access to
        // network and there is a link to the CRL inside the checking
        // certificate and the following settings are also turned on:
        // Проверка цепочки сертификатов осуществляется с помощью CRL. Проверка
        // подразумевает получение статуса сертификата из CRL. CRL может быть:
        // 1) передан в addSigner() в виде набора X509CRL в качестве параметра.
        // Такая проверка называется offline, без обращений в сеть.
        // 2) загружен из сети, если к ней есть доступ, есть ссылки на CRL в
        // проверяемом сертификате и заданы параметры:
        //
        // System.setProperty("com.sun.security.enableCRLDP", "true");
        // System.setProperty("com.ibm.security.enableCRLDP", "true");
        //
        // enableCRLDP with true means loading the CRL from network.
        // enableCRLDP, равный true, означает загрузку CRL из сети, если такая
        // возможность есть.
        //
        // Remember, settings like System.setProperty impact all the
        // java-process.
        // Помните, настройки типа System.setProperty влияет на весь
        // java-процесс.
        //
        //
        // Enabling online-validation the certificate chain. CRL will be
        // downloaded from network.
        // Включаем возможность онлайновой проверки цепочки сертификатов.
        // CRL будет скачан из сети.
        //

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // If enableCRLDP is not set 'true' then the valid CRLs should
        // be passed to the method addSigner().
        //
        // Если enableCRLDP не задан 'true', то валидные CRL должны
        // быть переданы в функцию addSigner().

        Certificate[] certificates = keyStore
            .getCertificateChain(alias);

        List<X509Certificate> chain =
            new ArrayList<X509Certificate>();

        for (Certificate cert : certificates) {
            chain.add((X509Certificate) cert);
        }

        // Signing the data.
        //
        // Подпись данных.

        CAdESSignature cAdESSignature = new CAdESSignature();

        // Adding the signer. Building and validating the
        // certificate chain will have been done.
        // Добавление подписанта. Будет выполнено построение
        // цепочки сертификатов ии проверка с помощью CRL.
        //
        // The last parameter of addSigner() means that the built
        // certificate chain should be added to the signature.
        // Последний параметр в addSigner() требует добавить
        // построенную цепочку сертификатов в подпись.

        cAdESSignature.addSigner(
            JCP.PROVIDER_NAME, // signature provider // провайдер подписи
            null,
            null,
            privateKey, // signing key // ключ подписанта
            chain,      // signing certificate chain // цепочка сертификатов подписанта
            CAdESType.CAdES_BES,
            null,
            false,
            null,
            null,
            null, // no CRL files
            true  // add the signing certificate chain to the signature // добавить цепочку подписанта в подпись
        );

        ByteArrayOutputStream signatureStream
            = new ByteArrayOutputStream();

        try {

            cAdESSignature.open(signatureStream);
            cAdESSignature.update(data);
            cAdESSignature.close();

        } finally {
            signatureStream.close();
        }

        return signatureStream.toByteArray();

    }

    /**
     * Start the program.
     * Запуск приложения.
     *
     * @param args Arguments. Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Key alias (name) that will be passed to sign data can
        // be got using enumerating of key containers:
        //
        // Алиас (имя) ключевого контейнера, передаваемый для
        // подписи, может быть получен путем перечисления имен
        // контейнеров:
        //
        // KeyStore keyStore = KeyStore.getInstance(keyStoreType, keyStoreProvider);
        // keyStore.load();
        // Enumeration<String> aliases = keyStore.aliases();
        //
        // Also you can find it in the JCP or CSP ControlPane.
        // Также их можно просмотреть в панели JCP или CSP.

        byte[] sign = sign(
            "le-0597430c-48d5-40b3-a928-850d97251821", // key alias         // алиас ключа
            "2".toCharArray(),                         // key password      // пароль к ключу
            "security".getBytes()                      // data to be signed // данные для подписи
        );

        System.out.println((new Encoder()).encode(sign));

    }

}
