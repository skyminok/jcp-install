/**
 * $RCSfile$
 * version $Revision$
 * created 13.07.2009 16:38:35 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2022.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xmlSign;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCP.params.CryptParamsSpec;

import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.xml.tools.TransformerFactoryHelper;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Пример шифрования XML с двумя подписантами.
 *
 * @author Copyright 2004-2022 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CryptXMLTwice {

    /**
     * Контейнер с ключом обмена 2012_256.
     */
    public static String ALIAS_256 = "testDH2012_256";
    public static String PASSWORD_256 = "1";

    /**
     * Контейнер с ключом обмена 2012_512.
     */
    public static String ALIAS_512 = "testDH2012_512";
    public static String PASSWORD_512 = "2";

    public static void main(String[] args) throws Exception {

        //
        // Регистрация алгоритмов ГОСТ.
        //

        JCPInit.initProviders(false);
        XmlInit.init();

        check(ALIAS_256, PASSWORD_256, ALIAS_512, PASSWORD_512, JCP.PROVIDER_NAME );
        System.out.println("%%% OK %%%");

}

/**
 * Чтение ключей, зашифрование и расшифрование документа.
 *
 * @param alias1 Алиас первого ключа.
 * @param pass1 Пароль для первого ключа.
 * @param alias2 Алиас второго ключа.
 * @param pass2 Пароль для второго ключа.
 * @param signProvider Провайдер подписи.
 * @throws Exception
 */
public static void check(String alias1, String pass1, String alias2, String pass2,
    String signProvider) throws Exception {

    //
    // Создание простого XML документа.
    //

    Document doc = CryptXML.createSampleDocument();
    CryptXML.writeDoc(doc, System.out);
    System.out.println("");

    //
    // Загрузка хранилища ключей.
    //

    String storeType = signProvider.equalsIgnoreCase(JCP.PROVIDER_NAME) ? "HDImageStore" : "HDIMAGE";
    KeyStore keyStore = KeyStore.getInstance(storeType, signProvider);
    keyStore.load(null, null);

    //
    // Чтение первой ключевой пары из контейнера.
    //

    PrivateKey key1 = (PrivateKey)keyStore.getKey(alias1, pass1.toCharArray());
    X509Certificate cert1 = (X509Certificate)keyStore.getCertificate(alias1);

    //
    // Чтение первой ключевой пары из контейнера.
    //

    PrivateKey key2 = (PrivateKey)keyStore.getKey(alias2, pass2.toCharArray());
    X509Certificate cert2 = (X509Certificate)keyStore.getCertificate(alias2);

    //
    // Зашифрование "на сертификате" для двух подписантов.
    //

    encryptTwice(doc, cert1,  cert2, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
    CryptXML.writeDoc(doc, System.out);
    System.out.println("");

    //
    // Расшифрование для первого подписанта.
    //

    Document doc1 = copyDoc(doc);
    CryptXML.decrypt(doc1, key1);
    CryptXML.writeDoc(doc1, System.out);
    System.out.println("");

    //
    // Расшифрование для второго подписанта.
    //

    Document doc2 = copyDoc(doc);
    CryptXML.decrypt(doc2, key2);
    CryptXML.writeDoc(doc2, System.out);
    System.out.println("");

}

/**
 * Зашифрование документа с двумя подписантами.
 *
 * @param doc документ, который будем шифровать
 * @param cert1 сертификат
 * @return зашифрованный документ
 * @throws Exception ошибки шифрования
 */
public static Document encryptTwice(Document doc, X509Certificate cert1, X509Certificate
    cert2, CryptParamsInterface p) throws Exception {

    //
    // Создание случайного сессионного ключа.
    //
    KeyGenerator kg = KeyGenerator.getInstance("GOST28147");

    if (p != null) {
        kg.init(p);
    }

    SecretKey sessionKey = kg.generateKey();

    //
    // Зашифрование сессионного ключа на первом сертификате.
    //

    EncryptedKey encryptedKey1 = CryptXML.wrapKey(doc, sessionKey, cert1);

    //
    // Зашифрование сессионного ключа на втором сертификате.
    //

    EncryptedKey encryptedKey2 = CryptXML.wrapKey(doc, sessionKey, cert2);

    //
    // Зашифрование документа.
    //

    return encryptTwice(doc, sessionKey, encryptedKey1, encryptedKey2);

}

/**
 * Зашифрование документа doc на sessionKey.
 *
 * @param doc документ, который будем шифровать
 * @param sessionKey сессионный ключ шифрования
 * @param encryptedKey1 зашифрованный sessionKey на первом сертификате
 * @param encryptedKey2 зашифрованный sessionKey на втором сертификате
 * @return шифрованный документ
 * @throws Exception ошибки шифрования
 */
public static Document encryptTwice(Document doc, SecretKey sessionKey,
    EncryptedKey encryptedKey1, EncryptedKey encryptedKey2) throws Exception {

    Element element = doc.getDocumentElement();

    //
    // Создаем шифратор в режиме зашифрования. Константа URI_GOST_CIPHER
    // определена в файле ru.CryptoPro.JCPxml.Consts
    // public static final String URI_GOST_CIPHER =
    // "urn:ietf:params:xml:ns:cpxmlsec:algorithms:gost28147";
    //

    XMLCipher xmlCipher = XMLCipher.getInstance(Consts.URI_GOST_CIPHER);
    xmlCipher.init(XMLCipher.ENCRYPT_MODE, sessionKey);

    //
    // Добавляем шифрованный ключ.
    //

    EncryptedData encryptedData = xmlCipher.getEncryptedData();
    KeyInfo keyInfo = new KeyInfo(doc);
    keyInfo.add(encryptedKey1);
    keyInfo.add(encryptedKey2);
    encryptedData.setKeyInfo(keyInfo);

    //
    // Зашифрование документа.
    //

    xmlCipher.doFinal(doc, element, true);
    return doc;

}

/**
 * Копирование документа.
 *
 * @param doc document to copy.
 * @throws TransformerException If an unrecoverable error
 * occurs during the course of the transformation.
 */
public static Document copyDoc(Document doc) throws TransformerException {
    // создание объекта копирования содержимого XML-документа
    DOMResult result = new DOMResult();
    Transformer transformer = TransformerFactoryHelper.newInstance().newTransformer();
    // копирование содержимого XML-документа в новый документ
    transformer.transform(new DOMSource(doc), result);
    return (Document)result.getNode();
}

}
