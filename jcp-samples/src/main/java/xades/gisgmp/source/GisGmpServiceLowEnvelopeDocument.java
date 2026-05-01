/**
 * $RCSfileGisGmpServiceLowEnvelopeDocument.java,v $
 * version $Revision: 36379 $
 * created 09.09.2015 9:33 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xades.gisgmp.source;

import org.apache.ws.security.WSConstants;

import xades.util.IXAdESCommon;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

/**
 * Класс для формирования сообщения для ГИС ГМП и
 * запроса результата обработки.
 * Для успешной работы (чтобы служба вернула код 0), нужно каждый раз
 * изменять значение строки SystemID.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class GisGmpServiceLowEnvelopeDocument implements IXAdESCommon {

    private static final String NS_GLOB_PREFIX = "";
    private static final String NS_GLOB = "http://roskazna.ru/gisgmp/xsd/116/PaymentInfo";

    private static final String NS_REV_PREFIX = "rev";
    private static final String NS_REV = "http://smev.gosuslugi.ru/rev120315";

    private static final String NS_SMEV_PREFIX = "smev";
    private static final String NS_SMEV = "http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/";

    private static final String NS_BGI_PREFIX = "bgi";
    private static final String NS_BGI = "http://roskazna.ru/gisgmp/xsd/116/BudgetIndex";

    private static final String NS_COM_PREFIX = "com";
    private static final String NS_COM = "http://roskazna.ru/gisgmp/xsd/116/Common";

    private static final String NS_DS_PREFIX = "ds";
    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";

    private static final String NS_GISGMP_PREFIX = "gisgmp";
    private static final String NS_GISGMP = "http://roskazna.ru/gisgmp/xsd/116/Message";

    private static final String NS_MSGD_PREFIX = "msgd";
    private static final String NS_MSGD = "http://roskazna.ru/gisgmp/xsd/116/MessageData";

    private static final String NS_N1_PREFIX = "n1";
    private static final String NS_N1 = "http://www.altova.com/samplexml/other-namespace";

    private static final String NS_PGU_PREFIX = "pgu";
    private static final String NS_PGU = "http://roskazna.ru/gisgmp/xsd/116/PGU_ImportRequest";

    private static final String NS_ORG_PREFIX = "org";
    private static final String NS_ORG = "http://roskazna.ru/gisgmp/xsd/116/Organization";

    private static final String NS_XSI_PREFIX = "xsi";
    private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String NS_INC_PREFIX = "inc";
    private static final String NS_INC = "http://www.w3.org/2004/08/xop/include";

    private static final String NS_PDR_PREFIX = "pdr";
    private static final String NS_PDR = "http://roskazna.ru/gisgmp/xsd/116/PGU_DataRequest";

    private static final String NS_PSR_PREFIX = "psr";
    private static final String NS_PSR = "http://roskazna.ru/gisgmp/xsd/116/PackageStatusRequest";

    // Время запроса (зафиксировано).
    private static final String TIMESTAMP_DATE_TIME = "2017-12-01T10:25:00.0Z";
    // Время запроса (зафиксировано).
    private static final String TIMESTAMP_DATE = "2017-12-01";
    // время в формате ДДММГГГГ
    private static final String TIMEFORMATE= "01122017";

    /**
     * Создание документа из частей.
     *
     * @return документ.
     * @throws Exception
     */
    public static byte[] createDocument() throws Exception {

        // Фабрика документов.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setNamespaceAware(true);

        // Фабрика сообщения.
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage sm = mf.createMessage();

        // Сообщение.
        SOAPEnvelope envelope = sm.getSOAPPart().getEnvelope();
        //envelope.setPrefix(SOAP_NEW_PREFIX);
        //envelope.removeNamespaceDeclaration(SOAP_OLD_PREFIX);
        envelope.addNamespaceDeclaration(WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS); //"wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
        envelope.addNamespaceDeclaration(WSConstants.WSU_PREFIX,  WSConstants.WSU_NS); //"wsu http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        envelope.addNamespaceDeclaration(NS_REV_PREFIX, NS_REV); //rev
        envelope.addNamespaceDeclaration(NS_SMEV_PREFIX, NS_SMEV); //smev

        //SOAPHeader header = sm.getSOAPHeader();
        //header.setPrefix(SOAP_NEW_PREFIX);

        SOAPBody body = sm.getSOAPBody();
        //body.setPrefix(SOAP_NEW_PREFIX);
        body.setAttributeNS(WSConstants.WSU_NS, "wsu:Id", "body");//"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

        // 1. Создание общего блока
        // <GISGMPTransferMsg>
        //      <Message>
        //          ...
        //      </Message>
        //      <MessageData>
        //          ...
        //      </MessageData>
        // </GISGMPTransferMsg>

        SOAPBodyElement GISGMPTransferMsg = body.addBodyElement(
            envelope.createName("GISGMPTransferMsg", NS_SMEV_PREFIX, NS_SMEV));

        // 1.1. Создание блока
        // <Message>
        //  ...
        // </Message>

        SOAPElement Message = GISGMPTransferMsg.addChildElement(
            envelope.createName("Message", NS_REV_PREFIX, NS_REV));

        SOAPElement Sender = Message.addChildElement(envelope.createName("Sender", NS_REV_PREFIX, NS_REV));

        SOAPElement Code = Sender.addChildElement(envelope.createName("Code", NS_REV_PREFIX, NS_REV));
        Code.setTextContent("AN0000001");

        SOAPElement Name = Sender.addChildElement(envelope.createName("Name", NS_REV_PREFIX, NS_REV));
        Name.setTextContent("ИС АН 1");

        SOAPElement recipient = Message.addChildElement(envelope.createName("Recipient", NS_REV_PREFIX, NS_REV));

        Code = recipient.addChildElement(envelope.createName("Code", NS_REV_PREFIX, NS_REV));
        Code.setTextContent("RKZN35001");

        Name = recipient.addChildElement(envelope.createName("Name", NS_REV_PREFIX, NS_REV));
        Name.setTextContent("Казначейство России");

        SOAPElement serviceName = Message.addChildElement(envelope.createName("ServiceName", NS_REV_PREFIX, NS_REV));
        serviceName.setTextContent("GISGMP");

        SOAPElement typeCode = Message.addChildElement(envelope.createName("TypeCode", NS_REV_PREFIX, NS_REV));
        typeCode.setTextContent("GFNC");

        SOAPElement status = Message.addChildElement(envelope.createName("Status", NS_REV_PREFIX, NS_REV));
        status.setTextContent("REQUEST");

        SOAPElement date = Message.addChildElement(envelope.createName("Date", NS_REV_PREFIX, NS_REV));
        date.setTextContent(TIMESTAMP_DATE_TIME);

        SOAPElement exchangeType = Message.addChildElement(envelope.createName("ExchangeType",NS_REV_PREFIX, NS_REV));
        exchangeType.setTextContent("6");

        SOAPElement testMsg = Message.addChildElement(envelope.createName("TestMsg", NS_REV_PREFIX, NS_REV));
        testMsg.setTextContent("test");

        // 1.2. Создание блока
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

        SOAPElement messageData = GISGMPTransferMsg.addChildElement(
            envelope.createName("MessageData", NS_REV_PREFIX, NS_REV));

        // a. Создание блока
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

        SOAPElement appData = messageData.addChildElement(
            envelope.createName("AppData", NS_REV_PREFIX, NS_REV));

        // b. Создание блока
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

        SOAPElement RequestMessage = appData.addChildElement(
            envelope.createName("RequestMessage", NS_GISGMP_PREFIX, NS_GISGMP));

        RequestMessage.addNamespaceDeclaration(NS_GLOB_PREFIX, NS_GLOB);
        RequestMessage.addNamespaceDeclaration(NS_BGI_PREFIX, NS_BGI);
        RequestMessage.addNamespaceDeclaration(NS_COM_PREFIX, NS_COM);
        RequestMessage.addNamespaceDeclaration(NS_DS_PREFIX, NS_DS);
        RequestMessage.addNamespaceDeclaration(NS_GISGMP_PREFIX, NS_GISGMP);
        RequestMessage.addNamespaceDeclaration(NS_MSGD_PREFIX, NS_MSGD);
        RequestMessage.addNamespaceDeclaration(NS_N1_PREFIX, NS_N1);
        RequestMessage.addNamespaceDeclaration(NS_PGU_PREFIX, NS_PGU);
        RequestMessage.addNamespaceDeclaration(NS_ORG_PREFIX, NS_ORG);
        RequestMessage.addNamespaceDeclaration(NS_XSI_PREFIX, NS_XSI);

        RequestMessage.addAttribute(envelope.createName("Id"), REQUEST_MESSAGE_ID);
        RequestMessage.addAttribute(envelope.createName("senderIdentifier"), SENDER_EXAMPLE_1);
        // по новым правилам - обязательный аттрибут
        RequestMessage.addAttribute(envelope.createName("senderRole"), SENDER_ROLE);
        RequestMessage.addAttribute(envelope.createName("timestamp"), TIMESTAMP_DATE_TIME);

        // c. Создание блока
        // <ImportRequest>
        //      <Package>
        //          <Document>
        //              <FinalPayment>
        //                  ...
        //              </FinalPayment>
        //          </Document>
        //      </Package>
        // </ImportRequest>

        SOAPElement ImportRequest = RequestMessage.addChildElement(
            envelope.createName("ImportRequest", NS_MSGD_PREFIX, NS_MSGD));

        // d. Создание блока
        // <Package>
        //      <Document>
        //          <FinalPayment>
        //              ...
        //          </FinalPayment>
        //      </Document>
        // </Package>

        SOAPElement Package = ImportRequest.addChildElement(
            envelope.createName("Package", NS_PGU_PREFIX, NS_PGU));

        // e. Создание блока
        // <Document>
        //      <FinalPayment>
        //          ...
        //      </FinalPayment>
        // </Document>

        SOAPElement Document = Package.addChildElement(
            envelope.createName("Document", NS_PGU_PREFIX, NS_PGU));

        // f. Создание блока
        // <FinalPayment>
        //  ...
        // </FinalPayment>

        SOAPElement FinalPayment = Document.addChildElement(
            envelope.createName("FinalPayment", NS_GLOB_PREFIX, NS_GLOB));
        FinalPayment.addAttribute(envelope.createName("Id"), SIGNING_ID);

        SOAPElement SupplierBillID = FinalPayment.addChildElement(
            envelope.createName("SupplierBillID", NS_GLOB_PREFIX, NS_GLOB));
        SupplierBillID.setTextContent("0");

        SOAPElement Narrative = FinalPayment.addChildElement(
            envelope.createName("Narrative", NS_GLOB_PREFIX, NS_GLOB));
        Narrative.setTextContent("Оплата");

        SOAPElement Amount = FinalPayment.addChildElement(
            envelope.createName("Amount", NS_GLOB_PREFIX, NS_GLOB));
        Amount.setTextContent("1000");

        // по новым требованиям значение - только дата (без времени)
        SOAPElement PaymentDate = FinalPayment.addChildElement(
            envelope.createName("PaymentDate", NS_GLOB_PREFIX, NS_GLOB));
        PaymentDate.setTextContent(TIMESTAMP_DATE);

        // по новым требованиям должна присутсвовать дата поступления - только дата (без времени)
        SOAPElement ReceiptDate = FinalPayment.addChildElement(
                envelope.createName("ReceiptDate", NS_GLOB_PREFIX, NS_GLOB));
        ReceiptDate.setTextContent(TIMESTAMP_DATE);


        SOAPElement BudgetIndex = FinalPayment.addChildElement(
            envelope.createName("BudgetIndex", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement Status = BudgetIndex.addChildElement(
            envelope.createName("Status", NS_BGI_PREFIX, NS_BGI));
        Status.setTextContent("01");

        SOAPElement Purpose = BudgetIndex.addChildElement(
            envelope.createName("Purpose", NS_BGI_PREFIX, NS_BGI));
        Purpose.setTextContent("0");

        SOAPElement TaxPeriod = BudgetIndex.addChildElement(
            envelope.createName("TaxPeriod", NS_BGI_PREFIX, NS_BGI));
        TaxPeriod.setTextContent("0");

        SOAPElement TaxDocNumber = BudgetIndex.addChildElement(
            envelope.createName("TaxDocNumber", NS_BGI_PREFIX, NS_BGI));
        TaxDocNumber.setTextContent("0");

        SOAPElement TaxDocDate = BudgetIndex.addChildElement(
            envelope.createName("TaxDocDate", NS_BGI_PREFIX, NS_BGI));
        TaxDocDate.setTextContent("0");

        SOAPElement PaymentType = BudgetIndex.addChildElement(
            envelope.createName("PaymentType", NS_BGI_PREFIX, NS_BGI));
        PaymentType.setTextContent("0");

        SOAPElement PaymentIdentificationData = FinalPayment.addChildElement(
            envelope.createName("PaymentIdentificationData", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement Bank = PaymentIdentificationData.addChildElement(
            envelope.createName("Bank", NS_GLOB_PREFIX, NS_GLOB));

        Name = Bank.addChildElement(envelope.createName("Name", NS_ORG_PREFIX, NS_ORG));
        Name.setTextContent("ВТБ24");

        String strBIK = "044525716";
        SOAPElement BIK = Bank.addChildElement(envelope.createName("BIK", NS_ORG_PREFIX, NS_ORG));
        BIK.setTextContent(strBIK);

        SOAPElement CorrespondentBankAccount = Bank.addChildElement(
            envelope.createName("CorrespondentBankAccount", NS_ORG_PREFIX, NS_ORG));
        CorrespondentBankAccount.setTextContent("40602810000380000020");

        SOAPElement SystemIdentifier = PaymentIdentificationData.addChildElement(
            envelope.createName("SystemIdentifier", NS_GLOB_PREFIX, NS_GLOB));

        String SystemID = "12124015";
        String SystemIDFull = "1" + strBIK + "452571" + TIMEFORMATE + SystemID;
        SystemIdentifier.setTextContent(SystemIDFull);

        SOAPElement AccDoc = FinalPayment.addChildElement(
                envelope.createName("AccDoc", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement AccDocNo = AccDoc.addChildElement(envelope.createName("AccDocNo", NS_GLOB_PREFIX, NS_GLOB));
        AccDocNo.setTextContent("0");

        SOAPElement AccDocDate = AccDoc.addChildElement(envelope.createName("AccDocDate", NS_GLOB_PREFIX, NS_GLOB));
        AccDocDate.setTextContent(TIMESTAMP_DATE);

        SOAPElement Payer = FinalPayment.addChildElement(
            envelope.createName("Payer", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement PayerIdentifier = Payer.addChildElement(
            envelope.createName("PayerIdentifier", NS_COM_PREFIX, NS_COM));
        PayerIdentifier.setTextContent("0100000000023456789012643");

        SOAPElement PayerName = Payer.addChildElement(
            envelope.createName("PayerName", NS_GLOB_PREFIX, NS_GLOB));
        PayerName.setTextContent("Иванов Иван Николаевич");

        SOAPElement Payee = FinalPayment.addChildElement(
            envelope.createName("Payee", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement PayeeName = Payee.addChildElement(
            envelope.createName("PayeeName", NS_GLOB_PREFIX, NS_GLOB));
        PayeeName.setTextContent("ГИБДД");

        SOAPElement payeeINN = Payee.addChildElement(
            envelope.createName("payeeINN", NS_GLOB_PREFIX, NS_GLOB));
        payeeINN.setTextContent("3543655766");

        SOAPElement payeeKPP = Payee.addChildElement(
            envelope.createName("payeeKPP", NS_GLOB_PREFIX, NS_GLOB));
        payeeKPP.setTextContent("354365576");

        SOAPElement PayeeBankAcc = Payee.addChildElement(
            envelope.createName("PayeeBankAcc", NS_GLOB_PREFIX, NS_GLOB));

        SOAPElement AccountNumber = PayeeBankAcc.addChildElement(
            envelope.createName("AccountNumber", NS_ORG_PREFIX, NS_ORG));
        AccountNumber.setTextContent("40602810000380000020");

        Bank = PayeeBankAcc.addChildElement(envelope.createName("Bank", NS_ORG_PREFIX, NS_ORG));

        Name = Bank.addChildElement(envelope.createName("Name", NS_ORG_PREFIX, NS_ORG));
        Name.setTextContent("Альфа");

        BIK = Bank.addChildElement(envelope.createName("BIK", NS_ORG_PREFIX, NS_ORG));
        BIK.setTextContent("044525716");

        CorrespondentBankAccount = Bank.addChildElement(
            envelope.createName("CorrespondentBankAccount", NS_ORG_PREFIX, NS_ORG));
        CorrespondentBankAccount.setTextContent("30101810100000000716");

        SOAPElement ChangeStatus = FinalPayment.addChildElement(
            envelope.createName("ChangeStatus", NS_GLOB_PREFIX, NS_GLOB));

        ChangeStatus.addAttribute(envelope.createName("meaning"), "1");

        SOAPElement KBK = FinalPayment.addChildElement(
            envelope.createName("KBK", NS_GLOB_PREFIX, NS_GLOB));
        KBK.setTextContent("18851111111111111113");

        SOAPElement OKTMO = FinalPayment.addChildElement(
            envelope.createName("OKTMO", NS_GLOB_PREFIX, NS_GLOB));
        OKTMO.setTextContent("12345673");

        try (FileOutputStream os = new FileOutputStream(WORK_DIR + "stub.xml")) {
            sm.writeTo(os); // лог
        }

        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();
        sm.writeTo(documentOut);
        documentOut.close();

        return documentOut.toByteArray();

    }

    /**
     * Создание запроса обработки результата из частей.
     *
     * @param packageId Идентификатор запрашиваемого документа.
     * @return запрос.
     * @throws Exception
     */
    public static byte[] createRequest(String packageId) throws Exception {

        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage sm = mf.createMessage();

        SOAPEnvelope envelope = sm.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(WSConstants.WSSE_PREFIX, WSConstants.WSSE_NS);
        envelope.addNamespaceDeclaration(WSConstants.WSU_PREFIX,  WSConstants.WSU_NS);
        envelope.addNamespaceDeclaration("xd", NS_DS);
        envelope.addNamespaceDeclaration(NS_REV_PREFIX, NS_REV);
        envelope.addNamespaceDeclaration(NS_SMEV_PREFIX, NS_SMEV);
        envelope.addNamespaceDeclaration("mes1", NS_MSGD);
        envelope.addNamespaceDeclaration("mes", NS_GISGMP);
        envelope.addNamespaceDeclaration(NS_INC_PREFIX, NS_INC);

        SOAPBody body = sm.getSOAPBody();
        body.setAttributeNS(WSConstants.WSU_NS, "wsu:Id", "body");

        // a. Создание общего блока
        // <GISGMPTransferMsg>
        //      <Message>
        //          ...
        //      </Message>
        //      <MessageData>
        //          ...
        //      </MessageData>
        // </GISGMPTransferMsg>

        SOAPBodyElement GISGMPTransferMsg = body.addBodyElement(
                envelope.createName("GISGMPTransferMsg", NS_SMEV_PREFIX, NS_SMEV));

        // b. Создание блока
        // <Message>
        //      ...
        // </Message>

        SOAPElement Message = GISGMPTransferMsg.addChildElement(
            envelope.createName("Message", NS_REV_PREFIX, NS_REV));

        SOAPElement Sender = Message.addChildElement(envelope.createName("Sender", NS_REV_PREFIX, NS_REV));

        SOAPElement Code = Sender.addChildElement(envelope.createName("Code", NS_REV_PREFIX, NS_REV));
        Code.setTextContent("AN0000001");

        SOAPElement Name = Sender.addChildElement(envelope.createName("Name", NS_REV_PREFIX, NS_REV));
        Name.setTextContent("ИС АН 1");

        SOAPElement recipient = Message.addChildElement(envelope.createName("Recipient", NS_REV_PREFIX, NS_REV));

        Code = recipient.addChildElement(envelope.createName("Code", NS_REV_PREFIX, NS_REV));
        Code.setTextContent("RKZN35001");

        Name = recipient.addChildElement(envelope.createName("Name", NS_REV_PREFIX, NS_REV));
        Name.setTextContent("Казначейство России");

        SOAPElement serviceName = Message.addChildElement(envelope.createName("ServiceName", NS_REV_PREFIX, NS_REV));
        serviceName.setTextContent("GISGMP");

        SOAPElement typeCode = Message.addChildElement(envelope.createName("TypeCode", NS_REV_PREFIX, NS_REV));
        typeCode.setTextContent("GFNC");

        SOAPElement status = Message.addChildElement(envelope.createName("Status", NS_REV_PREFIX, NS_REV));
        status.setTextContent("REQUEST");

        SOAPElement date = Message.addChildElement(envelope.createName("Date", NS_REV_PREFIX, NS_REV));
        date.setTextContent(TIMESTAMP_DATE_TIME);

        SOAPElement exchangeType = Message.addChildElement(envelope.createName("ExchangeType", NS_REV_PREFIX, NS_REV));
        exchangeType.setTextContent("6");

        SOAPElement testMsg = Message.addChildElement(envelope.createName("TestMsg", NS_REV_PREFIX, NS_REV));
        testMsg.setTextContent("test");

        // c. Создание блока
        // <MessageData>
        //      ...
        // </MessageData>

        SOAPElement messageData = GISGMPTransferMsg.addChildElement(
            envelope.createName("MessageData", NS_REV_PREFIX, NS_REV));

        // d. Создание блока
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

        SOAPElement appData = messageData.addChildElement(envelope.
            createName("AppData", NS_REV_PREFIX, NS_REV));

        appData.addNamespaceDeclaration(NS_GISGMP_PREFIX, NS_GISGMP);
        appData.addNamespaceDeclaration(NS_MSGD_PREFIX, NS_MSGD);
        appData.addNamespaceDeclaration(NS_PDR_PREFIX, NS_PDR);
        appData.addNamespaceDeclaration(NS_SMEV_PREFIX, NS_SMEV);
        appData.addNamespaceDeclaration(NS_XSI_PREFIX, NS_XSI);

        // e. Создание блока
        // <RequestMessage>
        //      <PackageStatusRequest>
        //          ...
        //      </PackageStatusRequest>
        // </RequestMessage>

        SOAPElement RequestMessage = appData.addChildElement(
            envelope.createName("RequestMessage", NS_GISGMP_PREFIX, NS_GISGMP));

        RequestMessage.addAttribute(envelope.createName("Id"), REQUEST_MESSAGE_ID);
        RequestMessage.addAttribute(envelope.createName("senderIdentifier"), SENDER_EXAMPLE_1);
        RequestMessage.addAttribute(envelope.createName("senderRole"), SENDER_ROLE);
        RequestMessage.addAttribute(envelope.createName("timestamp"), TIMESTAMP_DATE_TIME);

        // f. Создание блока
        // <PackageStatusRequest>
        //      ...
        // </PackageStatusRequest>

        SOAPElement PackageStatusRequest = RequestMessage.addChildElement(
            envelope.createName("PackageStatusRequest", NS_MSGD_PREFIX, NS_MSGD));

        PackageStatusRequest.addNamespaceDeclaration(NS_PSR_PREFIX, NS_PSR);

        SOAPElement PackageID = PackageStatusRequest.addChildElement(
            envelope.createName("PackageID", NS_PSR_PREFIX, NS_PSR));
        PackageID.setTextContent(packageId);

        try (FileOutputStream os = new FileOutputStream(WORK_DIR + "stub_req.xml")) {
            sm.writeTo(os); // лог
        }

        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();
        sm.writeTo(documentOut);
        documentOut.close();

        return documentOut.toByteArray();

    }

}
