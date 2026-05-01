/**
 * $RCSfileGisGmpServiceLowEnvelopeExample.java,v $
 * version $Revision: 36379 $
 * created 23.06.2015 16:18 by afevma
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
package xades.gisgmp;

import org.apache.axis.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import ru.CryptoPro.XAdES.util.XMLUtils;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_Service;
import ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_ServiceLocator;

import wss4j.examples.SMEVSignBodyThenSecurity;

import xades.XAdES4JSignVerify;
import xades.XAdESSignVerify;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.gisgmp.source.GisGmpServiceLowEnvelopeDocument;
import xades.util.GostXAdESUtility;
import xades.util.XMLUtility;

import javax.xml.soap.SOAPEnvelope;

import java.io.*;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Пример создания документа, подписи (xades-T) и отправки
 * документа в сервис ГИС ГМП.
 * Документ собирается с помощью SOAPMessage из блоков.
 * Конечная цель - документ, как в заголовке {@link
 * xades.gisgmp.GisGmpServiceExample}.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @see xades.XAdESExample
 * @see wss4j.examples.SMEVSignBodyThenSecurity
 * @see xades.gisgmp.GisGmpServiceExample
 */
public class GisGmpServiceLowEnvelopeExample extends GostXAdESUtility {

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }
    public static void main(String[] args) throws Exception {

      byte[] documentBin = GisGmpServiceLowEnvelopeDocument.createDocument();

        // 3. Подпись SOAP документа XAdES-T
        // Создание XAdES-T подписи с помощью тестового ключа
        // (сертификат из внешнего тестового УЦ) и внешней тестовой
        // TSP службы (http://www.cryptopro.ru:80/tsp/).

        IXAdESConfig xAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2001_S;

        Document signedXAdESTDoc = XAdESSignVerify.sign(new Integer[]
            {XAdESType.XAdES_T}, xAdESConfigTestSignKey, documentBin,
                WORK_DIR, SIGNING_ID, new ITransform[] {new EnvelopedTransform()},
                    null, false, "http://www.cryptopro.ru:80/tsp/", null);

        // XAdES
        XAdESSignVerify.verify(signedXAdESTDoc, new Integer[]{XAdESType.XAdES_T}, null, null, false, 1);

        // xades4j
        XAdES4JSignVerify.verify(xAdESConfigTestSignKey, signedXAdESTDoc, false);

        // 4. Создание подписи Security Header
        // Добавление Security Header с помощью ключевого контейнера
        // с сертификатом, выпущенным в аккредитованном УЦ.

        final String securityAlias = "GisSignContainer";
        final String securityPassword = "1";

        Document doubleSignedDoc =
            SMEVSignBodyThenSecurity.signSecurity(
            signedXAdESTDoc, WORK_DIR + "stub_xades_with_sh.xml", securityAlias,
            securityPassword); // подпись и лог

        // 5. Отправка документа в сервис

        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();
        XMLUtils.writeXML(documentOut, doubleSignedDoc);

        Message doubleSignedDocMsg = new Message(documentOut.toByteArray());
        SmevGISGMPService_Service gmpService = new SmevGISGMPService_ServiceLocator();

        SmevGISGMPService_PortType gmpPort = gmpService.getSmevGISGMPServiceSOAP();
        SOAPEnvelope response1Env = gmpPort.GISGMPTransferMsg(doubleSignedDocMsg);

        System.out.println("\nReceived response #1...\n");
        Document response1Doc = response1Env.getOwnerDocument();

        // 6. Обработка ответа и извлечение package Id

        // Лог
        //String response1Str = org.apache.ws.security.util
        //    .XMLUtils.PrettyDocumentToString(response1Doc);
        //System.out.println("\n" + response1Str + "\n");

        NodeList nodes = response1Doc.getElementsByTagNameNS(
            "http://roskazna.ru/gisgmp/xsd/116/ErrInfo", "ResultData");

        if (nodes.getLength() == 0) {
            throw new Exception("ResultData no found.");
        }

        Element resultData = (Element) nodes.item(0);
        String packageId = resultData.getFirstChild().getNodeValue();

        System.out.println("\n$$$ Package ID: " + packageId + " $$$\n");
        Thread.sleep(5000); // задержка

        // 7. Составление запроса

        documentBin = GisGmpServiceLowEnvelopeDocument.createRequest(packageId);

        // 8. Создание подписи Security Header
        // Подпись запроса с помощью ключевого контейнера
        // с сертификатом, выпущенным в аккредитованном УЦ.

        Document requestIdDoc = parseFile(documentBin);

        Document signedRequestIdDoc = SMEVSignBodyThenSecurity.signSecurity(
            requestIdDoc, WORK_DIR + "stub_req_with_security.xml",
                securityAlias, securityPassword); // подпись и лог

        // 9. Отправка запроса в сервис

        documentOut = new ByteArrayOutputStream();
        XMLUtils.writeXML(documentOut, signedRequestIdDoc);

        Message signedRequestIdDocMsg = new Message(documentOut.toByteArray());
        gmpService = new SmevGISGMPService_ServiceLocator();

        gmpPort = gmpService.getSmevGISGMPServiceSOAP();
        SOAPEnvelope response2Env = gmpPort.GISGMPTransferMsg(signedRequestIdDocMsg);

        System.out.println("\nReceived response #2...\n");
        Document response2Doc = response2Env.getOwnerDocument();

        // Лог
        //String response2Str = org.apache.ws.security.util
        //    .XMLUtils.PrettyDocumentToString(response2Doc);
        //System.out.println("\n" + response2Str + "\n");

        // 10. Обработка ответа

        nodes = response2Doc.getElementsByTagNameNS(
            "http://roskazna.ru/gisgmp/xsd/116/ErrInfo", "ResultCode");

        if (nodes.getLength() == 0) {
            throw new Exception("ResultCode no found.");
        }

        Element resultCode = (Element) nodes.item(0);
        String resultCodeValue = resultCode.getFirstChild().getNodeValue();
        System.out.println("\nFinal (XAdES-T?) result code: " + resultCodeValue + "\n");

    }

    /**
     * Создание хранилища со списком дополнительных сертификатов
     * и CRL.
     *
     * @param certList Список сертификатов.
     * @param crlList Список CRL.
     * @return хранилище.
     * @throws Exception
     */
    protected static CertStore getCertStore(Collection<X509Certificate>
        certList, Collection<X509CRL> crlList) throws Exception {

        final Collection certCrlList = new ArrayList();

        if (certList != null && !certList.isEmpty()) {
            certCrlList.addAll(certList);
        }

        if (crlList != null && !crlList.isEmpty()) {
            certCrlList.addAll(crlList);
        }

        return CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(certCrlList));

    }

}
