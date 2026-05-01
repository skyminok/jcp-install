/**
 * $RCSfileXadesVsCadesThread.java,v $
 * version $Revision$
 * created 13.02.2018 0:12 by elvira
 * last modified $Date$ by $Author$
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
package xades.speed;

import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2001;

import org.w3c.dom.Document;

import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;

import xades.config.XAdESConfig;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Реализация потока для сравнения скорости работы функций
 * подписи и проверки подписи для Xades и Cades  в многопоточном
 * режиме.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XadesVsCadesThread extends Thread {

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    private boolean isCades;
    private boolean isSign;
    Integer type;

    Set<X509Certificate> certificates;
    Set<X509CRL> cRLs;
    String tsp;
    int tscount;
    String dataPath;
    int threadnum;

    /**
     * Конструктор
     *
     * @param threadnum
     * @param isCades
     * @param isSign
     * @param type
     * @param certs
     * @param cRLs
     * @param tsp
     * @param tscount
     * @param dataPath
     */
    public XadesVsCadesThread(int threadnum, boolean isCades, boolean isSign,
        Integer type, Set<X509Certificate> certs, Set<X509CRL> cRLs, String tsp,
        int tscount, String dataPath) {
        this.isCades = isCades;
        this.isSign = isSign;
        this.type = type;
        this.certificates = certs;
        this.cRLs = cRLs;
        this.tsp = tsp;
        this.tscount = tscount;
        this.dataPath = dataPath;
        this.threadnum = threadnum;
    }

    @Override
    public void run() {

        if (!isCades && !isSign) { //проверка Xades

            try {
                byte[] doc = Array.readFile(dataPath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                dbFactory.setNamespaceAware(true);

                Document signedDoc = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(doc));
                XadesVsCades.verify(signedDoc, new Integer[]{type}, certificates, cRLs, false, tscount);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (!isCades && isSign) { // Подпись Xades

            try {

                XadesVsCades.sign(
                        new Integer[]{type},
                        XAdESConfig.Default.CONFIG_2001_S,
                        XadesVsCades.XML_DOC.getBytes("UTF-8"),
                        dataPath,
                        null,
                        new ITransform[]{new EnvelopedTransform()},
                        null,
                        false,
                        null,
                        null
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isCades && isSign) { // Подпись cades

            try {
                IConfiguration config = new SimpleConfiguration(new Container2001(), false);
                XadesVsCades.signCADES(config, type, null, XadesVsCades.XML_DOC.getBytes("UTF-8"), dataPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {//проверка cades

            try {
                IConfiguration config = new SimpleConfiguration(new Container2001(), false);
                XadesVsCades.verifyCAdES(config, dataPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}