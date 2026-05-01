/**
 * $RCSfileVerifyTool.java,v $
 * version $Revision$
 * created 18.06.2018 12:03 by la
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
package tools;

import ru.CryptoPro.JCP.tools.JCPLogger;
import ru.CryptoPro.JCPRequest.CertChainLoader;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.*;

/**
 * Класс, представляющиий утилиту для проверки подписи.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public abstract class VerifyTool {

    // таблица допустимых аргументов командной строки
    List<String> validArguments;
    // таблица допустимых аргументов командной строки с описанием
    List<String> validArgumentsHelp ;
    // путь к файлу подписи
    String sigPath = null;
    // путь к хранилищу сертификатов (папка с сертификатами)
    String certStorePath = null;
    // путь к хранилищу CRL
    String crlStorePath = null;
    // строковое представление типа подписи
    String stringType = null;
    // тип подписи
    Integer type;
    // уровень логирования (числовое представление)
    Integer logLevel = 0;
    // уровень логирования
    Level level = Level.OFF;
    // путь к файлу логирования
    String logPath = null;
    // путь к файлу с данными (для отделенной cades-подписи)
    String dataPath = null;
    // является ли подпись отделенной (для cades-подписи)
    boolean isDetached = false;

    // цепочка сертификатов для проверки
    Set<X509Certificate> chain = new HashSet<X509Certificate>();
    // crl для проверки
    Set<X509CRL> crlList = new HashSet<X509CRL>();

    /**
     * Функция осуществляет подготовку к проверке подписи: разбирает
     * параметры командной строки, читает файл подписи,
     * сертификаты, CRL, устанавливает уровень логирования.
     * @param args парметры командной строки
     * @throws Exception
     */
    public void prepareVerify(String[] args) throws Exception{

        // вывод на экран подсказку по параметрам
        System.out.println("usage:");
        for (String next : validArgumentsHelp)
            System.out.println(next);


        // разбираем параметры командной строки
        int i = 0;
        while (i < args.length) {
            if (args[i].startsWith("-")) {
                if (!validArguments.contains(args[i]) || !args[i].equals("-d") && (i +1 == args.length || args[i+1].startsWith("-")))
                    throw new Exception("Invalid argument:" + args[i]);
                switch (args[i]){
                    case "-sig":
                        sigPath = args[i+1];
                        i +=2;
                        break;
                    case "-type":
                        stringType = args[i+1];
                        i +=2;
                        break;
                    case "-data":
                        dataPath = args[i+1];
                        i +=2;
                        break;
                    case "-d":
                        isDetached = true;
                        i ++;
                        break;
                    case "-cert":
                        certStorePath = args[i+1];
                        i +=2;
                        break;
                    case "-crl":
                        crlStorePath = args[i+1];
                        i +=2;
                        break;
                    case "-log":
                        logLevel = Integer.parseInt(args[i+1]);
                        i +=2;
                        break;
                    case "-logpath":
                        logPath = args[i+1];
                        i +=2;
                        break;
                    default:
                        throw new Exception("Invalid argument:" + args[i]);
                }
            } else
                throw new Exception("Invalid argument:" + args[i]);
        }

        if (crlStorePath == null)
            crlStorePath = certStorePath;

        // устанавалием тип проверяемой подписи
        setSignatureType();

        // устанавливаем уровень логирования
        switch (logLevel){
            case 0:
                level = Level.OFF;
                break;
            case 1:
                level = Level.SEVERE;
                break;
            case 2:
                level = Level.WARNING;
                break;
            case 3:
                level = Level.INFO;
                break;
            case 4:
                level = Level.FINE;
                break;
            case 5:
                level = Level.ALL;
                break;
        }

        setLogLevelInFileHandler(level, logPath);

        // читаем сертификаты и CRL

        Set<File> certFiles = new HashSet<>();
        Set<File> crlFiles = new HashSet<>();

        if (certStorePath != null)
            getFiles(certStorePath, certFiles);

        if (crlStorePath != null)
            getFiles(crlStorePath, crlFiles);

        for (File file : certFiles) {
            String name = file.getName();

            // файл - сертификат
            if (name.endsWith(".cer")) {
                try (FileInputStream certStream = new FileInputStream(file.getAbsolutePath())) {
                    X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certStream);
                    chain.add(cert);
                }
            }

            //цепочка сертификатов
            else if (name.endsWith(".p7b")) {
                Certificate[] certs = CertChainLoader.loadChain(file.getAbsolutePath());
                for (Certificate cert : certs) {
                    chain.add((X509Certificate) cert);
                }
            }

        }

        for (File file : crlFiles) {
            String name = file.getName();
            //crl
            if (name.endsWith(".crl")) {
                try (FileInputStream is = new FileInputStream(file.getAbsolutePath())) {
                    X509CRL crl = (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(is);
                    crlList.add(crl);
                }
            }
        }

    }

    /**
     * Функция осуществляет проверку проверку подписи.
     * @param args Параметры проверки.
     * @throws Exception
     */
    public abstract void verifySignature(String[] args) throws Exception ;

    /**
     * Функция устанавлиает тип проверяемой подписи
     * @throws Exception
     */
    public abstract void setSignatureType() throws Exception ;

    /**
     * Запись в файл данных логгера путем вызова функций, пишущих на
     * заданном уровне. Т.к. JCPLogger содержит в себе объект для определения
     * текущего уровня логирования, а его конструктор - приватный, то
     * некоторые сообщения могут не выводиться.
     *
     * @param level Актуальный уровень логгера.
     * @param logPath Путь для сохранения лога.
     * @throws Exception
     */
    protected void setLogLevelInFileHandler(Level level, String logPath)
        throws Exception {

        Logger logger = Logger.getLogger(JCPLogger.LOGGER_NAME);
        logger.setLevel(level);

        // Вывод в консоль.

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);

        // Запись в файл.

        if (logPath != null) {
            Handler fileHandler = new FileHandler(logPath);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(level);
            logger.addHandler(fileHandler);
        } // if

    }

    /**
     * Функция получает все файлы из папки рекурсивно.
     *
     * @param path Путь к папке.
     * @param files Полученные файлы.
     */
    public static void getFiles(String path, Set<File> files) {
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            File[] listFiles = pathFile.listFiles();
            if (listFiles == null) return;
            for (File next : listFiles) {
                if (next.isFile())
                    files.add(next);
                else if (next.isDirectory())
                    getFiles(next.getAbsolutePath(), files);
            }
        } else if (pathFile.isFile())
            files.add(pathFile);
    }


}
