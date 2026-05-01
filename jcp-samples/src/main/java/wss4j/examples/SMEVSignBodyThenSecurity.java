/**
 * $RCSfileSMEVSignBodyThenSecurity.java,v $
 * version $Revision: 36379 $
 * created 11.06.2015 15:50 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 * Copyright 2004-2015 Crypto-Pro. All rights reserved.
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
import org.apache.xml.security.transforms.Transforms;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCPxml.Consts;

import ru.CryptoPro.XAdES.util.XMLUtils;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.GostXAdESUtility;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Пример двойной подписи документа: подпись документа средствами xmlsec
 * и создание Security Header с помощью WSS4J.
 * Чтобы в дальнейшем проверка службой СМЭВ прошла успешно, для создания Security Header должен быть использован контейнер
 * с сертификатом, выпущенным аккредитованным УЦ (контейнер "GisSignContainer").
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class SMEVSignBodyThenSecurity extends GostXAdESUtility {

    /**
     * Контейнер для  создания Security Header с аккредитованным сертификатом.
     */
    public static final String securityAlias = "GisSignContainer";
    public static final String securityPassword = "1";

    /**
     * Контейнер для подписи документа
     */
    public static final IXAdESConfig CONFIG_2001 = XAdESConfig.Default.CONFIG_2001_S;

    /**
     * Загрузчик ключей.
     */
    public static Crypto keyLoader = null;

    /**
     * Запуск примеров.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 1. Читаем документ из файла.

        Document document = GostXAdESUtility.parseFile(TRUST_DIR + "template.xml");

        // 2. Подписываем весь документ.

        JCPPrivateKeyEntry entry = getKeyEntry();
        Document signedDocument = signBody(document, WORK_DIR +
            "result.signed_doc.xml", entry);

        // 3. Добавляем Security Header с подписью.
        // Подписываем тем же ключом.

        signSecurity(signedDocument,
                WORK_DIR + "result.signed_security.xml",
                securityAlias,
                securityPassword);
    }

    /**
     * Получение ключа и сертификата для формирования подписи.
     * Используем класс JCPPrivateKeyEntry просто в качестве
     * варианта для хранения.
     *
     * @return ключ и сертификат.
     * @throws Exception
     */
    private static JCPPrivateKeyEntry getKeyEntry() throws Exception {

        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        // Ключ подписи.
        PrivateKey privateKey = (PrivateKey)keyStore.getKey(
            CONFIG_2001.getSignatureContainer().getAlias(),
            CONFIG_2001.getSignatureContainer().getPassword());

        // Сертификат для помещения в X509Data (KeyInfo).
        Certificate cert = keyStore.getCertificate(
            CONFIG_2001.getSignatureContainer().getAlias());

        return new JCPPrivateKeyEntry(privateKey, new Certificate[] {cert});

    }

    /**
     * Подпись тела документа (uri="").
     *
     * @param srcDoc Подписываемый документ.
     * @param logFile Путь и имя файла для записи документа.
     * @param entry Ключ подписи и сертификат.
     * @return подписанный документ.
     * @throws Exception
     */
    public static Document signBody(Document srcDoc, String logFile,
        JCPPrivateKeyEntry entry) throws Exception {

        final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance(
            "DOM", (Provider) Class.forName(providerName).newInstance());

        String referenceURI = ""; // Пустая строка означает весь документ.

        Node sigParent = srcDoc.getDocumentElement();

        // обязательно 2.
        List transforms = new ArrayList<Transform>() {{
            add(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
            add(sigFactory.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null));
        }};

        // Ссылка на подписываемые данные с указанием алгоритма хеширования.
        Reference ref = sigFactory.newReference(referenceURI,
            sigFactory.newDigestMethod(ru.CryptoPro.JCPxml.Consts.URI_GOST_DIGEST, null),
                transforms, null, null);

        // Создаем объект SignedInfo с указанием алгоритма подписи.
        SignedInfo signedInfo = sigFactory.newSignedInfo(
            sigFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                sigFactory.newSignatureMethod(ru.CryptoPro.JCPxml.Consts.URI_GOST_SIGN, null),
                    Collections.singletonList(ref));

        KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();

        // Создаем X509Data, содержащий сертификат.
        X509Data x509d = keyInfoFactory.newX509Data(Collections.singletonList(entry.getCertificate()));

        // Создаем KeyInfo и добавляем X509Data в него.
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509d));

        // Создаем DOMSignContext и задаем закрытый ключ, а также родительский
        // элемент подписи XMLSignature.
        DOMSignContext dsc = new DOMSignContext(entry.getPrivateKey(), sigParent);

        // Создаем XMLSignature.
        XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);

        // Подписываем.
        signature.sign(dsc);

        // Вывод результата.
        if (logFile != null) {
            XMLUtils.saveXml2File(srcDoc, logFile, false);
        }

        return srcDoc;

    }

    /**
     * Добавление Security Header с подписью документа.
     * Подпись осуществляется методами wss4j.
     *
     * @param document Подписываемый документ.
     * @param logFile Путь и имя файла для записи документа.
     * @return подписанный документ.
     * @throws Exception
     */
    public static Document signSecurity(Document document, String logFile,
        String alias, String password) throws Exception {

        // Общие настройки.
        WSSConfig.setAddJceProviders(false);
        WSSConfig config = new WSSConfig();
        config.setWsiBSPCompliant(false); // убираем inclusive

        Merlin merlin = new Merlin();

        // Контейнер пользователя для подбора ключа.
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

        // Добавляем заголовок.
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.setMustUnderstand(true);
        secHeader.setActor(ACTOR);
        secHeader.insertSecurityHeader(document);

        // Может потребоваться добавление Transform.ENVELOPED в подпись.
        WSSecSignature sigAsymBuilder = new WSSecSignature();
        sigAsymBuilder.setWsConfig(config);
        sigAsymBuilder.setUserInfo(alias, password);
        sigAsymBuilder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        sigAsymBuilder.setSignatureAlgorithm(Consts.URI_GOST_SIGN);
        sigAsymBuilder.setDigestAlgo(Consts.URI_GOST_DIGEST);

        Document signedDoc = sigAsymBuilder.build(document, keyLoader, secHeader);

        if (logFile != null) {
            XMLUtils.saveXml2File(signedDoc, logFile, true);
        }

        // Проверка.

        WSSecurityEngine engine = new WSSecurityEngine();
        engine.setWssConfig(config);

        List<WSSecurityEngineResult> results = engine
            .processSecurityHeader(signedDoc, ACTOR, null, keyLoader);

        System.out.println(results);
        return signedDoc;

    }

}
