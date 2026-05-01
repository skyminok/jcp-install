/**
 * $RCSfileUploadServlet.java,v $
 * version $Revision$
 * created 25.06.2018 13:09 by elvira
 * last modified $Date$ by $Author$
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package webVerify;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.bouncycastle.tsp.TimeStampToken;
import org.w3c.dom.Document;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DirList;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Decoder;
import ru.CryptoPro.JCP.tools.JCPLogger;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESSigner;
import ru.CryptoPro.XAdES.XAdESType;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

/**
 * Класс для обработки запроса и проверки подписи.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
@MultipartConfig
public class VerifyServlet extends HttpServlet {

    public static List<String> cadesTypes = new ArrayList<String>() {{
        add("Default");
        add("PKCS7");
        add("CAdES-BES");
        add("CAdES-T");
        add("CAdES-XLT1");
    }};

    public static List<String> xadesTypes = new ArrayList<String>() {{
        add("XAdES-BES");
        add("XAdES-T");
        add("XAdES-XLT1");
    }};

    static {

        JCPInit.initProviders(false);

        if(!JCPXMLDSigInit.isInitialized()) {
            JCPXMLDSigInit.init();
        } // if

    }

    /**
     * Функция обрабатывает запрос.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream signatureStream = null;
        InputStream dataStream = null;
        InputStream certStream = null;

        // тип подписи
        String signatureTypeStr = request.getParameter("SigTypeBox");
        String resultMsg = "Signature type: " + signatureTypeStr + "\r\n";

        // считываем подпись из файла
        Part sigPart = request.getPart("SigFile");
        if (sigPart != null && sigPart.getSize() > 0) {
            String sigFileName = getSubmittedFileName(sigPart);
            resultMsg += "Signature source (file):  " + sigFileName + "\r\n";
            request.setAttribute("SigFile", sigFileName);
            signatureStream = sigPart.getInputStream();
        } else { // иначе из окна
            resultMsg += "Signature source: window\r\n";
            String signature = request.getParameter("SigInBase");
            if (signature != null) {
                request.setAttribute("SigInBase", signature);
                byte[] sigEncoded = signature.getBytes();
                if (DirList.isBase64(sigEncoded)) {
                    final Decoder decoder = new Decoder();
                    final byte[] sigDecoded = decoder.decodeBuffer(signature);
                    signatureStream = new ByteArrayInputStream(sigDecoded);
                }
                else{
                    signatureStream = new ByteArrayInputStream(sigEncoded);
                }
            }
        }
        // отделенная ли подпись
        boolean isDetached = request.getParameter("isDetached") != null;
        resultMsg += "Signature detached: " + isDetached + "\r\n";

        // файл с данными (для отделенной подписи)
        Part dataPart = request.getPart("DataFile");
        if (dataPart != null && dataPart.getSize() > 0 && isDetached) {
            String dataFileName = getSubmittedFileName(dataPart);
            resultMsg += "Data file: " + dataFileName + "\r\n";
            request.setAttribute("DataFile", dataFileName);
            dataStream = dataPart.getInputStream();
        }

        // файл с сертификатом (для CAdes-BES)
        Part certPart = request.getPart("CertFile");
        if (certPart != null && certPart.getSize() > 0) {
            String certFileName = getSubmittedFileName(certPart);
            resultMsg += "Certificate file: " + certFileName + "\r\n";
            request.setAttribute("CertFile", certFileName);
            certStream = certPart.getInputStream();
        }

        // получаем путь к файлу, в который пишется лог
        Logger logger = Logger.getLogger(JCPLogger.LOGGER_NAME);
        Handler[] handlers = logger.getHandlers();
        String fname = null;
        for (Handler next: handlers){
            fname = LogManager.getLogManager().getProperty( next.getClass().getName() + ".pattern");
            if (fname != null)
                break;
        }
        FileInputStream fs = (fname != null) ? new FileInputStream(fname) : null;

        // число байт, которые уже есть файле (их проигнорируем)
        int skipBytes = (fs != null) ? fs.available(): 0;

        // проверка подписи
        String verifyResult = "";

        if (cadesTypes.contains(signatureTypeStr))
            verifyResult = verifyCades(signatureStream, dataStream, certStream, signatureTypeStr);
        else if (xadesTypes.contains(signatureTypeStr))
            verifyResult = verifyXades(signatureStream, certStream, signatureTypeStr);
        else if (signatureTypeStr.equals("SignPDF"))
            verifyResult = verifyPDF(signatureStream);
        resultMsg += verifyResult;
        request.setAttribute("Result", resultMsg);

        // лог проверки
        if (fs != null) {
            fs.skip(skipBytes);
            final int len = fs.available();
            final byte[] all = new byte[len];
            if (fs.read(all) == len){
                String logStr = new String(all);
                logStr = logStr.replaceAll("\n", "<br>");
                request.setAttribute("Log", logStr);
            }
            fs.close();
        }
        this.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
    }

    /**
     * Функция получает имя файла
     *
     * @param part
     * @return
     */
    private static String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }


    private static DateFormat DATE_FORMAT_CERT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    /**
     * Функция проверки Cades подписи.
     * @param signatureStream
     * @param dataStream
     * @param certStream
     * @param signatureTypeStr
     * @return
     * @throws Exception
     */
    private static String verifyCades(InputStream signatureStream, InputStream dataStream, InputStream certStream, String signatureTypeStr)  {
        String resultMsg = "";

        try {
            Integer signatureType = null;
            if (signatureTypeStr.equals("Default"))
                signatureType = null;
            else if (signatureTypeStr.equals("PKCS7"))
                signatureType = CAdESType.PKCS7;
            else if (signatureTypeStr.equals("CAdES-BES"))
                signatureType = CAdESType.CAdES_BES;
            else if (signatureTypeStr.equals("CAdES-T"))
                signatureType = CAdESType.CAdES_T;
            else if (signatureTypeStr.equals("CAdES-XLT1"))
                signatureType = CAdESType.CAdES_X_Long_Type_1;

            CAdESSignature cadesSignature = new CAdESSignature(signatureStream, dataStream, signatureType);

            Set<X509Certificate> chain = new HashSet<X509Certificate>();
            if (certStream != null) {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certStream);
                resultMsg += "\r\nCertificate for verification:";
                resultMsg += "\r\n    SerialNumber: " + cert.getSerialNumber().toString(16);
                resultMsg += "\r\n    Subject: " + cert.getSubjectDN().toString();
                resultMsg += "\r\n    Issuer: " + cert.getIssuerDN().toString();
                resultMsg += "\r\n    Subject: " + cert.getSubjectDN().toString();
                resultMsg += "\r\n    NotBefore: " + DATE_FORMAT_CERT.format(cert.getNotBefore());
                resultMsg += "\r\n    NotAfter: " + DATE_FORMAT_CERT.format(cert.getNotAfter());
                chain.add(cert);
            }

            CAdESSigner[] signers = cadesSignature.getCAdESSignerInfos();
            for (int i = 0; i < signers.length; i++) {
                resultMsg += "\r\nSigner #" + i + 1;
                if (signatureType == null) {
                    resultMsg += "\r\n  Signature type: ";
                    Integer signerType = signers[i].getSignatureType();
                    if (signerType.equals(CAdESType.PKCS7))
                        resultMsg += "PKCS7";
                    else if (signerType.equals(CAdESType.CAdES_BES))
                        resultMsg += "CAdES_BES";
                    else if (signerType.equals(CAdESType.CAdES_T))
                        resultMsg += "CAdES_T";
                    else if (signerType.equals(CAdESType.CAdES_X_Long_Type_1))
                        resultMsg += "CAdES_X_Long_Type_1";
                    else
                        resultMsg += "Unknown";
                }
                // получаем сертификат подписи
                X509Certificate certificate = signers[i].getSignerCertificate();
                if (certificate != null) {
                    resultMsg += "\r\n  Certificate info: ";
                    resultMsg += "\r\n      SerialNumber: " + certificate.getSerialNumber().toString(16);
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      Issuer: " + certificate.getIssuerDN().toString();
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      NotBefore: " + DATE_FORMAT_CERT.format(certificate.getNotBefore());
                    resultMsg += "\r\n      NotAfter: " + DATE_FORMAT_CERT.format(certificate.getNotAfter());
                }
            }
            // проверяем подпись
            cadesSignature.verify(chain, null);
            resultMsg += "\r\n\r\nResult: signature verified successfully.";
        }
        catch(Exception ex){
            resultMsg += "\r\n\r\nResult: signature verification failed.";
            resultMsg +="\r\n\r\nReason: " + ex;
        }
        return resultMsg;

    }

    /**
     * Функция проверки xades подписи.
     * @param signatureStream
     * @param certStream
     * @param signatureTypeStr
     * @return
     * @throws Exception
     */
    private static String verifyXades(InputStream signatureStream, InputStream certStream, String signatureTypeStr) {
        String resultMsg = "";

        try {
            Integer signatureType = null;
            if (signatureTypeStr.equals("XAdES-BES"))
                signatureType = XAdESType.XAdES_BES;
            else if (signatureTypeStr.equals("XAdES-T"))
                signatureType = XAdESType.XAdES_T;
            else if (signatureTypeStr.equals("XAdES-XLT1"))
                signatureType = XAdESType.XAdES_X_Long_Type_1;


            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setNamespaceAware(true);
            Document signedDoc = dbFactory.newDocumentBuilder().parse(signatureStream);

            XAdESSignature xadesSignature = new XAdESSignature(signedDoc.getDocumentElement(), signatureType);

            Set<X509Certificate> chain = new HashSet<X509Certificate>();
            if (certStream != null) {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certStream);
                resultMsg += "\r\nCertificate for verification:";
                resultMsg += "\r\n    SerialNumber: " + cert.getSerialNumber().toString(16);
                resultMsg += "\r\n    Subject: " + cert.getSubjectDN().toString();
                resultMsg += "\r\n    Issuer: " + cert.getIssuerDN().toString();
                resultMsg += "\r\n    Subject: " + cert.getSubjectDN().toString();
                resultMsg += "\r\n    NotBefore: " + DATE_FORMAT_CERT.format(cert.getNotBefore());
                resultMsg += "\r\n    NotAfter: " + DATE_FORMAT_CERT.format(cert.getNotAfter());
                chain.add(cert);
            }

            XAdESSigner[] signers = xadesSignature.getXAdESSignerInfos();
            for (int i = 0; i < signers.length; i++) {
                resultMsg += "\r\nSigner #" + i + 1;
                X509Certificate certificate = signers[i].getSignerCertificate();
                if (certificate != null) {
                    resultMsg += "\r\n  Certificate info: ";
                    resultMsg += "\r\n      SerialNumber: " + certificate.getSerialNumber().toString(16);
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      Issuer: " + certificate.getIssuerDN().toString();
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      NotBefore: " + DATE_FORMAT_CERT.format(certificate.getNotBefore());
                    resultMsg += "\r\n      NotAfter: " + DATE_FORMAT_CERT.format(certificate.getNotAfter());
                }
            }
            // проверка подписи
            if (!signatureType.equals(XAdESType.XAdES_X_Long_Type_1)) {
                xadesSignature.verify(chain, null);
            }
            else {
                xadesSignature.verify(null);
            }
            resultMsg += "\r\n\r\nResult: signature verified successfully.";
        } catch (Exception ex) {
            resultMsg += "\r\n\r\nResult: signature verification failed.";
            resultMsg +="\r\n\r\nReason: " + ex;

        }
        return resultMsg;
    }

    /**
     * Функция проверки pdf документа.
     * @param signatureStream
     * @return
     */
    private static String verifyPDF(InputStream signatureStream)  {
        String resultMsg = "";

        try {
            PdfReader checker = new PdfReader(signatureStream);
            AcroFields af = checker.getAcroFields();

            ArrayList<String> signatureNames = af.getSignatureNames();
            if (signatureNames.size() == 0) {
                throw new Exception("Signatures not found.");
            } // if


            for (int i = 0; i <signatureNames.size(); i ++) {
                String signatureName = signatureNames.get(i);
                resultMsg += "\r\nSigner #" + i + 1;
                resultMsg += "\r\n  Signature name: " + signatureName;
                PdfPKCS7 pk = af.verifySignature(signatureName, JCP.PROVIDER_NAME);

                X509Certificate certificate = pk.getSigningCertificate();
                if (certificate != null) {
                    resultMsg += "\r\n  Certificate info: ";
                    resultMsg += "\r\n      SerialNumber: " + certificate.getSerialNumber().toString(16);
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      Issuer: " + certificate.getIssuerDN().toString();
                    resultMsg += "\r\n      Subject: " + certificate.getSubjectDN().toString();
                    resultMsg += "\r\n      NotBefore: " + DATE_FORMAT_CERT.format(certificate.getNotBefore());
                    resultMsg += "\r\n      NotAfter: " + DATE_FORMAT_CERT.format(certificate.getNotAfter());
                }
                Date date = pk.getSignDate().getTime();
                resultMsg += "\r\n  Signature date: " + DATE_FORMAT_CERT.format(date);

                TimeStampToken ts = pk.getTimeStampToken();
                if (ts != null) {
                    boolean imprint = pk.verifyTimestampImprint();
                    resultMsg += "\r\n  Timestamp imprint verified: " + imprint;
                    date = pk.getTimeStampDate().getTime();
                    resultMsg += "\r\n  Timestamp date: " + DATE_FORMAT_CERT.format(date);
                } // if

                boolean verified = pk.verify();
                if (!verified)
                    throw new Exception("Invalid signature: " + signatureName);
            } // if

            resultMsg += "\r\n\r\nResult: signature verified successfully.";
        } catch (Exception ex) {
            resultMsg += "\r\n\r\nResult: signature verification failed.";
            resultMsg +="\r\n\r\nReason: " + ex;

        }
        return resultMsg;

    }

}