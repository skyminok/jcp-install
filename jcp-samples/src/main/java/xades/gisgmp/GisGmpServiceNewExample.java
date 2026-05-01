    /**
 * $RCSfileGisGmpServiceCombinedExample.java,v $
 * version $Revision: 36379 $
 * created 19.06.2015 15:10 by afevma
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
/**
 * $RCSfileGisGmpServiceCombinedExample.java,v $
 * version $Revision: 36379 $
 * created 19.06.2015 15:10 by afevma
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

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import ru.CryptoPro.XAdES.util.XMLUtils;
import ru.gosuslugi.smev.rev120315.*;
import ru.roskazna.gisgmp._02000000.smevgisgmpservice.SmevGISGMPService;
import ru.roskazna.gisgmp._02000000.smevgisgmpservice.SmevGISGMPService_Service;
import ru.roskazna.gisgmp.xsd._116.budgetindex.BudgetIndexType;
import ru.roskazna.gisgmp.xsd._116.errinfo.ResultInfo;
import ru.roskazna.gisgmp.xsd._116.message.RequestMessageType;
import ru.roskazna.gisgmp.xsd._116.message.ResponseMessageType;
import ru.roskazna.gisgmp.xsd._116.organization.AccountType;
import ru.roskazna.gisgmp.xsd._116.organization.BankType;
import ru.roskazna.gisgmp.xsd._116.packagestatusrequest.PackageStatusRequestType;
import ru.roskazna.gisgmp.xsd._116.paymentinfo.PaymentIdentificationDataType;
import ru.roskazna.gisgmp.xsd._116.paymentinfo.PaymentType;
import ru.roskazna.gisgmp.xsd._116.pgu_importrequest.ImportRequestType;
import ru.roskazna.gisgmp.xsd._116.ticket.TicketType;
import wss4j.examples.SMEVSignBodyThenSecurity;
import xades.XAdES4JSignVerify;
import xades.XAdESSignVerify;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.GostXAdESUtility;
import xades.util.XMLUtility;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Пример создания документа, подписи (xades-T) и отправки
 * документа в сервис ГИС ГМП.
 * Используются классы, созданные по xsd-схемам, и JAXBContext.
 * Отправка сообщения в сервис ГИС ГМП (с параллельным созданием security header)
 * осуществеляется при помощи  WSS4JOutInterceptor.\
 * Для работы требуется настроить файл конфигурации crypto.properties, прописав в нем контейнер с аккридованным
 * сертификатом ("GisSignContainer" с паролем "1"), а также в качестве реализации Merlin указать
 * wss4j.wss4j1_6.ws.security.components.crypto.MerlinEx.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @see xades.XAdESExample
 * @see SMEVSignBodyThenSecurity
 */
public class GisGmpServiceNewExample extends GostXAdESUtility {

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        // время указываем фиксированное
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(2017, 12, 25, 16, 37, 30, 0, DatatypeConstants.FIELD_UNDEFINED);
        date2 = date2.normalize();
        String TIMEFORMATE = "25122017"; // для формирования SystemIdentificator.

        // 1. Создание блока
        // <Message>
        //  ...
        // </Message>

        ru.gosuslugi.smev.rev120315.ObjectFactory revObjectFactory =
                new ru.gosuslugi.smev.rev120315.ObjectFactory();

        MessageType messageType = revObjectFactory.createMessageType();

        OrgExternalType senderType = revObjectFactory.createOrgExternalType();
        senderType.setName("ИС АН 1");
        senderType.setCode("AN0000001");

        messageType.setSender(senderType);

        OrgExternalType recipientType = revObjectFactory.createOrgExternalType();
        recipientType.setCode("RKZN35001");
        recipientType.setName("Казначейство России");

        messageType.setRecipient(recipientType);

        messageType.setServiceName("GISGMP");
        messageType.setTypeCode(TypeCodeType.GFNC);
        messageType.setStatus(StatusType.REQUEST);
        messageType.setDate(date2);
        messageType.setExchangeType("6");
        messageType.setTestMsg("test");

