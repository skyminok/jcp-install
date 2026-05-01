/**
 * $RCSfile$
 * version $Revision$
 * created 30.09.2008 10:37:34 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package ComLine;

import JTLS_samples.Client;
import JTLS_samples.Server;
import JTLS_samples.connection.SSLConfiguration;
import JTLS_samples.connection.SSLConnector;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.DefaultProviders;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.License;

import util.simulator.BioDetector;
import util.simulator.BioSimulator;

import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import ru.CryptoPro.ssl.util.cpSSLConfig;

import userSamples.Constants;

import util.ResolveProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Проверка работы провайдера.
 *
 * @author Copyright 2004-2008 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CheckConfFull {

private static boolean f_jcp    = false;
private static boolean f_crypto = false;
private static boolean f_jtls   = false;
private static boolean f_jcsp   = false;
private static boolean f_sspi   = false;

private static final Logger log = Logger.getLogger("Log");

// Список читаемых имен алгоритмов
private static final String[] ALG_NAMES = {
    "GOST R 34.10-2001",
    "GOST R 34.10-2012 (256)",
    "GOST R 34.10-2012 (512)"
};

// Список алгоритмов для генерации контейнера на алгоритме подписи
private static final String[] kpAlg = {
    JCP.GOST_EL_DEGREE_NAME,
    JCP.GOST_EL_2012_256_NAME,
    JCP.GOST_EL_2012_512_NAME
};

// Список алгоритмов для генерации контейнера на алгоритме обмена
private static final String[] kpAlgDH = {
    JCP.GOST_EL_DH_NAME,
    JCP.GOST_DH_2012_256_NAME,
    JCP.GOST_DH_2012_512_NAME
};

// Список алгоритмов подписи
private static final String[] signAlg = {
    JCP.GOST_EL_SIGN_NAME,
    JCP.GOST_SIGN_2012_256_NAME,
    JCP.GOST_SIGN_2012_512_NAME
};

// Список алгоритмов шифрования
private static final String[] cipherAlg = {
    JCP.GOST_CIPHER_NAME,
    JCP.GOST_M_CIPHER_NAME,
    JCP.GOST_K_CIPHER_NAME
};

// Список алиасов для подписи
private static final String[] signAliases = {
    "jcptestsignkey_2001",
    "jcptestsignkey_2012_256",
    "jcptestsignkey_2012_512"
};

// Список алиасов для шифрования
private static final String[] clientAliases = {
    "jcptestclientkey_2001",
    "jcptestclientkey_2012_256",
    "jcptestclientkey_2012_512"
};

// список алиасов для шифрования
private static final String[] serverAliases = {
    "jcptestserverkey_2001",
    "jcptestserverkey_2012_256",
    "jcptestserverkey_2012_512"
};

// Список CN для серверных контейнеров
private static final String[] serverCNs = {
    "localhost",
    "localhost",
    "localhost"
};

// Список паролей
private static final char[][] passwords = {
    "a1234".toCharArray(),
    "p1234".toCharArray(),
    "q1234".toCharArray()
};

private static final char[] trustPass = "jcptest".toCharArray();
private static final String dNameCN = "CN=";
private static final String dNameE = ",OU=Security,O=CryptoPro,C=RU";
private static final int sslPort = 1212;
private static final String server_store = "jcptestserverkey.store";
private static final String key_store = "jcptestclientkey.store";
private static final String in = "jcptestdoc.in";
private static final String out = "jcptestdoc.out";
private static final String out_a = "jcptestdoc_a.out";
private static String doc;
private static String get_doc;
private static String get_doc_a;
private static String trustSt;
private static String keySt;

public static final String OK = "OK";
public static final String FAIL = "FAILURE";
private static final String FORMAT_LINE = "* %-32s - %s\n";
private static final String FORMAT_ITEM = "\n\t%-26s - %s (%d ms)";

public static final Map<String, String> checkedModules = new LinkedHashMap<String, String>();

private static final String JCP_PROVIDER_NAME = "JCP";
private static final String CRYPTO_PROVIDER_NAME = "Crypto";
private static final String JTLS_PROVIDER = "JTLS";
private static final String JCSP_PROVIDER_NAME = ResolveProvider.ALTERNATIVE_PROVIDER;

// Класс с описанием статуса проверки
private static class CheckStatus {

    // Успех проверки
    private final boolean status;

    // Описание проверки
    private final String description;

    /**
     * Конструктор.
     *
     * @param status Успех проверки.
     * @param description Описание проверки.
     */
    public CheckStatus(boolean status, String description) {
        this.status = status;
        this.description = description;
    }

    /**
     * Результат проверки.
     *
     * @return результат.
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Описание проверки.
     *
     * @return описание.
     */
    public String getDescription() {
        return description;
    }

}

static {
    // Включаем возможность онлайновой проверки цепочки.
    System.setProperty("com.sun.security.enableCRLDP", "true");
    System.setProperty("com.ibm.security.enableCRLDP", "true");
}

/**
 * Конструктор.
 *
 */
private CheckConfFull() {
    ;
}

/**
 * Получение строки состояния по результату проверки.
 *
 * @param status Состояние проверки.
 * @return строка состояния.
 */
private static String getStatusInfo(CheckStatus status) {
    StringBuilder result = new StringBuilder();
    result.append(status.getStatus() ? OK : FAIL);
    result.append(status.getDescription());
    return result.toString();
}

/**
 * Класс для записи логов в файл.
 *
 */
static class LocalFileHandler extends StreamHandler {

    /**
     * Директория пользователя.
     */
    private static final String USER_DIR = System.getProperty("user.dir");

    /**
     * Файл лога.
     */
    private static final String LOG_FILE = USER_DIR + File.separator + "CheckConfFull.log";

    /**
     * Конструктор.
     *
     * @throws Exception
     */
    public LocalFileHandler() throws Exception {
        super(new FileOutputStream(LOG_FILE), new SimpleFormatter());
    }

}

/**
 * Упорядочивание провайдеров. Указанный провайдер ставится на первое место.
 *
 * @param isDefaultProviderJCSP True, если провайдер по умолчанию - Java CSP.
 */
private static void orderProviders(boolean isDefaultProviderJCSP) {
    boolean useSSPI = false;
    if (isDefaultProviderJCSP) { // только в случае Java CSP
        try {
            Class.forName(DefaultProviders.JTLS_SSPI_PROVIDER_CLASS);
            useSSPI = true;
        } catch (Exception | Error e) {}
        log.info("SSPI SSL provider (based on CSP) is " + (useSSPI ? "" : "NOT ") + "available.");
    } // if
    JCPInit.initProvidersFromCheckConfFull(isDefaultProviderJCSP, useSSPI);
    clearCache(); // очистка кеша сертификатов и открытых ключей
}

