/**
 * Copyright 2004-2013 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package wss4j.examples;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;

import org.w3c.dom.Document;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCPxml.Consts;

import ru.CryptoPro.XAdES.util.XMLUtils;
import ru.gosuslugi.smev.SignatureTool.SignatureTool;
import ru.gosuslugi.smev.SignatureTool.SignatureToolService;
import ru.gosuslugi.smev.SignatureTool.SignatureToolServiceLocator;
import ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureRequestType;
import ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureResponseType;
import wss4j.utility.SOAPUtility;
import xades.util.GostXAdESUtility;
import xades.util.XMLUtility;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

import java.util.List;

/**
 *
 * Пример SMEVExample демонстрирует создание  и проверку подписи xml-файла средствами wss4j, а также (опционально)
 * последующую проверку подписи сервисом СМЭВ.
 * Чтобы проверка службой СМЭВ прошла успешно, для подписи должен быть использован контейнер с сертификатом, выпущенным
 * аккредитованным УЦ (контейнер "GisSignContainer").
 *
 */
public class SMEVExample extends GostXAdESUtility {

    /**
     * Загрузчик ключей.
     */
    public static Crypto keyLoader = null;

    /**
     * Контейнер с аккредитованным сертификатом.
     */
    public static final String securityAlias = "GisSignContainer";
    public static final String securityPassword = "1";


    /**
     * Нужно ли проверять подпись онлайн в сервисе СМЭВ.
     */
    private final static boolean checkOnline = true;

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // подпись
        Document signedDoc = sign(
                securityAlias,
                securityPassword.toCharArray(),// - не работает с пустым паролем!!
                TRUST_DIR + "template.sgn.xml");

        // сохранение подписанного документа
        XMLUtils.saveXml2File(signedDoc,
                WORK_DIR + "stub_xades_with_sh.xml", true);

        // Проверка подписи
        verify(WORK_DIR + "stub_xades_with_sh.xml");
        if (checkOnline)
            verifyOnline(WORK_DIR + "stub_xades_with_sh.xml");

    }


    /**
     * Подпись XML-документа из файла
     * @param alias контейнер для подписи
     * @param password пароль
     * @param file путь к файлу
     * @return
     * @throws Exception
     */
    public static Document sign(String alias, char[] password,
                                String file) throws Exception {

        Document inDoc = SOAPUtility.getDocumentFromFile(file);
        return sign(alias, password, inDoc);

    }

    /**
     *  Подпись документа  средствами wss4j.
     * @param alias контейнер для подписи
     * @param password пароль
     * @param inDoc документ для подписи
     * @return
     * @throws Exception
     */
    public static Document sign(String alias, char[] password,
                                Document inDoc) throws Exception {


        WSSConfig.setAddJceProviders(false);
        WSSConfig config = new WSSConfig();
        config.setWsiBSPCompliant(false);

        Merlin merlin = new Merlin();

        // Контейнер пользователя.
        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        // Хранилище доверенных сертификатов, содержит корневой сертификат клиента.
        KeyStore trustStore = KeyStore.getInstance(JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME);
        try (FileInputStream is = new FileInputStream(TRUST_STORE)) {
            trustStore.load(is, TRUST_PASSWORD);
        }
        merlin.setKeyStore(keyStore);
        merlin.setTrustStore(trustStore);
        keyLoader = merlin;

        // *** Подпись документа ***

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.setMustUnderstand(true);
        secHeader.setActor(ACTOR);
        secHeader.insertSecurityHeader(inDoc);

        WSSecSignature sigAsymBuilder = new WSSecSignature();
        sigAsymBuilder.setWsConfig(config);
        sigAsymBuilder.setUserInfo(alias, password==null? null:String.valueOf(password));
        sigAsymBuilder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        sigAsymBuilder.setSignatureAlgorithm(Consts.URI_GOST_SIGN);
        sigAsymBuilder.setDigestAlgo(Consts.URI_GOST_DIGEST);

        return sigAsymBuilder.build(inDoc, keyLoader, secHeader);
    }

    /**
     * Проверка подписанного документа средствами wss4j (из файла)
     * @param file путь к файлу
     * @throws Exception
     */
    public static void verify(String file) throws Exception {

        WSSConfig.setAddJceProviders(false);
        WSSConfig config = new WSSConfig();
        config.setWsiBSPCompliant(false);

        Merlin merlin = new Merlin();

        // Контейнер пользователя.
        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        // Хранилище доверенных сертификатов, содержит корневой сертификат клиента.
        KeyStore trustStore = KeyStore.getInstance(JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME);
        try (FileInputStream is = new FileInputStream(TRUST_STORE)) {
            trustStore.load(is, TRUST_PASSWORD);
        }
        merlin.setKeyStore(keyStore);
        merlin.setTrustStore(trustStore);
        keyLoader = merlin;

        // *** Проверка документа ***

        Document outDoc = SOAPUtility.getDocumentFromFile(file);

        WSSecurityEngine engine = new WSSecurityEngine();
        engine.setWssConfig(config);

        List<WSSecurityEngineResult> results = engine
                .processSecurityHeader(outDoc, ACTOR, null, keyLoader);

        System.out.println(results);
    }

    /**
     * Проверка подписанного документа (из файла) онлайн сервисом СМЭВ.
     * @param file
     * @throws Exception
     */
    public static void verifyOnline(String file) throws Exception {

        Document doc = SOAPUtility.getDocumentFromFile(file);

        String msg = org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);


        // Используем веб-клиент СМЭВ.
        SignatureToolService sts = new SignatureToolServiceLocator();

        // Задаем адрес тестового сервиса.
        SignatureTool st = sts.getSignatureToolPort(new URL(SMEV_SERVICE));

        // Передаем документ, при этом зарещаем проверять сертификат.
        VerifySignatureRequestType vsrType = new VerifySignatureRequestType(msg, true, ACTOR);


        VerifySignatureResponseType result = st.verifySignature(vsrType);
        // Результат проверки подписи сервисом СМЭВ.
        System.out.println("Проверка ЭЦП в сервисе СМЭВ: код ошибки = " +
                result.getError().getErrorCode() + ", описание = " + result.getError().getErrorMessage());
    }


}