        // 2. Создание подблоков
        // <MessageData>
        //  ...
        // </MessageData>

        // a. Создание блока
        // <FinalPayment>
        //  ...
        // </FinalPayment>

        ru.roskazna.gisgmp.xsd._116.paymentinfo.ObjectFactory paymentObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.paymentinfo.ObjectFactory();

        PaymentType paymentType = paymentObjectFactory.createPaymentType();
        paymentType.setId(SIGNING_ID);
        paymentType.setSupplierBillID("0");
        paymentType.setNarrative("Оплата");
        paymentType.setAmount(BigInteger.valueOf(1000));
        paymentType.setPaymentDate(date2);
        paymentType.setReceiptDate(date2);

        ru.roskazna.gisgmp.xsd._116.budgetindex.ObjectFactory budgetObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.budgetindex.ObjectFactory();

        BudgetIndexType budgetIndexType = budgetObjectFactory.createBudgetIndexType();
        budgetIndexType.setStatus("01");
        budgetIndexType.setPurpose("0");
        budgetIndexType.setTaxPeriod("0");
        budgetIndexType.setTaxDocNumber("0");
        budgetIndexType.setTaxDocDate("0");
        budgetIndexType.setPaymentType("0");

        paymentType.setBudgetIndex(budgetIndexType);

        ru.roskazna.gisgmp.xsd._116.organization.ObjectFactory organizationObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.organization.ObjectFactory();

        String strBIK = "044525716";
        BankType bankType = organizationObjectFactory.createBankType();
        bankType.setName("ВТБ24");
        bankType.setBIK(strBIK);
        bankType.setCorrespondentBankAccount("40602810000380000020");

        PaymentIdentificationDataType paymentIdentificationDataType =
                paymentObjectFactory.createPaymentIdentificationDataType();

        paymentIdentificationDataType.setBank(bankType);
        // тут изменяем идентификатор платежа
        String SystemID = "87655303";
        String SystemIDFull = "1" + strBIK + "452571" + TIMEFORMATE + SystemID;
        paymentIdentificationDataType.setSystemIdentifier(SystemIDFull);

        paymentType.setPaymentIdentificationData(paymentIdentificationDataType);
        PaymentType.AccDoc accdoc = paymentObjectFactory.createPaymentTypeAccDoc();
        accdoc.setAccDocNo("0");
        accdoc.setAccDocDate(date2);
        paymentType.setAccDoc(accdoc);

        PaymentType.Payer payer = paymentObjectFactory.createPaymentTypePayer();
        payer.setPayerIdentifier("0100000000023456789012643");
        payer.setPayerName("Иванов Иван Николаевич");

        paymentType.setPayer(payer);

        PaymentType.Payee payee = paymentObjectFactory.createPaymentTypePayee();
        payee.setPayeeName("ГИБДД");
        payee.setPayeeINN("3543655766");
        payee.setPayeeKPP("354365576");

        AccountType accountType = organizationObjectFactory.createAccountType();
        accountType.setAccountNumber("40602810000380000020");

        BankType accountBankType = organizationObjectFactory.createBankType();
        accountBankType.setName("Альфа");
        accountBankType.setBIK("044525716");
        accountBankType.setCorrespondentBankAccount("30101810100000000716");

        accountType.setBank(accountBankType);
        payee.setPayeeBankAcc(accountType);

        paymentType.setPayee(payee);

        PaymentType.ChangeStatus changeStatus = paymentObjectFactory.createPaymentTypeChangeStatus();
        changeStatus.setMeaning("1");

        paymentType.setChangeStatus(changeStatus);
        paymentType.setKBK("18851111111111111113");
        paymentType.setOKTMO("12345673");

        // b. Создание блока
        // <Document>
        //      <FinalPayment>
        //          ...
        //      </FinalPayment>
        // </Document>

