/**
 * $RCSfileXmlUtility.java,v $
 * version $Revision: 36379 $
 * created 14.01.2018 11:40 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xmlSign;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;

/**
 * Служебный класс для получения подписи,
 * сертификата из документа и проверки
 * подписи.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XmlUtility {

    /**
     * Чтение узла подписи <ds:Signature> из XML-документа.
     *
     * @param doc подписанный документ
     * @return подпись <ds:Signature>
     * @throws Exception error
     */
    public static Element getSignature(Document doc) throws Exception {
        // чтение из загруженного документа содержимого пространства имени Signature
        Element context = doc.createElementNS(null, "namespaceContext");
        context.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:" + "ds",
                Constants.SignatureSpecNS);
        // выбор из прочитанного содержимого пространства имени узла подписи <ds:Signature>
        return (Element) XPathAPI.selectSingleNode(doc, "//ds:Signature[1]",
                context);
    }

    /**
     * Чтение сертификата из подписи.
     *
     * @param sigElement подпись документа
     * @return X509Certificate
     * @throws Exception error
     */
    public static X509Certificate getCert(Element sigElement) throws Exception {
        // инициализация объекта проверки подписи
        XMLSignature signature = new XMLSignature(sigElement, "");
        // чтение узла <ds:KeyInfo> информации об открытом ключе
        KeyInfo ki = signature.getKeyInfo();
        // чтение сертификата их узла информации об открытом ключе
        return ki.getX509Certificate();
    }

    /**
     * Чтение сертификата из подписанного документа.
     *
     * @param doc подписанный документ
     * @return X509Certificate
     * @throws Exception error
     */
    public static X509Certificate getCert(Document doc) throws Exception {
        return getCert(getSignature(doc));
    }

    /**
     * Проверка подписи всего XML-документа для алгоритма ГОСТ Р 34.10-2001.
     *
     * @param doc подписанный документ
     * @return результат проверки
     * @throws Exception /
     */
    public static boolean verifyDoc(Document doc) throws Exception {
        // выбор из прочитанного содержимого пространства имени узла подписи <ds:Signature>
        Element sigElement = getSignature(doc);

        // инициализация объекта проверки подписи
        XMLSignature signature = new XMLSignature(sigElement, "");
        // чтение узла <ds:KeyInfo> информации об открытом ключе
        KeyInfo ki = signature.getKeyInfo();
        // чтение сертификата их узла информации об открытом ключе
        X509Certificate certKey = ki.getX509Certificate();

        // если сертификат найден, то осуществляется проверка
        // подписи на основе сертфиката
        if (certKey == null)
            throw new Exception("There are no information about public key.");
        return signature.checkSignatureValue(certKey);
    }

}
