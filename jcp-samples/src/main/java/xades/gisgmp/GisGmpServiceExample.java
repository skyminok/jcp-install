/**
 * $RCSfileGisGmpServiceExample.java,v $
 * version $Revision: 36379 $
 * created 10.06.2015 11:08 by afevma
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
package xades.gisgmp;

import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;

import ru.CryptoPro.XAdES.util.XMLUtils;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_Service;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_ServiceLocator;

import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.CryptoPro.JCP.tools.Array;


import wss4j.examples.SMEVSignBodyThenSecurity;
import xades.XAdES4JSignVerify;
import xades.XAdESSignVerify;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.GostXAdESUtility;
import xades.util.XMLUtility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Пример подписи (xades-T) и отправки документа в сервис ГИС ГМП.
 *
 * Если все хорошо, то возвращает код 0.
 * Для успешного выполнения в документе нужно каждый раз задвать новое значение узла
 * <SystemIdentifier>.
 * Значение SystemInformation (==идентификатор платежа) формируется следующим образом: он состоит из 32 знаков.
 * 1 - "1"
 * следующие 9 - БИК ("044525716", совпадат со значением <org:BIK>)
 * следующие 6 - номер подразделения ("452571")
 * следующие 6 - дата платежа в формате ДДММГГГГ ("20112017") - должна совпадать с датами в узлах <PaymentDate> и
 * <ReceiptDate> (только в них дата записывается в формате ГГГГ-ММ-ДД).
 * последние 8 - уникальный номер платежа - вот его как раз нужно каждый раз изменять.
 *
 * Если повторно провести платеж с тем же  SystemIdentifier, сервис вернет код 5 (Импортируемые данные уже присутствуют
 * в Системе)
 *
 *
 * Пример платежного документа (SOAP_etalon_pay.xml).
 <soapenv:Envelope
    xmlns:rev="http://smev.gosuslugi.ru/rev120315"
    xmlns:smev="http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/"
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
    xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
    <soapenv:Header/>
    <soapenv:Body wsu:Id="body">
        <smev:GISGMPTransferMsg>
            <rev:Message>
                <rev:Sender>
                    <rev:Code>AN0000001</rev:Code>
                    <rev:Name>ИС АН 1</rev:Name>
                </rev:Sender>
                <rev:Recipient>
                    <rev:Code>RKZN35001</rev:Code>
                    <rev:Name>Казначейство России</rev:Name>
                </rev:Recipient>
                <rev:ServiceName>GISGMP</rev:ServiceName>
                <rev:TypeCode>GFNC</rev:TypeCode>
                <rev:Status>REQUEST</rev:Status>
                <rev:Date>2017-11-20T10:25:00.000</rev:Date>
                <rev:ExchangeType>6</rev:ExchangeType>
                <rev:TestMsg>test</rev:TestMsg>
            </rev:Message>
            <rev:MessageData>
                <rev:AppData>
                    <gisgmp:RequestMessage
                        xmlns="http://roskazna.ru/gisgmp/xsd/116/PaymentInfo"
                        xmlns:com="http://roskazna.ru/gisgmp/xsd/116/Common"
                        xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                        xmlns:gisgmp="http://roskazna.ru/gisgmp/xsd/116/Message"
                        xmlns:msgd="http://roskazna.ru/gisgmp/xsd/116/MessageData"
                        xmlns:n1="http://www.altova.com/samplexml/other-namespace"
                        xmlns:pgu="http://roskazna.ru/gisgmp/xsd/116/PGU_ImportRequest"
                        xmlns:bgi="http://roskazna.ru/gisgmp/xsd/116/BudgetIndex"
                        xmlns:org="http://roskazna.ru/gisgmp/xsd/116/Organization"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        Id="P_a7654321-8bcf-de90-123f-abcde0987654"
                        senderIdentifier="000147"
                        senderRole="7"
                        timestamp="2017-11-20T10:25:00.000">
                        <msgd:ImportRequest>
                            <pgu:Package>
                                <pgu:Document>
                                    <FinalPayment Id="P_a1234567-bcf8-90de-f123-4567890abcde">
                                        <SupplierBillID>0</SupplierBillID>
                                        <Narrative>Оплата</Narrative>
                                        <Amount>10000</Amount>
                                        <PaymentDate>2017-11-20</PaymentDate>
                                        <ReceiptDate>2017-11-20</ReceiptDate>
                                        <BudgetIndex>
                                            <bgi:Status>01</bgi:Status>
                                            <bgi:Purpose>0</bgi:Purpose>
                                            <bgi:TaxPeriod>0</bgi:TaxPeriod>
                                            <bgi:TaxDocNumber>0</bgi:TaxDocNumber>
                                            <bgi:TaxDocDate>0</bgi:TaxDocDate>
                                            <bgi:PaymentType>0</bgi:PaymentType>
                                        </BudgetIndex>
                                        <PaymentIdentificationData>
                                            <Bank>
                                                <org:Name>ВТБ24</org:Name>
                                                <org:BIK>044525716</org:BIK>
                                                <org:CorrespondentBankAccount>40602810000380000020</org:CorrespondentBankAccount>
                                            </Bank>
                                            <SystemIdentifier>10445257164525712011201788888888</SystemIdentifier>
                                        </PaymentIdentificationData>
                                        <AccDoc xmlns="http://roskazna.ru/gisgmp/xsd/116/PaymentInfo">
                                            <AccDocNo xmlns="http://roskazna.ru/gisgmp/xsd/116/PaymentInfo">0</AccDocNo>
                                            <AccDocDate xmlns="http://roskazna.ru/gisgmp/xsd/116/PaymentInfo">2017-11-20</AccDocDate>
                                        </AccDoc>
                                        <Payer>
                                            <com:PayerIdentifier>0100000000023456789012643</com:PayerIdentifier>
                                            <PayerName>Иванов Иван Николаевич</PayerName>
                                        </Payer>
                                        <Payee>
                                            <PayeeName>ГИБДД</PayeeName>
                                            <payeeINN>3543655766</payeeINN>
                                            <payeeKPP>354365576</payeeKPP>
                                            <PayeeBankAcc>
                                                <org:AccountNumber>40602810000380000020</org:AccountNumber>
                                                <org:Bank>
                                                    <org:Name>Альфа</org:Name>
                                                    <org:BIK>044525716</org:BIK>
                                                    <org:CorrespondentBankAccount>30101810100000000716</org:CorrespondentBankAccount>
                                                </org:Bank>
                                            </PayeeBankAcc>
                                        </Payee>
                                        <ChangeStatus  meaning="1"/>
                                        <KBK>18851111111111111113</KBK>
                                        <OKTMO>12345673</OKTMO>
                                    </FinalPayment>
                                </pgu:Document>
                            </pgu:Package>
                        </msgd:ImportRequest>
                    </gisgmp:RequestMessage>
                </rev:AppData>
            </rev:MessageData>
        </smev:GISGMPTransferMsg>
    </soapenv:Body>
 </soapenv:Envelope>
 *
 * Запрос обработки платежа (SOAP_etalon_pay_response_1_id.xml).
 <soapenv:Envelope xmlns:inc="http://www.w3.org/2004/08/xop/include" xmlns:mes="http://roskazna.ru/gisgmp/xsd/116/Message" xmlns:mes1="http://roskazna.ru/gisgmp/xsd/116/MessageData" xmlns:rev="http://smev.gosuslugi.ru/rev120315" xmlns:smev="http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" xmlns:xd="http://www.w3.org/2000/09/xmldsig#">
    <soapenv:Body wsu:Id="body">
        <smev:GISGMPTransferMsg>
            <rev:Message>
                 <rev:Sender>
                    <rev:Code>AN0000001</rev:Code>
                    <rev:Name>ИС АН 1</rev:Name>
                </rev:Sender>
                <rev:Recipient>
                    <rev:Code>RKZN35001</rev:Code>
                    <rev:Name>Казначейство России</rev:Name>
                </rev:Recipient>
                <rev:ServiceName>GISGMP</rev:ServiceName>
                <rev:TypeCode>GFNC</rev:TypeCode>
                <rev:Status>REQUEST</rev:Status>
                <rev:Date>20172015-06-15T10:25:00.0Z</rev:Date>
                <rev:ExchangeType>6</rev:ExchangeType>
            </rev:Message>
            <rev:MessageData>
                <rev:AppData xmlns:gisgmp="http://roskazna.ru/gisgmp/xsd/116/Message" xmlns:msgd="http://roskazna.ru/gisgmp/xsd/116/MessageData" xmlns:pdr="http://roskazna.ru/gisgmp/xsd/116/PGU_DataRequest" xmlns:smev="http://smev.gosuslugi.ru/rev120315" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                    <gisgmp:RequestMessage Id="P_a7654321-8bcf-de90-123f-abcde0987654"
                        senderIdentifier="000147"
                        senderRole="7"
                        timestamp="2015-06-15T10:25:00.0Z">
                        <msgd:PackageStatusRequest xmlns:psr="http://roskazna.ru/gisgmp/xsd/116/PackageStatusRequest">
                            <psr:PackageID></psr:PackageID>
                        </msgd:PackageStatusRequest>
                    </gisgmp:RequestMessage>
                </rev:AppData>
            </rev:MessageData>
        </smev:GISGMPTransferMsg>
    </soapenv:Body>
 </soapenv:Envelope>
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @see wss4j.examples.SMEVSignBodyThenSecurity
 * @see xades.XAdESExample
 */