        ru.roskazna.gisgmp.xsd._116.pgu_importrequest.ObjectFactory importRequestObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.pgu_importrequest.ObjectFactory();

        ImportRequestType.Package.Document document =
                importRequestObjectFactory.createImportRequestTypePackageDocument();

        document.setFinalPayment(paymentType);

        // c. Создание блока
        // <Package>
        //      <Document>
        //          <FinalPayment>
        //              ...
        //          </FinalPayment>
        //      </Document>
        // </Package>

        ImportRequestType.Package packageType = importRequestObjectFactory.createImportRequestTypePackage();
        packageType.getDocument().add(document);

        // d. Создание блока
        // <ImportRequest>
        //      <Package>
        //          <Document>
        //              <FinalPayment>
        //                  ...
        //              </FinalPayment>
        //          </Document>
        //      </Package>
        // </ImportRequest>

        ImportRequestType importRequestType = importRequestObjectFactory.createImportRequestType();
        importRequestType.setPackage(packageType);

        ru.roskazna.gisgmp.xsd._116.messagedata.ObjectFactory messageDataObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.messagedata.ObjectFactory();

        JAXBElement<ImportRequestType> importRequestObject =
                messageDataObjectFactory.createImportRequest(importRequestType);

        // e. Создание блока
        // <RequestMessage>
        //      <ImportRequest>
        //          <Package>
        //              <Document>
        //                  <FinalPayment>
        //                      ...
        //                  </FinalPayment>
        //              </Document>
        //          </Package>
        //      </ImportRequest>
        // </RequestMessage>

        ru.roskazna.gisgmp.xsd._116.message.ObjectFactory messageObjectFactory =
                new ru.roskazna.gisgmp.xsd._116.message.ObjectFactory();

        RequestMessageType requestMessageType = messageObjectFactory.createRequestMessageType();
        requestMessageType.setSenderIdentifier(SENDER_EXAMPLE_1);
        requestMessageType.setSenderRole(SENDER_ROLE);
        requestMessageType.setTimestamp(date2);
        requestMessageType.setId(REQUEST_MESSAGE_ID);
        requestMessageType.setRequestMessageData(importRequestObject);

        JAXBElement<RequestMessageType> requestMessageObject =
                messageObjectFactory.createRequestMessage(requestMessageType);

        // f. Создание блока
        // <AppData>
        //      <RequestMessage>
        //          <ImportRequest>
        //              <Package>
        //                  <Document>
        //                      <FinalPayment>
        //                          ...
        //                      </FinalPayment>
        //                  </Document>
        //              </Package>
        //          </ImportRequest>
        //      </RequestMessage>
        // </AppData>

        AppDataType appDataType = revObjectFactory.createAppDataType();
        appDataType.getAny().add(requestMessageObject);

        // g. Создание блока
        // <MessageData>
        //      <AppData>
        //          <RequestMessage>
        //              <ImportRequest>
        //                  <Package>
        //                      <Document>
        //                          <FinalPayment>
        //                              ...
        //                          </FinalPayment>
        //                      </Document>
        //                  </Package>
        //              </ImportRequest>
        //          </RequestMessage>
        //      </AppData>
        // </MessageData>

        MessageDataType messageDataType = revObjectFactory.createMessageDataType();
        messageDataType.setAppData(appDataType);

        // 3. Создание общего блока
        // <GISGMPTransferMsg>
        //      <Message>
        //          ...
        //      </Message>
        //      <MessageData>
        //          ...
        //      </MessageData>
        // </GISGMPTransferMsg>

        BaseMessageType baseMessageType = revObjectFactory.createBaseMessageType();
        baseMessageType.setMessage(messageType);
        baseMessageType.setMessageData(messageDataType);

        ru.roskazna.gisgmp._02000000.smevgisgmpservice.ObjectFactory serviceObjectFactory =
                new ru.roskazna.gisgmp._02000000.smevgisgmpservice.ObjectFactory();

