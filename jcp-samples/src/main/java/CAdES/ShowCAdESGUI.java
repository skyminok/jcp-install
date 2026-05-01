/**
 * $RCSfileShowCAdESGUI.java,v $
 * version $Revision: 36379 $
 * created 22.11.2016 10:49 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2016.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <br>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CAdES;

import ComLine.ComLine;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.tools.gui.CAdESSignatureViewer;

import ru.CryptoPro.JCP.tools.Array;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Класс утилиты для визуализации подписи
 * формата CAdES.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ShowCAdESGUI {

    /**
     * Список расширений файлов сертификатов.
     */
    private static final List EXT_CER = Arrays.asList("cer", "crt", "der");

    /**
     * Список расширений файлов CRL.
     */
    private static final List EXT_CRL = Collections.singletonList("crl");

    /**
     * Главный метод.
     *
     * @param args Аргументы.
     * @return результат выполнения.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("Usage: -signature <file> [-data <data-file>]" +
                " [-cert <path-to-certificates>] [-crl <path-to-CRLs>]");
            System.exit(1);
        } // if

        // Чтение параметров.

        String filePath = ComLine.getValue("-signature", args, null);
        String dataPath = ComLine.getValue("-data",      args, null);
        String certPath = ComLine.getValue("-cert",      args, null);
        String cRLPath  = ComLine.getValue("-crl",       args, null);

        byte[] signature = Array.readFile(filePath);
        if (signature == null) {
            throw new Exception("Signature file not found");
        } // if

        byte[] data = null;

        if (dataPath != null) {
            data = Array.readFile(dataPath);
        } // if

        // Загрузка сертификатов.

        Set<X509Certificate> certificates = new HashSet<X509Certificate>();
        if (certPath != null) {
            load(certPath, certificates, true);
        } // if

        // Загрузка CRL.

        Set<X509CRL> cRLs = new HashSet<X509CRL>();
        if (cRLPath != null) {
            load(cRLPath, cRLs, false);
        } // if

        // Отображение и проверка подписи.

        CAdESSignature cAdESSignature = new CAdESSignature(signature, data, null);
        CAdESSignatureViewer.show(cAdESSignature, certificates, cRLs);

    }

    /**
     * Чтение содержимого файла или папки.
     *
     * @param filePath Путь к файлу или папке.
     * @param set Список созданных объектов.
     * @param isCertificate True, если сертификат.
     * @throws Exception
     */
    private static void load(String filePath, Set set, boolean
        isCertificate) throws Exception {

        File file = new File(filePath);
        if (file.exists()) {

            if (file.isDirectory()) {
                File[] files = file.listFiles();

                if (files == null) {
                    return;
                } // if

                for (File subFile : files) {

                    if (subFile.isDirectory()) {
                        continue;
                    } // if

                    Object cert = readContent(file, isCertificate);

                    if (cert != null) {
                        set.add(cert);
                    } // if

                } // for

            } // if
            else {

                Object crl = readContent(file, isCertificate);

                if (crl != null) {
                    set.add(crl);
                } // if

            } // else

        } // if

    }

    /**
     * Чтение содержимого файла и преобразование в
     * сертификат или CRL.
     *
     * @param file Файл.
     * @param isCertificate True, если сертификат.
     * @return созданный объект или null.
     * @throws Exception
     */
    private static Object readContent(File file, boolean isCertificate)
        throws Exception {

        if (isCertificate) {

            if (EXT_CER.contains(extractFileExt(file))) {
                try (FileInputStream is = new FileInputStream(file)) {
                    return CertificateFactory.getInstance("X.509").generateCertificate(is);
                }
            } // if

        } // if
        else {

            if (EXT_CRL.contains(extractFileExt(file))) {
                try (FileInputStream is = new FileInputStream(file)) {
                    return CertificateFactory.getInstance("X.509").generateCRL(is);
                }
            } // if

        } // else

        return null;

    }

    /**
     * Извлечение расширения файла из имени.
     *
     * @param file Файл.
     * @return расширение файла или "".
     */
    private static String extractFileExt(File file) {

        String fileName = file.getName();
        int dotPos = fileName.indexOf('.');

        if (dotPos < 0) {
            return "";
        } // if

        return fileName.substring(dotPos + 1);

    }

}