public class GisGmpServiceExample extends GostXAdESUtility {

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 1. Исходный документ. Содержит FinalPayment с Id {@link #SIGNING_ID}.

        byte[] finalPaymentDoc = Array.readFile(TRUST_DIR + "SOAP_etalon_pay.xml");

        // 2. Создание XAdES-T подписи с помощью тестового ключа
        // (сертификат из внешнего тестового УЦ) и внешней тестовой
        // TSP службы (http://www.cryptopro.ru:80/tsp/).

        IXAdESConfig XAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2001_S;

        Document signedXAdESTFinalPaymentDoc = XAdESSignVerify.sign(new Integer[]
            {XAdESType.XAdES_T}, XAdESConfigTestSignKey, finalPaymentDoc, WORK_DIR,
                SIGNING_ID, new ITransform[] {new EnvelopedTransform()}, null, false,
                    "http://www.cryptopro.ru:80/tsp/", null);

        XAdESSignVerify.verify(signedXAdESTFinalPaymentDoc, new Integer[]{XAdESType.XAdES_T},
            null, null, false, 1);

        XAdES4JSignVerify.verify(XAdESConfigTestSignKey, signedXAdESTFinalPaymentDoc, false);

        // Лог
        XMLUtils.saveXml2File(signedXAdESTFinalPaymentDoc,
            WORK_DIR + "SOAP_etalon_pay_xades_t.xml", true);

        // 3. Добавление Security Header с помощью ключевого контейнера
        // с сертификатом, выпущенным в аккредитованном УЦ.


        // Ключевой контейнер с сертификатом из аккредитованного УЦ.
        final String securityAlias = "GisSignContainer";
        final String securityPassword = "1";

        // Подпись и лог
        SMEVSignBodyThenSecurity.signSecurity(signedXAdESTFinalPaymentDoc,
            WORK_DIR + "SOAP_etalon_pay_xades_t_with_security.xml",
                securityAlias, securityPassword);

        // Файл с двумя подписями.

        byte[] doubleSignedDoc = Array.readFile(WORK_DIR +
            "SOAP_etalon_pay_xades_t_with_security.xml");

        InputStream doubleSignedDocInput = new ByteArrayInputStream(doubleSignedDoc);
        Message doubleSignedDocMsg = new Message(doubleSignedDocInput);

        // 4. Первое обращение к сервису - передача документа, получение
        // ответа с идентификатором пакета.

        SmevGISGMPService_Service gmpService = new SmevGISGMPService_ServiceLocator();
        SmevGISGMPService_PortType gmpPort = gmpService.getSmevGISGMPServiceSOAP();

        SOAPEnvelope response1Env = gmpPort.GISGMPTransferMsg(doubleSignedDocMsg);

        System.out.println("\nReceived response #1...\n");
        Document response1Doc = response1Env.getOwnerDocument();

        // Лог
        String response1Str = org.apache.ws.security.util
            .XMLUtils.PrettyDocumentToString(response1Doc);
        System.out.println("\n" + response1Str + "\n");

        // Лог
        //SMEVSignBodyThenSecurity.saveXml2File(response1Doc,
        //    TRUST_DIR + "SOAP_gmp_response_1_id.xml", true);

        // 5. Извлечение идентификатора пакета из первого ответа.

        NodeList nodes = response1Doc.getElementsByTagNameNS(
            "http://roskazna.ru/gisgmp/xsd/116/ErrInfo", "ResultData");

        if (nodes.getLength() == 0) {
            throw new Exception("ResultData no found.");
        }

        Element resultData = (Element) nodes.item(0);
        String packageId = resultData.getFirstChild().getNodeValue();

        System.out.println("\n$$$ Package ID: " + packageId + " $$$\n");
        Thread.sleep(5000); // задержка

        // 6. Чтение шаблона второго запроса, подстановка
        // packageId в нужное поле.

        Document requestIdTemplateDoc = parseFile(TRUST_DIR +
            "SOAP_etalon_pay_response_1_id.xml");

        nodes = requestIdTemplateDoc.getElementsByTagNameNS(
            "http://roskazna.ru/gisgmp/xsd/116/PackageStatusRequest",
                "PackageID");

        if (nodes.getLength() == 0) {
            throw new Exception("PackageID no found.");
        }

        Element packageID = (Element) nodes.item(0);
        packageID.setTextContent(packageId);

        // Лог
        //SMEVSignBodyThenSecurity.saveXml2File(requestIdTemplateDoc,
        //    TRUST_DIR + "SOAP_gmp_response_1_id_combined.xml", true);

        // 7. Подпись второго запроса с помощью ключевого контейнера
        // с сертификатом, выпущенным в аккредитованном УЦ.

        // Подпись и лог
        SMEVSignBodyThenSecurity.
            signSecurity(requestIdTemplateDoc, WORK_DIR +
                "SOAP_gmp_response_1_id_combined_with_security.xml",
                    securityAlias, securityPassword);

        // 8. Отправка второго запроса для проверки обработки документа.

        byte[] requestIdDocBin = Array.readFile(WORK_DIR +
            "SOAP_gmp_response_1_id_combined_with_security.xml");

        InputStream signedRequestIdDocInput = new ByteArrayInputStream(requestIdDocBin);
        Message signedRequestIdDocMsg = new Message(signedRequestIdDocInput);

        gmpService = new SmevGISGMPService_ServiceLocator();
        gmpPort = gmpService.getSmevGISGMPServiceSOAP();

        SOAPEnvelope response2Env = gmpPort.GISGMPTransferMsg(signedRequestIdDocMsg);

        // 9. Обработка ответа. Извлечение поля ResultCode.

        System.out.println("\nReceived response #2...\n");
        Document response2Doc = response2Env.getOwnerDocument();

        // Лог
        String response2Str = org.apache.ws.security.util
            .XMLUtils.PrettyDocumentToString(response2Doc);
        System.out.println("\n" + response2Str + "\n");

        // Лог
        //SMEVSignBodyThenSecurity.saveXml2File(response2Doc,
        //    TRUST_DIR + "SOAP_gmp_response_2_result.xml", true);

        nodes = response2Doc.getElementsByTagNameNS(
            "http://roskazna.ru/gisgmp/xsd/116/ErrInfo", "ResultCode");

        if (nodes.getLength() == 0) {
            throw new Exception("ResultCode no found.");
        }

        Element resultCode = (Element) nodes.item(0);
        String resultCodeValue = resultCode.getFirstChild().getNodeValue();

        System.out.println("\nFinal (XAdES-T?) result code: " + resultCodeValue + "\n");
    }

}