        JAXBElement<BaseMessageType> documentObject =
                serviceObjectFactory.createGISGMPTransferMsg(baseMessageType);

        // 4. Конвертация документа из BaseMessageType в SOAP XML (для того, чтобы добавить подпись)

        JAXBContext jaxbBaseMessageType = JAXBContext.newInstance(
                ru.roskazna.gisgmp.xsd._116.paymentinfo.ObjectFactory.class,
                ru.roskazna.gisgmp.xsd._116.message.ObjectFactory.class,
                ru.roskazna.gisgmp.xsd._116.messagedata.ObjectFactory.class,
                ru.gosuslugi.smev.rev120315.ObjectFactory.class,
                org.w3._2004._08.xop.include.Include.class
        );

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setNamespaceAware(true);

        Document resultDocument = dbf.newDocumentBuilder().newDocument();

        Marshaller marshaller = jaxbBaseMessageType.createMarshaller();
        Unmarshaller unmarshaller = jaxbBaseMessageType.createUnmarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        try (FileOutputStream os = new FileOutputStream(WORK_DIR + "out.xml")) {
            marshaller.marshal(documentObject, os); // дамп корня
        }
        marshaller.marshal(documentObject, resultDocument);

        // 5. Создание SOAP Message

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();
        msg.getSOAPBody().addDocument(resultDocument);

        SOAPPart soapPart = msg.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        soapEnvelope.addNamespaceDeclaration(WSConstants.WSU_PREFIX, WSConstants.WSU_NS);

        SOAPBody soapBody = soapEnvelope.getBody();
        soapBody.addAttribute(soapEnvelope.createName("Id",
                WSConstants.WSU_PREFIX, WSConstants.WSU_NS), "body");

        try (FileOutputStream os = new FileOutputStream(WORK_DIR + "soap.xml")) {
            msg.writeTo(os); // дамп SOAP
        }

        // 6. Подпись SOAP документа XAdES-T
        // Создание XAdES-T подписи с помощью тестового ключа
        // (сертификат из внешнего тестового УЦ) и внешней тестовой
        // TSP службы (http://www.cryptopro.ru:80/tsp/).


        IXAdESConfig XAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2012_S;
        byte[] finalPaymentDoc = Array.readFile(WORK_DIR + "soap.xml");

        Document signedXAdESTDoc = XAdESSignVerify.sign(new Integer[]
                        {XAdESType.XAdES_T}, XAdESConfigTestSignKey, finalPaymentDoc,
                WORK_DIR, SIGNING_ID, new ITransform[]{new EnvelopedTransform()},
                null, false, "http://www.cryptopro.ru:80/tsp/", null);

        XAdESSignVerify.verify(signedXAdESTDoc, new Integer[]{XAdESType.XAdES_T},
                null, null, false, 1);

        XAdES4JSignVerify.verify(XAdESConfigTestSignKey, signedXAdESTDoc, false);

        XMLUtils.saveXml2File(signedXAdESTDoc,
                WORK_DIR + "soap_signed.xml", false);

        //7. Из подписанного SOAP документа нужно получить обратно объект BaseMessageType (с проставленной подписью )
        // для последующей отправки сервису ГИС.

        SOAPMessage signedMsg;
        try (FileInputStream is = new FileInputStream(WORK_DIR + "soap_signed.xml")) {
            signedMsg = factory.createMessage(new MimeHeaders(), is);
        }

        // Получаем body
        NodeList messnodes = signedMsg.getSOAPPart().getEnvelope().getBody().getChildNodes();
        Element messnode = (Element) messnodes.item(0);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(messnode);
        StreamResult fileResult = new StreamResult(new File(WORK_DIR + "soap_body_signed.xml"));
        transformer.transform(source, fileResult);