/**
 * Загрузка класса лицензии.
 *
 * @param licenseClass Класс лицензии.
 * @return лицензия.
 * @throws Exception
 */
private static License initLicense(String licenseClass) throws Exception {
    Class<?> javaTlsClass = Class.forName(licenseClass);
    return (License) javaTlsClass.newInstance();
}

/**
 * CheckConfFull [-servDir C:/*.*]
 * <p/>
 * <DL> <DT><b> -servDir </b>  <DD>рабочая директория<DD>(по умолчанию
 * текущая)</DD></DT> </DL>
 *
 * @param args аргументы командной строки
 * @throws IOException /
 */
public static void main(String[] args) throws Exception {

    LocalFileHandler logHandler = new LocalFileHandler();
    log.addHandler(logHandler);

    CheckStatus status;
    boolean isJavaTlsServer = true;

    if (ComLine.getFunc(ComLine.help, args)) {
        log.info(ComLine.CheckConfFullHelp);
    }
    else {

        // Использование симулятора ввода.

        final String useBioSimulatorString = ComLine.getBooleanValue(ComLine.bioSimulator, args, "false");
        final boolean useBioSimulator = Boolean.parseBoolean(useBioSimulatorString);
        log.info("Use bio simulator: " + useBioSimulator + "\n");

        // Упорядочивание провайдеров в ходе проверок.

        final String noOrderProvidersString = ComLine.getValue(ComLine.noOrderProviders, args, "false");
        final boolean orderProviders = !Boolean.parseBoolean(noOrderProvidersString);
        log.info("Order providers: " + orderProviders + "\n");

        // jcp - jcp only
        // java_csp - java csp only
        // all - all

        final String testVolumeValue = ComLine.getValue(ComLine.test, args, "ALL");

        final int ALL_MASK      = 0xff; // all
        final int JCP_MASK      = 0x01; // jcp only
        final int JAVA_CSP_MASK = 0x10; // java csp only

        final String testVolumeString;
        final int TestVolume;

        if (testVolumeValue.equalsIgnoreCase("JCP")) {
            TestVolume = JCP_MASK;
            testVolumeString = "JCP only";
        } // if
        else if (testVolumeValue.equalsIgnoreCase("JAVA_CSP")) {
            TestVolume = JAVA_CSP_MASK;
            testVolumeString = "Java CSP only";
        } // else
        else {
            TestVolume = ALL_MASK;
            testVolumeString = "all";
        } // else

        log.info("Test following provider(s): " + testVolumeString + "\n");

        if (orderProviders) {
            orderProviders(false); // сначала первый JCP
        } // if

        checkProviders(); // состав провайдеров может измениться

        // server working dir

        final String dir = ComLine.getValue(ComLine.servDir, args, new File(".").getCanonicalPath());
        log.info("Working directory: " + dir + "\n");

        if (f_jtls & !f_sspi) { // all

            // JCP-2324: не проверяем серверность лицензии Java TLS, если его нет.

            log.info("Checking Java TLS license...\n");
            java_tls_license: {

                License serverLicense;
                final String JAVA_TLS_LICENSE_CLASS = "ru.CryptoPro.ssl.ServerLicense";

                try {
                    serverLicense = initLicense(JAVA_TLS_LICENSE_CLASS);
                } catch (Exception e) {
                    // Возможно, имеем дело с SSPI провайдером.
                    log.warning("Cannot find or load class " + JAVA_TLS_LICENSE_CLASS + ".");
                    break java_tls_license;
                }

                isJavaTlsServer = serverLicense.isServer();
                log.info("Java TLS license is for " + (isJavaTlsServer ? "server" : "client") + ".\n");

            } // java_tls_license

        } // if

        if (((TestVolume & JCP_MASK) == JCP_MASK) && f_jcp) { // jcp only

            // Генерация контейнеров.

            deleteContainers(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, kpAlg, signAliases);
            gen(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, JCP.PROVIDER_NAME, kpAlg, signAlg, signAliases, false, signAliases, useBioSimulator);

            if (f_crypto || f_jtls) { // + crypto

                deleteContainers(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, kpAlgDH, clientAliases);
                gen(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, CRYPTO_PROVIDER_NAME, kpAlgDH, signAlg, clientAliases, false, clientAliases, useBioSimulator);

                deleteContainers(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, kpAlgDH, serverAliases);
                gen(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, CRYPTO_PROVIDER_NAME, kpAlgDH, signAlg, serverAliases, true, serverCNs, useBioSimulator);

            } // if

            // Проверка подписи.

            status = checkSignature(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME);
            checkedModules.put(JCP.PROVIDER_NAME, getStatusInfo(status));
            logResult(JCP.PROVIDER_NAME, status.getStatus(), getStatusInfo(status));

            if (f_crypto) {

                // Проверка шифрования.

                status = checkEncDec(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, CRYPTO_PROVIDER_NAME);
                checkedModules.put(CRYPTO_PROVIDER_NAME, getStatusInfo(status));
                logResult(CRYPTO_PROVIDER_NAME, status.getStatus(), getStatusInfo(status));

                if (f_jtls) {

                    if (!f_sspi) { // JCP-2324: SSPI TLS не работает с JCP

                        // Java TLS + JCP.

                        prepareTls(dir, JCP.HD_STORE_NAME, JCP.PROVIDER_NAME);

                        // Проверка TLS. Выполняем TLS-проверки только
                        // в том случае, если гарантировано поддерживается
                        // создание серверного сокета, т.е. лицензия Java
                        // TLS - серверная.

                        if (isJavaTlsServer) {

                            License jcpLicense = new License();

                            // Выполняем TLS-проверки только в том случае, если
                            // гарантировано поддерживается создание серверного
                            // сокета, т.е. лицензия JCP - серверная.

                            if (jcpLicense.isServer()) {

                                // Server Java TLS + server JCP.

                                status = checkTLS(false, dir, JCP.HD_STORE_NAME, JCP.PROVIDER_NAME, CRYPTO_PROVIDER_NAME, JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME);
                                checkedModules.put(JTLS_PROVIDER, getStatusInfo(status));
                                logResult(JTLS_PROVIDER, status.getStatus(), getStatusInfo(status));

                            } // if
                            else {

                                // Server Java TLS + client JCP (not work).

                                final String reason = "SKIPPED (reason: JCP has a client license, but a server license is required)";
                                checkedModules.put(JTLS_PROVIDER, reason);
                                logResult(JTLS_PROVIDER, false, reason);

                            } // else

                        } // if
                        else {

                            // Client Java TLS + JCP (not work).

                            final String reason = "SKIPPED (reason: Java TLS has a client license, but a server license is required).";
                            checkedModules.put(JTLS_PROVIDER, reason);
                            logResult(JTLS_PROVIDER, false, reason);

                        } // else

                        clearTls();

                    } // if
                    else {

                        // SSPI TLS + JCP (not supported).

                        final String reason = "SKIPPED (reason: SSPI TLS does not support JCP).";
                        checkedModules.put(JTLS_PROVIDER, reason);
                        logResult(JTLS_PROVIDER, false, reason);

                    } // else

                } // if java tls

            } // if crypto

        } // if jcp

        if (((TestVolume & JAVA_CSP_MASK) == JAVA_CSP_MASK)) { // java csp only

            // Если провайдер по умолчанию - JCP,
            // то перед тестами Java CSP первым
            // ставим Java CSP.

            if (orderProviders) {
                orderProviders(true); // теперь первый Java CSP
            } // if

            checkProviders(); // состав провайдеров может измениться

            if (f_jcsp) {

                final String defaultKeyStore = findDefaultKeyStore();

                // Генерация контейнеров.

                deleteContainers(defaultKeyStore, JCSP_PROVIDER_NAME, kpAlg, signAliases);
                gen(defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME, kpAlg, signAlg, signAliases, false, signAliases, false);

                deleteContainers(defaultKeyStore, JCSP_PROVIDER_NAME, kpAlgDH, clientAliases);
                gen(defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME, kpAlgDH, signAlg, clientAliases, false, clientAliases, false);

                deleteContainers(defaultKeyStore, JCSP_PROVIDER_NAME, kpAlgDH, serverAliases);
                gen(defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME, kpAlgDH, signAlg, serverAliases, true, serverCNs, false);

                // Проверка подписи.

                status = checkSignature(defaultKeyStore, JCSP_PROVIDER_NAME);
                checkedModules.put(JCSP_PROVIDER_NAME + " (Signature)", getStatusInfo(status));
                logResult(JCSP_PROVIDER_NAME + " (Signature)", status.getStatus(), getStatusInfo(status));

                // Проверка шифрования.

                status = checkEncDec(defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME);
                checkedModules.put(JCSP_PROVIDER_NAME + " (Encryption/Decryption)", getStatusInfo(status));
                logResult(JCSP_PROVIDER_NAME + " (Encryption/Decryption)", status.getStatus(), getStatusInfo(status));

                if (f_jtls) {

                    prepareTls(dir, defaultKeyStore, JCSP_PROVIDER_NAME);

                    if (!f_sspi) {

                        // Java TLS + Java CSP.

                        // Проверка TLS. Выполняем TLS-проверки только
                        // в том случае, если гарантировано поддерживается
                        // создание серверного сокета, т.е. лицензия Java
                        // TLS - серверная.

                        if (isJavaTlsServer) {

                            // Server Java TLS + Java CSP.

                            final String JAVA_CSP_LICENSE_CLASS = "ru.CryptoPro.JCSP.JCSPLicense";
                            License javaCspLicense = initLicense(JAVA_CSP_LICENSE_CLASS);

                            // Выполняем TLS-проверки только в том случае, если
                            // гарантировано поддерживается создание серверного
                            // сокета, т.е. лицензии CSP и Java CSP - серверные.

                            if (javaCspLicense.isServer()) {

                                // Server Java TLS + server Java CSP.

                                status = checkTLS(true, dir, defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME, JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME);
                                checkedModules.put(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", getStatusInfo(status));
                                logResult(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", status.getStatus(), getStatusInfo(status));

                            } // if
                            else {

                                // Server Java TLS + client Java CSP (not work).

                                final String reason = "SKIPPED (reason: Java CSP has a client license, but a server license is required)";
                                checkedModules.put(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", reason);
                                logResult(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", false, reason);

                            } // else

                        } // if
                        else {

                            // Client Java TLS + Java CSP.

                            final String reason = "SKIPPED (reason: Java TLS has a client license, but a server license is required)";
                            checkedModules.put(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", reason);
                            logResult(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", false, reason);

                        } // else

                    } // if java tls
                    else {

                        // Ничего не знаем о лицензиях.

                        status = checkTLS(true, dir, defaultKeyStore, JCSP_PROVIDER_NAME, JCSP_PROVIDER_NAME, JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME);
                        checkedModules.put(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", getStatusInfo(status));
                        logResult(JCSP_PROVIDER_NAME + " (" + JTLS_PROVIDER + ")", status.getStatus(), getStatusInfo(status));

                    } // if sspi tls

                    clearTls();

                } // if

            } // if

        } // if java csp

    } // else

    // Вывод результата.

    log.info("\n*******************************\n");
    String result = "\n";

    for (String key : checkedModules.keySet()) {
        result += String.format(FORMAT_LINE, key, checkedModules.get(key));
    } // for

    log.info(result);
    logHandler.close();

}

/**
 * Очистка кеша декодированных открытых ключей и
 * сертификатов. Если не сделать, то открытый ключ
 * будет взят из кеша, декодированный с помощью
 * другого провайдера.
 *
 */
private static void clearCache() {

    try {
        CertificateFactory.getInstance("X.509").generateCertificate(null);
    } catch (Exception e) {}

    try {
        CertificateFactory.getInstance("X.509").generateCRL(null);
    } catch (Exception e) {}

}

/**
 * Поиск типа хранилища по умолчанию.
 *
 * @return тип хранилища.
 */
private static String findDefaultKeyStore() {

    log.info("Looking for default key store name...\n");

    Provider provider = Security.getProvider(JCSP_PROVIDER_NAME);
    final Set<Provider.Service> services = provider.getServices();

    // Список типов контейнеров.

    for (Provider.Service service : services) {
        String serviceType = service.getType();

        if (serviceType.equalsIgnoreCase("CRYPTO_PRO_KEY_STORE")) {
            String serviceAlgorithm = service.getAlgorithm();

            if (serviceAlgorithm.equals("DEFAULT_NAME")) {

                String name = service.getClassName(); // вместо класса на самом деле имя хранилища
                log.info("Default key store name has been found: " + name + "\n");

                return name;

            } // if

        } // if

    } // for

    String name = ComLine.JCSP_DEFAULT_STORE_TYPE;
    log.info("Default key store name has NOT been found, default is used: " + name + "\n");

    return name;

}

/**
 * Генерация ключевой пары.
 *
 * @param storeType Тип контейнера.
 * @param storeProvider Провайдер контейнера.
 * @param genProvider Провайдер ключа.
 * @param algList Список алгоритмов ключей.
 * @param sigAlgList Список алгоритмов подписи.
 * @param aliases Список алиасов.
 * @param isServer Флаг серверного сертификата.
 * @throws Exception
 */
private static void gen(String storeType, String storeProvider, String
    genProvider, String[] algList, String[] sigAlgList, String[] aliases,
    boolean isServer, String[] CNs, boolean useBioSimulator) throws Exception {

    log.info("Prepare containers");
    for (int i = 0; i < algList.length; i++) {

        String kpAlgorithm = algList[i];
        String signAlgorithm = sigAlgList[i];
        String alias = aliases[i];
        char[] password = f_sspi ? null : passwords[i];
        String cn = CNs[i];

        log.info("*** Check " + genProvider + " key pair generation on " +
            kpAlgorithm + " with signature algorithm " + signAlgorithm +
                " and writing to store");

        final String dName = dNameCN + cn + dNameE;

        if (keyGen(storeType, storeProvider, genProvider, alias,
            kpAlgorithm, signAlgorithm, password, dName, isServer, useBioSimulator)) {

            log.info("*** Check " + genProvider + " key pair generation on " +
                kpAlgorithm + " with signature algorithm " + signAlgorithm +
                    " and writing to store - OK");

        } // if

    } // for

    log.info("Containers created");

}

/**
 * Подготовка хранилищ доверенных сертификатов для TLS.
 *
 * @param dir Папка сервера.
 * @param storeType Тип контейнера.
 * @param provider Провайдер.
 * @throws Exception
 */
private static void prepareTls(String dir, String storeType, String provider) throws Exception {

    log.info("*** Prepare files");

    doc = dir + File.separator + in;
    get_doc = dir + File.separator + out;
    get_doc_a = dir + File.separator + out_a;

    Array.writeFile(doc, "12345".getBytes());

    if (!new File(doc).exists()) {
        throw new Exception("Can't create file for test");
    } // if

    log.info("*** Prepare TrustStore");

    keySt = dir + File.separator + key_store;
    createTrustStore(storeType, provider, JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME, keySt, clientAliases);

    trustSt = dir + File.separator + server_store;
    createTrustStore(storeType, provider, JCP.CERT_STORE_NAME, JCP.PROVIDER_NAME, trustSt, serverAliases);

    log.info("*** TrustStore created");

}

/**
 * Удаление промежуточных данных и хранилищ доверенных сертификатов,
 * используемых в TLS тесте.
 */
private static void clearTls() {
    log.info("*** Delete trust stores ***");
    if((new File(keySt)).delete())
        log.info("*** " + keySt +  " TrustStore deleted.");
    if((new File(trustSt)).delete())
        log.info("*** " + trustSt +  " TrustStore deleted.");
    log.info("*** Delete files ***");
    if ((new File(doc)).delete())
        log.info("*** " + doc +  " deleted.");
    // в таком формате записывает tlss
    if ((new File(get_doc + in)).delete())
        log.info("*** " + get_doc + in +  " deleted.");
    if ((new File(get_doc_a + in)).delete())
        log.info("*** " + get_doc_a + in +  " deleted.");
}
/**
 * Запись в лог сообщения о проверке.
 *
 * @param module Имя модуля для вывода в лог.
 * @param status Статус проверки.
 */
private static void logResult(String module, Boolean status, String info) {
    if (status) {
        log.info(String.format("\n" + FORMAT_LINE, module, info));
    } else {
        log.warning(String.format("\n" + FORMAT_LINE, module, info));
    }
}

/**
 * Проверка установки провайдеров.
 *
 */
private static void checkProviders() {

    final Provider[] providers = Security.getProviders();
    for (int i = 0; i < providers.length; i++) {

        if (JCP_PROVIDER_NAME.equals(providers[i].getName()))    f_jcp = true;
        if (CRYPTO_PROVIDER_NAME.equals(providers[i].getName())) f_crypto = true;
        if (JTLS_PROVIDER.equals(providers[i].getName()))        f_jtls = true;
        if (JCSP_PROVIDER_NAME.equals(providers[i].getName()))   f_jcsp = true;

    } // for

    if (!f_jcp)    log.info("Provider " + JCP_PROVIDER_NAME + " not installed");
    if (!f_crypto) log.info("Provider " + CRYPTO_PROVIDER_NAME + " not installed");
    if (!f_jtls)   log.info("Provider " + JTLS_PROVIDER + " not installed");
    if (!f_jcsp)   log.info("Provider " + JCSP_PROVIDER_NAME + " not installed");
    if (f_jtls)    f_sspi = Security.getProvider(JTLS_PROVIDER).getInfo().contains("SSPI");

}

/**
 * Удаление контейнера.
 *
 * @param storeType Тип ключевого контейнера.
 * @param provider Провайдер для работы с контейнером.
 * @param alias Алиас удаляемого ключа (контейнера).
 */
private static void deleteContainer(String storeType, String provider, String alias) {

    log.info("### Delete container " + alias + " by using " + " store provider " + provider + " ###");

    try {

        KeyStore keyStore = KeyStore.getInstance(storeType, provider);
        keyStore.load(null, null);

        keyStore.deleteEntry(alias);
        log.info("Deleting of " + alias + " completed");

    } catch (KeyStoreException e) {
        log.warning("Couldn't delete container: " + alias + "(" + e.getMessage() + ")");
    } catch (NoSuchProviderException e) {
        log.warning("Couldn't delete container: " + alias + "(" + e.getMessage() + ")");
    } catch (CertificateException e) {
        log.warning("Couldn't delete container: " + alias + "(" + e.getMessage() + ")");
    } catch (NoSuchAlgorithmException e) {
        log.warning("Couldn't delete container: " + alias + "(" + e.getMessage() + ")");
    } catch (IOException e) {
        log.warning("Couldn't delete container: " + alias + "(" + e.getMessage() + ")");
    }

}

/**
 * Проверка создания подписи.
 *
 * @param storeType Тип ключевого контейнера.
 * @param provider Провайдер для подписи и работы с
 * контейнером.
 * @return True, если проверка выполнена успешно.
 */
private static CheckStatus checkSignature(String storeType, String provider) {

    log.info("### Check singing and verifying by using provider " + provider + " ###");

    boolean ok = true;
    boolean oneCheck = true;
    StringBuilder description = new StringBuilder();

    for (int i = 0; i < kpAlg.length; i++) {

        String signAlgorithm = signAlg[i];
        String signKey = signAliases[i];
        char[] password = f_sspi ? null : passwords[i];

        long start = System.currentTimeMillis();

        try {

            log.info("*** Check " + provider + " signature and " +
                "verification with signature algorithm " + signAlgorithm);

            if (signVer(storeType, provider, signAlgorithm, signKey, password)) {

                log.info("*** Check " + provider + " with signature algorithm " +
                    signAlgorithm + " generation|verification - OK");

            } // if
            else {

                log.info("*** Check " + provider + " with signature algorithm " +
                    signAlgorithm + " generation|verification - FAILURE");

            } // else

        } catch (Exception e) {

            ok = false;
            oneCheck = false;

            e.printStackTrace();
            log.log(Level.SEVERE, "Exception", e);

        }

        long end = System.currentTimeMillis();

        deleteContainer(storeType, provider, signKey); // удаляем
        description.append(String.format(FORMAT_ITEM, ALG_NAMES[i], (oneCheck ? OK : FAIL), end - start));

        oneCheck = true;

    } // for

    return new CheckStatus(ok, description.toString());

}

/**
 * Функция удаляет все контейнеры.
 * @param storeType Тип контейнера.
 * @param storeProvider Провайдер контейнера.
 * @param algList Список алгоритмов ключей.
 * @param aliases Список алиасов.
 */
private static void deleteContainers(String storeType, String storeProvider,
    String[] algList, String[] aliases) {

    try {

        KeyStore keyStore = KeyStore.getInstance(storeType, storeProvider);
        keyStore.load(null, null);

        log.info("Prepare containers");
        for (int i = 0; i < algList.length; i++) {

            if (keyStore.containsAlias(aliases[i])) {
                log.info("*** Container " + aliases[i] + " exists. Delete");
                keyStore.deleteEntry(aliases[i]);
                log.info("Deleting of " + aliases[i] + " completed");
            } // if

        }

    } catch (KeyStoreException e) {
            log.warning("Couldn't delete containers " + "(" + e.getMessage() + ")");
    } catch (NoSuchProviderException e) {
            log.warning("Couldn't delete containers " + "(" + e.getMessage() + ")");
    } catch (CertificateException e) {
            log.warning("Couldn't delete containers " + "(" + e.getMessage() + ")");
    } catch (NoSuchAlgorithmException e) {
            log.warning("Couldn't delete containers " + "(" + e.getMessage() + ")");
    } catch (IOException e) {
            log.warning("Couldn't delete containers " + "(" + e.getMessage() + ")");
    }
}

/**
 * Проверка возможности шифрования.
 *
 * @param storeType Тип ключевого контейнера.
 * @param storeProvider Провайдер для работы с контейнером.
 * @param edProvider Провайдер для шифрования (генерации
 * ключевой пары).
 * @return True, если проверка выполнена успешно.
 */
private static CheckStatus checkEncDec(String storeType, String storeProvider, String edProvider) {

    log.info("### Check encryption and decryption by using provider " + edProvider + " ###");

    boolean ok = true;
    boolean oneCheck = true;

    StringBuilder description = new StringBuilder();

    // Проверяем обычную схему шифрования для всех алгоритмов.

    for (int i = 0; i < cipherAlg.length; i++) {

        long start = System.currentTimeMillis();

        try {

            log.info("*** Check " + edProvider + " data encryption with key algorithm " + cipherAlg[i]);

            if (encryptDecryptSimple(
                cipherAlg[i],
                cipherAlg[i].equalsIgnoreCase(JCP.GOST_K_CIPHER_NAME) ? 16 : 8, // IV length
                storeProvider,
                edProvider)) {

                log.info("*** Check " + edProvider + " encryption and decryption (" + cipherAlg[i] + ") - OK");

            } // if

        } catch (Exception e) {

            ok = false;
            oneCheck = false;

            e.printStackTrace();
            log.log(Level.SEVERE, "Exception", e);

        }

        long end = System.currentTimeMillis();

        description.append(String.format(FORMAT_ITEM, cipherAlg[i], (oneCheck ? OK : FAIL), end - start));
        oneCheck = true;

    } // for

    // проверяем схему шифрования на ключах согласования для ГОСТ 28147

    for (int i = 0; i < kpAlgDH.length; i++) {

        String kpAlgorithmDH = kpAlgDH[i];
        String alisaDhKey = clientAliases[i];
        String bobDhKey = serverAliases[i];
        char[] password = f_sspi ? null : passwords[i];

        long start = System.currentTimeMillis();

        try {

            log.info("*** Check " + edProvider + " encryption with key algorithm " + kpAlgorithmDH);

            if (encryptDecrypt(
                storeType,
                storeProvider,
                storeProvider,
                edProvider,
                alisaDhKey,
                bobDhKey,
                password)) {

                log.info("*** Check " + edProvider + " encryption and decryption (" + kpAlgDH[i] + ") - OK");

            } // if

        } catch (Exception e) {

            ok = false;
            oneCheck = false;

            e.printStackTrace();
            log.log(Level.SEVERE, "Exception", e);

        }

        long end = System.currentTimeMillis();

        if (!f_jtls) {
            deleteContainer(storeType, storeProvider, bobDhKey);
            deleteContainer(storeType, storeProvider, alisaDhKey);
        } // if

        description.append(String.format(FORMAT_ITEM, ALG_NAMES[i], (oneCheck ? OK : FAIL), end - start));
        oneCheck = true;

    } // for

    return new CheckStatus(ok, description.toString());

}

/**
 * Проверка TLS.
 *
 * @param JCSPEnabled Флаг использования Java CSP.
 * @param dir Папка сервера.
 * @param keyStoreType Тип ключевого контейнера.
 * @param keyStoreProvider Провайдер для работы с контейнером.
 * @param keyGenProvider Провайдер для генерации ключевой пары.
 * @param trustStoreType тип хранилища доверенных сертификатов.
 * @param trustStoreProvider Провайдер для работы с хранилищем
 * доверенных сертификатов.
 * @return True, если проверка выполнена успешно.
 */
private static CheckStatus checkTLS(boolean JCSPEnabled, String dir,
    String keyStoreType, String keyStoreProvider, String keyGenProvider,
    String trustStoreType, String trustStoreProvider) {

    log.info("### Check TLS by using key provider " + keyGenProvider +
        " and store provider " + keyStoreProvider + " ###");

    boolean ok = true; // полная проверка
    StringBuilder description = new StringBuilder();

    for (int i = 0; i < kpAlgDH.length; i++) {

        String kpAlgorithmDH = kpAlgDH[i];
        String signAlgorithm = signAlg[i];
        boolean onePairCheck = false; // результат проверки пары
        String serverKey = serverAliases[i];
        String clientKey = clientAliases[i];
        char[] password = f_sspi ? null : passwords[i];

        log.info("*** Check TLS with key algorithm " + kpAlgorithmDH +
            " and signature algorithm " + signAlgorithm);

        long start = System.currentTimeMillis();

        try {

            log.info("*** Test ssl without authentication of client");

            boolean no_auth = tls(JCSPEnabled, keyStoreType, keyStoreProvider,
                trustStoreType, false, dir, get_doc, serverKey, clientKey, password);

            if (no_auth) {
                log.info("*** " + keyStoreProvider + " and " + keyGenProvider +
                    " tls check ssl without authentication of client - OK");
            } // if

            onePairCheck = no_auth;
            log.info("*** Test ssl with authentication of client");

            boolean client_auth = tls(JCSPEnabled, keyStoreType, keyStoreProvider,
                trustStoreType, true, dir, get_doc_a, serverKey, clientKey, password);

            if (client_auth) {
                log.info("*** " + keyStoreProvider + " and " + keyGenProvider +
                    " tls check ssl with authentication of client - OK");
            } // if

            onePairCheck &= client_auth;

        } catch (Exception e) {

            e.printStackTrace();
            log.log(Level.SEVERE, "Exception", e);

        }

        long end = System.currentTimeMillis();
        ok &= onePairCheck;

        deleteContainer(keyStoreType, keyStoreProvider, serverKey);
        deleteContainer(keyStoreType, keyStoreProvider, clientKey);

        description.append(String.format(FORMAT_ITEM, ALG_NAMES[i], (onePairCheck ? OK : FAIL), end - start));

    } // for

    return new CheckStatus(ok, description.toString());

}

/**
 * Создание хранилища доверенных сертификатов.
 *
 * @param keyStoreType Тип ключевого контейнера.
 * @param keyStoreProvider Провайдер для работы с контейнером.
 * @param trustStoreType тип хранилища доверенных сертификатов.
 * @param trustStoreProvider Провайдер для работы с хранилищем
 * доверенных сертификатов.
 * @param stPath Путь к будущему хранилищу.
 * @param aliases Список алиасов сертификатов для получения из
 * ключевого контейнера.
 */
private static void createTrustStore(String keyStoreType,
    String keyStoreProvider, String trustStoreType, String
    trustStoreProvider, String stPath, String[] aliases)
    throws Exception {

    log.info("### Create trusted store by using store provider " + trustStoreProvider);

    // Загрузка сертификата из контейнера.

    log.info("Loading of a certificate with type " + keyStoreType + " by using provider " + keyStoreProvider);

    final KeyStore ks = KeyStore.getInstance(keyStoreType, keyStoreProvider);
    ks.load(null, null);

    final KeyStore ts = KeyStore.getInstance(trustStoreType, trustStoreProvider);
    ts.load(null, null);

    for (String alias : aliases) {

        final Certificate cert = ks.getCertificate(alias);

        if (cert == null) {
            throw new Exception("Certificate named \"" + alias + "\" not found");
        } // if

        log.info("Loading of a certificate completed");

        // Создание хранилища сертификатов.

        log.info("Creating of a certificate store with type " + trustStoreType + " by using provider " + trustStoreProvider);
        ts.setCertificateEntry(alias, cert);

    }

    try (FileOutputStream os = new FileOutputStream(stPath)) {
        ts.store(os, trustPass);
        log.info("Certificate was moved to store");
    }

}

/**
 * Выполнение TLS соединения.
 *
 * @param keyStoreType Тип ключевого контейнера.
 * @param provider Провайдер для работы с контейнером.
 * @param trustStoreType тип хранилища доверенных сертификатов.
 * @param isAuth True, если требуется аутентификация клиента.
 * @param dir Папка сервера для передачи файлов.
 * @param outPath Папка клиента для сохранения файла.
 * @param clientAlias Алиас клиента.
 * @param serverAlias Алиас сервера.
 * @param password Пароль к ключам.
 * @return True, если соединение выполнено успешно.
 */
private static boolean tls(boolean JCSPEnabled, String keyStoreType,
    String provider, String trustStoreType, boolean isAuth, String dir,
    String outPath, String serverAlias, String clientAlias, char[]
    password) throws Exception {

    log.info("*** Create TLS connection by using provider " + provider +
        " (client authentication - " + isAuth + ")");

    boolean ok = false;
    Server server = null;

    try {

        // Задаем провайдер Java CSP в качестве провайдера по
        // умолчанию для Java TLS, если необходимо.

        log.info("ENABLE " + (JCSPEnabled ? "Java CSP" : "JCP") + " provider for TLS");

        if (JCSPEnabled) {
            cpSSLConfig.setDefaultSSLProvider(JCSP_PROVIDER_NAME);
        } // if
        else {
            // Если в панели JCP на вкладке "Алгоритмы" будет стоять
            // провайдер Java CSP как провайдер по умолчанию, то если
            // default SSL provider не задан (null), может быть взят
            // провайдер из панели, т.к. не задали ни с помощью
            // setDefaultSSLProvider, ни System.setProperty.
            cpSSLConfig.setDefaultSSLProvider(JCP.PROVIDER_NAME);
        } // else

        // Настройки сервера: параметры соединения.

        log.info("Prepare server connection");

        SSLConfiguration serverSslConfig = new SSLConfiguration(
            JCSPEnabled,
            trustStoreType,
            keySt,
            trustPass,
            isAuth,
            keyStoreType,
            serverAlias,
            password
        );

        server = new Server();
        server.create(serverSslConfig, sslPort, dir, ComLine.GOST_TLS, false);
        server.setTimeout(10000000);
        server.start();

        // Настройки клиента: параметры соединения и SSL-контекст.

        log.info("Prepare client connection");

        SSLConfiguration clientSslConfig = new SSLConfiguration(
            JCSPEnabled,
            trustStoreType,
            trustSt,
            trustPass,
            isAuth,
            keyStoreType,
            clientAlias,
            password
        );

        SSLConnector clientSslConn = new SSLConnector(clientSslConfig);
        clientSslConn.prepare(false);

        SSLContext clientSslContext = clientSslConn.create(ComLine.GOST_TLS_12);

        final Client client = new Client("localhost", sslPort);
        client.setTimeout(10000000);

        if (!server.isAlive()) {
            throw new Exception("server not running");
        } // if
        else {
            log.info("server started");
        } // else

        log.info("Execute client request");

        if (client.get(clientSslContext, in, outPath, dir) != 0) {
            throw new IOException("Couldn't get data.");
        } // if

        if (!server.isAlive()) {
            throw new Exception("server not alive after client Get");
        } // if
        else {
            log.info("GET complete");
        } // else

        ok = true;

    } catch (Exception e) {

        e.printStackTrace();
        log.log(Level.SEVERE, "Exception", e);

    } finally {

        if (server == null) {
            throw new Exception("server not running at the test");
        } // if

        server.stop();
        Thread.sleep(100);

    }

    return ok;

}

/**
 * Генерирование ключевой пары и запись в хранилище.
 *
 * @param storeType Тип ключевого контейнера.
 * @param storeProvider Провайдер для работы с контейнером.
 * @param provider Провайдер для генерации пары.
 * @param alias Алиас закрытого ключа для сохранения в
 * контейнере.
 * @param keyPairAlgorithm Алгоритм генерации пары.
 * @param signatureAlgorithm Алгоритм подписи.
 * @param keyPass Пароль для сохранения закрытого ключа.
 * @param name DN-имя в для генерации сертификата.
 * @param isServer True, если генерируется серверный ключ.
 * @return True, если ключи сгенерированы и сохранены.
 */
private static boolean keyGen(String storeType, String
    storeProvider, String provider, String alias, String
    keyPairAlgorithm, String signatureAlgorithm, char[]
    keyPass, String name, boolean isServer, boolean useBioSimulator) throws Exception {

    KeyStore keyStore = KeyStore.getInstance(storeType, storeProvider);
    keyStore.load(null, null);

    if (keyStore.containsAlias(alias)) {

        log.info("*** Container " + alias + " exists. Continue");
        return true;

    } // if

    log.info("*** Generate key pair using algorithm " + keyPairAlgorithm + " of provider " + provider);

    BioSimulator bioSimulator = null;

    // Генерирование ключевой пары.

    final KeyPairGenerator kg = KeyPairGenerator.getInstance(keyPairAlgorithm, provider);
    final KeyPair pair;

    try {
        if (useBioSimulator) {
            bioSimulator = BioDetector.createBioSimulator();
            bioSimulator.init();
        } // if
        pair = kg.generateKeyPair();
    } finally {
        if (bioSimulator != null) {
            bioSimulator.release();
        } // if
    }

    log.info("Generation of key pair completed");

    // Запрос на сертификат.

    final GostCertificateRequest req = new GostCertificateRequest(storeProvider);
    req.init(keyPairAlgorithm, isServer);

    final byte[] encodedCert = req.getEncodedSelfCert(pair, name, signatureAlgorithm);
    log.info("Certificate request completed");

    // Генерирование самоподписанного сертификата.

    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate[] certs = new Certificate[1];

    certs[0] = cf.generateCertificate(new ByteArrayInputStream(encodedCert));
    log.info("Generation of " + (isServer ? "server" : "client") + " certificate completed");

    // Запись в хранилище ключевой пары с самоподписанным сертификатом.

    final PrivateKey key = pair.getPrivate();
    if (provider.equalsIgnoreCase(JCSP_PROVIDER_NAME)) {

        log.info("Save key pair by using " + JCSP_PROVIDER_NAME);
        final KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection(keyPass);

        final KeyStore.Entry entry = new JCPPrivateKeyEntry(key, certs);
        keyStore.setEntry(alias, entry, protectedParam);

    } // if
    else {
        keyStore.setKeyEntry(alias, key, keyPass, certs);
    } // else

    keyStore.store(null, null);

    log.info("Recording of a private key named \"" + alias + "\" with type " +
        storeType + " by using " + storeProvider + " completed");

    return true;

}

/**
 * Создание и проверка подписи.
 *
 * @param keyStoreType Тип ключевого контейнера.
 * @param provider Провайдер для работы с контейнером
 * и создания/проверки подписи.
 * @param signAlgorithm Алгоритм подписи.
 * @param alias Алиас ключа/сертификата для подписи/проверки.
 * @param key_pass Пароль для доступа к ключу.
 * @return True, если подпись верна, иначе false.
 */
private static boolean signVer(String keyStoreType, String provider,
    String signAlgorithm, String alias, char[] key_pass) throws Exception {

    log.info("*** Sign data and verify signature by using key " +
        alias + " on algorithm " + signAlgorithm + " of provider " +
            provider);

    // Загрузка ключа из хранилища.

    final KeyStore ks = KeyStore.getInstance(keyStoreType, provider);
    ks.load(null, null);

    PrivateKey privateKey = null;
    Certificate cert = null;

    if (provider.equalsIgnoreCase(JCSP_PROVIDER_NAME)) {

        log.info("Save key pair by using " + JCSP_PROVIDER_NAME);

        final KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection(key_pass);
        final JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) ks.getEntry(alias, protectedParam);

        privateKey = entry.getPrivateKey();
        cert = entry.getCertificate();

    } // if
    else {

        privateKey = (PrivateKey) ks.getKey(alias, key_pass);
        cert = ks.getCertificate(alias);

    } // else

    if (privateKey == null) {
        throw new Exception("Key named \"" + alias + "\" not found");
    } // if

    log.info("Loading of a private key completed");

    // Текст.

    final byte[] text = "sample".getBytes();
    log.info("Loading of a text completed");

    // Генерирование ЭЦП.

    final java.security.Signature sig = java.security.Signature.getInstance(signAlgorithm, provider);
    sig.initSign(privateKey);

    sig.update(text);
    final byte[] signature = sig.sign();

    log.info("Generation of the signature completed");

    // Загрузка открытого ключа из хранилища.

    ks.load(null, null);
    final PublicKey publicKey = cert.getPublicKey();

    if (publicKey == null) {
        throw new Exception("Key named \"" + alias + "\" not found");
    } // if

    log.info("Loading of a public key completed");

    // Проверка подписи.

    final java.security.Signature sign = java.security.Signature.getInstance(signAlgorithm, provider);
    sign.initVerify(publicKey);

    sign.update(text);
    final boolean verifies = sign.verify(signature);

    final String s;
    if (verifies) {
        s = "The signature is valid";
    } // if
    else {
        s = "The signature is not invalid";
    } // else

    log.info(s);
    return verifies;

}

/**
 * Зашифрование и расшифрование данных на ключах согласования.
 *
 * @param storeType Тип ключевого контейнера.
 * @param storeProvider Провайдер для работы с контейнером
 * и создания/проверки подписи.
 * @param rndProvider Провайдер для получения случайной
 * последовательности (IV).
 * @param edProvider Провайдер шифрования.
 * @param alisaDhKey Алиас ключа отправителя.
 * @param bobDhKey Алиас ключа получателя.
 * @param password Пароль к ключам.
 * @return True, если шифрование выполнено успешно, иначе
 * false.
 * @throws Exception
 */
private static boolean encryptDecrypt(String storeType,
    String storeProvider, String rndProvider, String
    edProvider, String alisaDhKey, String bobDhKey,
    char[] password) throws Exception {

    // Текст.

    final byte[] SAMPLE_TEXT = "Classic encryption/decryption".getBytes();

    // Длина вектора.

    final int RND_LENGTH = 8;

    // Алгоритм шифрования.

    final String CIPHER_ALG = "GOST28147/CNT/NoPadding";
    log.info("*** Encrypt and decrypt data on algorithm " + CIPHER_ALG + " by using provider " + edProvider);

    // Загрузка ключей Алисы из хранилища.

    log.info("Prepare Alisa's container");

    final KeyStore ks = KeyStore.getInstance(storeType, storeProvider);
    ks.load(null, null);

    PrivateKey alisaPrivateKey = null;
    Certificate alisaCert = null;

    if (storeProvider.equalsIgnoreCase(JCSP_PROVIDER_NAME)) {

        log.info("Loading key pair by using " + JCSP_PROVIDER_NAME);

        final KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection(password);
        final JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)ks.getEntry(alisaDhKey, protectedParam);

        alisaPrivateKey = entry.getPrivateKey();
        alisaCert = entry.getCertificate();

    } // if
    else {

        alisaPrivateKey = (PrivateKey) ks.getKey(alisaDhKey, password);
        alisaCert = ks.getCertificate(alisaDhKey);

    } // else

    if (alisaPrivateKey == null) {
        throw new Exception("Key named \"" + alisaDhKey + "\" not found");
    } // if

    if (alisaCert == null) {
        throw new Exception("Certificate named \"" + alisaDhKey + "\" not found");
    } // if

    log.info("Loading of Alisa's private key and certificate completed");

    // Загрузка ключей Боба из хранилища.

    log.info("Prepare Bob's container");

    PrivateKey bobPrivateKey = null;
    Certificate bobCert = null;

    if (storeProvider.equalsIgnoreCase(JCSP_PROVIDER_NAME)) {

        log.info("Loading key pair by using " + JCSP_PROVIDER_NAME);

        final KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection(password);
        final JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) ks.getEntry(bobDhKey, protectedParam);

        bobPrivateKey = entry.getPrivateKey();
        bobCert = entry.getCertificate();

    } // if
    else {

        bobPrivateKey = (PrivateKey)ks.getKey(bobDhKey, password);
        bobCert = ks.getCertificate(bobDhKey);

    } // else

    if (bobPrivateKey == null) {
        throw new Exception("Key named \"" + bobDhKey + "\" not found");
    } // if

    if (bobCert == null) {
        throw new Exception("Certificate named \"" + bobDhKey + "\" not found");
    } // if

    log.info("Loading of Bob's private key and certificate completed");

    // Генерирование начальной синхропосылки для выработки ключа согласования.

    final byte[] sv = new byte[RND_LENGTH];
    final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG, rndProvider);

    random.nextBytes(sv);
    final IvParameterSpec ivSpec = new IvParameterSpec(sv);

    log.info("Random sequence is generated");

    // Выработка ключа согласования Алисы c SV.

    final KeyAgreement alisaKeyAgree = KeyAgreement.getInstance(alisaPrivateKey.getAlgorithm(), edProvider);
    alisaKeyAgree.init(alisaPrivateKey, ivSpec, null);

    alisaKeyAgree.doPhase(bobCert.getPublicKey(), true);
    final SecretKey alisaAgree = alisaKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    log.info("Alisa's agreement key completed");

    // Зашифрование текста на ключе согласования Алисы.

    Cipher cipher = Cipher.getInstance(CIPHER_ALG, edProvider);
    cipher.init(Cipher.ENCRYPT_MODE, alisaAgree, (AlgorithmParameterSpec)null, null);

    // Передача вектора инициализации Бобу.

    final byte[] iv = cipher.getIV();
    final byte[] encryptedText = cipher.doFinal(SAMPLE_TEXT, 0, SAMPLE_TEXT.length);

    log.info("Encryption by using of Alisa's key completed");

    // Выработка ключа согласования Боба с тем же SV.

    final KeyAgreement bobKeyAgree = KeyAgreement.getInstance(bobPrivateKey.getAlgorithm(), edProvider);
    bobKeyAgree.init(bobPrivateKey, ivSpec, null);

    bobKeyAgree.doPhase(alisaCert.getPublicKey(), true);
    final SecretKey bobAgree = bobKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    log.info("Bob's agreement key completed");

    // Расшифрование текста на ключе согласования Боба.
    // IV передан от Алисы.

    cipher = Cipher.getInstance(CIPHER_ALG, edProvider);
    cipher.init(Cipher.DECRYPT_MODE, bobAgree, new IvParameterSpec(iv), null);

    final byte[] decryptedText = cipher.doFinal(encryptedText, 0, encryptedText.length);
    log.info("Decryption by using of Bob's key completed");

    // Проверка результата.

    if (decryptedText.length != SAMPLE_TEXT.length) {
        throw new Exception("Invalid length of decrypted data");
    } // if

    for (int i = 0; i < decryptedText.length; i++) {
        if (SAMPLE_TEXT[i] != decryptedText[i]) {
            throw new Exception("Invalid value of decrypted data");
        } // if
    } // for

    log.info("Decrypted data is valid");
    return true;

}

/**
 * Зашифрование и расшифрование данных на случайном ключе.
 *
 * @param cipherAlg Алгоритм шифрования
 * @param ivLen длина вектора инициализации
 * @param storeProvider Провайдер для генерации ключа, IV и подписи.
 * @param edProvider Провайдер шифрования.
 * @return True, если шифрование выполнено успешно, иначе
 * false.
 * @throws Exception
 */
private static boolean encryptDecryptSimple(String cipherAlg, int ivLen,
    String storeProvider, String edProvider) throws Exception {

    // Текст.

    final byte[] SAMPLE_TEXT = "Classic encryption/decryption".getBytes();
    log.info("*** Simple encrypt and decrypt data on algorithm " + cipherAlg + " by using provider " + edProvider);

    // Генерируем симметричный ключ.

    KeyGenerator kg = KeyGenerator.getInstance(cipherAlg, edProvider);
    SecretKey symmetricKey = kg.generateKey();

    log.info("Session key is generated");

    // Генерирование начальной синхропосылки для
    // выработки ключа согласования.

    final byte[] sv = new byte[ivLen];
    final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG, storeProvider);

    random.nextBytes(sv);
    final IvParameterSpec ivSpec = new IvParameterSpec(sv);

    log.info("Random sequence is generated");

    // Зашифрование текста на сессионном ключе.

    Cipher cipher = Cipher.getInstance(cipherAlg +  "/CNT/NoPadding", edProvider);
    cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, ivSpec, null);

    final byte[] encryptedText = cipher.doFinal(SAMPLE_TEXT, 0, SAMPLE_TEXT.length);
    log.info("Encryption is completed");

    // Расшифрование текста на сессионном.

    cipher = Cipher.getInstance(cipherAlg + "/CNT/NoPadding", edProvider);
    cipher.init(Cipher.DECRYPT_MODE, symmetricKey, ivSpec,null);

    final byte[] decryptedText = cipher.doFinal(encryptedText, 0, encryptedText.length);
    log.info("Decryption is completed");

    // Проверка результата.

    if (decryptedText.length != SAMPLE_TEXT.length) {
        throw new Exception("Invalid length of decrypted data");
    } // if

    for (int i = 0; i < decryptedText.length; i++) {
        if (SAMPLE_TEXT[i] != decryptedText[i]) {
            throw new Exception("Invalid value of decrypted data");
        } // if
    } // for

    log.info("Decrypted data is valid");
    return true;

}

}