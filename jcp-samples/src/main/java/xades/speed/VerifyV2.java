/**
 * $RCSfileVerifyV2.java,v $
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

import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.w3c.dom.Document;

import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.XAdES.XAdESType;

import xades.AnonymousResolver;
import xades.util.XMLUtility;

import java.io.File;

/**
 * Пример для определения скорости проверки подписи
 * Xades для нового формата (в одном потоке и в
 * многопоточном режиме).
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class VerifyV2 {

    public static final String XADES_DOC_PATH = XadesVsCades.TRUST_DIR
        + "xml" + File.separator + "XAdES" + File.separator + "V2" + File.separator;

    public static void main(String[] args) throws Exception {

        // Отключаем проверку цепочки службы штампов
        System.setProperty("ru.CryptoPro.CAdES.validate_tsp", "false");

        // Добавление AnonymousResolver для проверки узлов Reference без URI
        ResourceResolver.register(new AnonymousResolver(XADES_DOC_PATH + "xades-v2-base.xml"), true);

        int n = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        switch (n) {
            case 1:
                verifyXadesBes();
                break;
            case 2:
                verifyXadesBesThread();
                break;
            case 3:
                verifyXadesT();
                break;
            case 4:
                verifyXadesTThread();
                break;
            default:
                break;
        }

    }

    /**
     * Проверка BES подписи в одном потоке
     *
     * @throws Exception
     */
    public static void verifyXadesBes() throws Exception {
        System.out.println("Verify xades BES (single)");
        byte[] doc = Array.readFile(XADES_DOC_PATH + "xades-v2-verify.xml");
        Document signedDoc = XMLUtility.parseFile(doc);
        XadesVsCades.verify(signedDoc, new Integer[]{XAdESType.XAdES_BES}, null, null, false, 0);
        System.out.println("*************");
    }

    /**
     * Проверка Xades-BES подписи в нескольких потоках
     *
     * @throws Exception
     */
    public static void verifyXadesBesThread() throws Exception {
        System.out.println("Verify xades BES (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, false,
                XAdESType.XAdES_BES, null, null, null, 0,
                    XADES_DOC_PATH + "xades-v2-verify.xml");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }

    /**
     * Проверка Xades-T подписи в одном потоке
     *
     * @throws Exception
     */
    public static void verifyXadesT() throws Exception {
        System.out.println("Verify xades T (single)");
        byte[] doc = Array.readFile(XADES_DOC_PATH + "xades-v2-verify.xml");
        Document signedDoc = XMLUtility.parseFile(doc);
        XadesVsCades.verify(signedDoc, new Integer[]{XAdESType.XAdES_T},
            null, null, false, 1);
        System.out.println("*************");
    }

    /**
     * Проверка Xades-T подписи в нескольких потоках
     *
     * @throws Exception
     */
    public static void verifyXadesTThread() throws Exception {
        System.out.println("Verify xades T (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, false,
                XAdESType.XAdES_T, null, null, null, 1,
                    XADES_DOC_PATH + "xades-v2-verify.xml");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }
}