        // получаем узлы, соответствующие объектам Message и MessageData
        NodeList nextnodes = messnode.getChildNodes();
        for (int i = 0; i < nextnodes.getLength(); i++) {
            Node next = (Node) nextnodes.item(i);
            if (next.getNodeName().contains("Message") && !next.getNodeName().contains("MessageData")) {
                source = new DOMSource(next);
                fileResult = new StreamResult(new File(WORK_DIR + "soap_message.xml"));
                transformer.transform(source, fileResult);
            } else if (next.getNodeName().contains("MessageData")) {
                source = new DOMSource(next);
                fileResult = new StreamResult(new File(WORK_DIR + "soap_messagedata.xml"));
                transformer.transform(source, fileResult);
            }
        }

        // получаем из узлов объекты Message и MessageData и формируем BaseMessageType
        JAXBElement<MessageType> signedType = (JAXBElement<MessageType>) unmarshaller.unmarshal(new File(WORK_DIR + "soap_message.xml"));
        JAXBElement<MessageDataType> signedDataType = (JAXBElement<MessageDataType>) unmarshaller.unmarshal(new File(WORK_DIR + "soap_messagedata.xml"));

        BaseMessageType signedBase = new BaseMessageType();
        signedBase.setMessage(signedType.getValue());
        signedBase.setMessageData(signedDataType.getValue());


        // 8. Отправка документа в сервис. Для этого используем WSS4JOutInterceptor.

        SmevGISGMPService_Service gmpService = new SmevGISGMPService_Service();
        SmevGISGMPService gmpPort = gmpService.getSmevGISGMPServiceSOAP();

