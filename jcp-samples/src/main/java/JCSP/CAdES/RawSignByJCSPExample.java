/**
 * RawSignByJCSPExample.java,v $
 * version $
 * created 30.06.2021 20:15 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2021.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.CAdES;

import ru.CryptoPro.AdES.AdESConfig;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.Encoder;

import ru.CryptoPro.JCSP.JCSP;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Пример RawSignByJCSPExample демонстрирует создание и проверку
 * CAdES-BES подписи на основе хеша данных на алгоритме ГОСТ 2012
 * (256) провайдера Java CSP.
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class RawSignByJCSPExample {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        final String STORE_TYPE = JCSP.HD_STORE_NAME; // тип контейнера
        final String PROVIDER = JCSP.PROVIDER_NAME; // имя провайдера
        final String ALIAS = "CAdES-2012-256"; // алиас ключа на алгоритме ГОСТ 2012 (256)
        final char[] PASSWORD = "password".toCharArray(); // пароль к ключу
        final byte[] DATA = "Hello, world!".getBytes(); // подписываемые данные

        // Используем CRL из CRL DP сертификата для доступа к
        // CRL в сети.

        System.setProperty("com.sun.security.enableCRLDP", "true");

        // Добавляем java-провайдеры: Java CSP - провайдер
        // по умолчанию.

        JCPInit.initProviders(true);

        // Задаем провайдер по умолчанию для CAdES, если он
        // отличен от JCP.

        AdESConfig.setDefaultProvider(PROVIDER);

        // Читаем закрытый ключ и цепочку сертификатов из
        // контейнера.

        KeyStore keyStore = KeyStore.getInstance(STORE_TYPE, PROVIDER);
        keyStore.load(null, null);

        JCPProtectionParameter parameter = new JCPProtectionParameter(PASSWORD);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(ALIAS, parameter);

        PrivateKey privateKey = entry.getPrivateKey();
        Certificate[] chain = entry.getCertificateChain();

        List<X509Certificate> certificates = new ArrayList<>();

        for (Certificate cert : chain) {
            certificates.add((X509Certificate) cert);
        } // for

        // Формируем отдельный хеш подписываемых данных на нужном
        // алгоритме ГОСТ 2012 (256).

        MessageDigest md = MessageDigest.getInstance( // ГОСТ 2012 (256)
            JCP.GOST_DIGEST_2012_256_NAME, PROVIDER);

        final byte[] RAW_DIGEST = md.digest(DATA); // хеш данных

        // Создаем CAdES-BES отделенную подпись по хешу данных.

        CAdESSignature cAdESSignature = new CAdESSignature(true, true);

        // Корневой сертификат цепочки подписанта должен быть
        // установлен в cacerts.

        cAdESSignature.addSigner(
            PROVIDER,
            null,
            null,
            privateKey,
            certificates,
            CAdESType.CAdES_BES,
            null,
            false,
            null,
            null,
            null,
            true // добавляем цепочку сертификатов в подпись
        );

        ByteArrayOutputStream outSignatureStream = new ByteArrayOutputStream();
        cAdESSignature.open(outSignatureStream);

        cAdESSignature.update(RAW_DIGEST); // хеш данных
        cAdESSignature.close();

        byte[] signature = outSignatureStream.toByteArray(); // подпись
        System.out.println("%%% SIGNATURE:\n" + (new Encoder()).encode(signature));

        // 1. Проверка подписи по хешу данных.

        cAdESSignature = new CAdESSignature(signature, RAW_DIGEST, null, true); // используем хеш данных
        cAdESSignature.verify(null); // можно не задавать сертификаты, т.к. они есть в подписи

        System.out.println("%%% VERIFIED by HASH.");

        // 2. Проверка подписи по подписанным данным.

        cAdESSignature = new CAdESSignature(signature, DATA, null); // используем данные
        cAdESSignature.verify(null); // можно не задавать сертификаты, т.к. они есть в подписи

        System.out.println("%%% VERIFIED by DATA.");

    }

}
