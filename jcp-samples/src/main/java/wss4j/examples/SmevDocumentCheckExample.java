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

import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;

import org.w3c.dom.Document;

import ru.CryptoPro.JCP.JCP;

import ru.gosuslugi.smev.SignatureTool.*;
import ru.gosuslugi.smev.SignatureTool.SignatureToolService;
import ru.gosuslugi.smev.SignatureTool.SignatureToolServiceLocator;
import ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureRequestType;
import ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureResponseType;

import xades.util.GostXAdESUtility;

import java.io.FileInputStream;
import java.net.URL;

import java.security.KeyStore;

import java.util.List;

/**
 * Пример SmevDocumentCheckExample демонстрирует проверку средствами wss4j подписанного и сохраненного в файл документа
 * (см. например wss4j.examples.SMEVSignBodyThenSecurity)
 * и (опционально) проверку подписи сервисом СМЭВ.
 *
 * @see wss4j.examples.SMEVSignBodyThenSecurity
 */
public class SmevDocumentCheckExample extends GostXAdESUtility {

    /**
     * Нужно ли проверять подпись онлайн в сервисе СМЭВ.
     */
    private final static boolean checkOnline = true;

    /**
     * Загрузчик ключей.
     */
    public static Crypto keyLoader = null;

    /**
     * Выполнение примеров.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Merlin merlin = new Merlin();

        // Загружаем доверенные сертификаты.
        KeyStore trustStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        try (FileInputStream is = new FileInputStream(TRUST_STORE)) {
            trustStore.load(is, TRUST_PASSWORD);
        }
        merlin.setTrustStore(trustStore);
        keyLoader = merlin;

        Document doc = GostXAdESUtility.parseFile(WORK_DIR +
            "result.signed_security.xml");

        WSSConfig config = new WSSConfig();
        config.setWsiBSPCompliant(false);

        WSSecurityEngine engine = new WSSecurityEngine();
        engine.setWssConfig(config);

        // Можно перебирать разные actor
        List<WSSecurityEngineResult> results = engine.processSecurityHeader(
            doc, ACTOR, null, keyLoader);

        System.out.println("*** Результат проверки:");
        System.out.println(results);

        if (checkOnline) {

            SignatureToolService sts = new SignatureToolServiceLocator();
            SignatureTool st = sts.getSignatureToolPort(new URL(SMEV_SERVICE));

            String mes = org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc);
            System.out.println(mes);

            VerifySignatureRequestType vsrt = new VerifySignatureRequestType(mes, true, ACTOR);
            VerifySignatureResponseType result = st.verifySignature(vsrt);

            System.out.println("Проверка ЭЦП в сервисе СМЭВ: код ошибки = " +
                result.getError().getErrorCode() + ", описание = " +
                    result.getError().getErrorMessage());

        }
    }

}