        // таблица настроек для WSS4JOutInterceptor.
        // Пароль на контейнер возвращается через callback класс ClientPasswordCallback
        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        outProps.put(WSHandlerConstants.USER, "client");
        outProps.put(WSHandlerConstants.SIG_PROP_FILE, "WebContent/crypto.properties");
        outProps.put(WSHandlerConstants.SIGNATURE_USER, "GisSignContainer");
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName());
        outProps.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
        outProps.put(WSHandlerConstants.SIG_ALGO, Consts.URI_GOST_SIGN);
        outProps.put(WSHandlerConstants.SIG_DIGEST_ALGO, Consts.URI_GOST_DIGEST);
        outProps.put(WSHandlerConstants.ACTOR, "http://smev.gosuslugi.ru/actors/smev");
        outProps.put(WSHandlerConstants.MUST_UNDERSTAND, "false");
        outProps.put(WSHandlerConstants.IS_BSP_COMPLIANT, "false");

        WSSConfig.setAddJceProviders(false); // запрещаем использовать JCE (без этого наш провайдер не виден)

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        final org.apache.cxf.endpoint.Client client = ClientProxy.getClient(gmpPort);
        client.getOutInterceptors().add(wssOut);

        // Оправляем в ГИС и получаем ответ
        BaseMessageType checkResponse = gmpPort.gisgmpTransferMsg(signedBase);

        // 9. Обработка ответа и извлечение package Id
        MessageDataType resultMessageData = checkResponse.getMessageData();
        List list = resultMessageData.getAppData().getAny();
        JAXBElement<ResponseMessageType> responseMessageTypeObject = (JAXBElement<ResponseMessageType>) list.get(0);
        ResponseMessageType responseMessageType = responseMessageTypeObject.getValue();
        JAXBElement<TicketType> ticketInfo = (JAXBElement<TicketType>) responseMessageType.getResponseMessageData();
        ResultInfo resultInfo = ticketInfo.getValue().getRequestProcessResult();
        String resultCode = resultInfo.getResultCode();
        String packageId = resultInfo.getResultData();

        System.out.println("\n$$$Answer recieved: Result: " + resultCode + " Package ID: " + packageId + " $$$\n");
        Thread.sleep(5000); // задержка

        // 10. Составление запроса

        // a. Создание блока
        // <PackageStatusRequest>
        //      ...
        // </PackageStatusRequest>

        ru.roskazna.gisgmp.xsd._116.packagestatusrequest.ObjectFactory
                packageStatusRequestObjectFactory = new ru.roskazna.gisgmp.xsd._116
                .packagestatusrequest.ObjectFactory();

        PackageStatusRequestType packageStatusRequestType =
                packageStatusRequestObjectFactory.createPackageStatusRequestType();

        packageStatusRequestType.setPackageID(packageId);

        JAXBElement<PackageStatusRequestType> packageStatusRequestElement =
                messageDataObjectFactory.createPackageStatusRequest(packageStatusRequestType);

        // b. Создание блока
        // <RequestMessage>
        //      <PackageStatusRequest>
        //          ...
        //      </PackageStatusRequest>
        // </RequestMessage>

        requestMessageType = messageObjectFactory.createRequestMessageType();
        requestMessageType.setSenderIdentifier(SENDER_EXAMPLE_2);
        requestMessageType.setSenderRole(SENDER_ROLE);
        requestMessageType.setTimestamp(date2);
        requestMessageType.setId(REQUEST_MESSAGE_ID);
        requestMessageType.setRequestMessageData(packageStatusRequestElement);

        requestMessageObject = messageObjectFactory.createRequestMessage(requestMessageType);

        // c. Создание блока
        // <AppData>
        //      <RequestMessage>
        //          <ImportRequest>
        //              <Package>
        //                  <Document>
        //                      <FinalPayment>
        //                      </FinalPayment>
        //                  </Document>
        //              </Package>
        //          </ImportRequest>
        //      </RequestMessage>
        // </AppData>

        appDataType = revObjectFactory.createAppDataType();
        appDataType.getAny().add(requestMessageObject);

        // d. Создание блока
        // <MessageData>
        //      ...
        // </MessageData>

        messageDataType = revObjectFactory.createMessageDataType();
        messageDataType.setAppData(appDataType);

        // e. Создание общего блока
        // <GISGMPTransferMsg>
        //      <Message>
        //          ...
        //      </Message>
        //      <MessageData>
        //          ...
        //      </MessageData>
        // </GISGMPTransferMsg>

        baseMessageType = revObjectFactory.createBaseMessageType();
        baseMessageType.setMessage(messageType);
        baseMessageType.setMessageData(messageDataType);

        // 14. Отправка запроса в сервис
        checkResponse = gmpPort.gisgmpTransferMsg(baseMessageType);

        // 15. Обработка ответа, получение результата.
        resultMessageData = checkResponse.getMessageData();
        list = resultMessageData.getAppData().getAny();
        responseMessageTypeObject = (JAXBElement<ResponseMessageType>) list.get(0);
        responseMessageType = responseMessageTypeObject.getValue();
        ticketInfo = (JAXBElement<TicketType>) responseMessageType.getResponseMessageData();
        resultInfo = ticketInfo.getValue().getRequestProcessResult();

        // данные о запросе
        if (resultInfo!=null) {
            resultCode = resultInfo.getResultCode();
            String resultCodeValue = resultInfo.getResultData();
            System.out.println("\nFinal (XAdES-T?) result code: " + resultCode + " result:" + resultCodeValue + "\n");
        }
        else{ // данные о пакете
            TicketType.PackageProcessResult packageResult = ticketInfo.getValue().getPackageProcessResult();
            List<TicketType.PackageProcessResult.EntityProcessResult> entity = packageResult.getEntityProcessResult();
            if (entity.size() == 0)
                System.out.println("No package process results!!!");
            else
                for (int i = 0; i < entity.size(); i++){
                    TicketType.PackageProcessResult.EntityProcessResult next = entity.get(i);
                    if (next.getEntityId().equalsIgnoreCase(SIGNING_ID)){
                        System.out.println("Found package result: \nFinal (XAdES-T?) result code: " +
                                next.getResultCode()+ "\n" + next.getResultDescription());
                    }

                }
        }

    }
}
