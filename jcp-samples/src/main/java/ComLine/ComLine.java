/**
 * $RCSfile$
 * version $Revision$
 * created 20.06.2007 17:15:15 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2007.
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProviders;
import ru.CryptoPro.JCP.tools.Platform;
import util.ResolveProvider;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Командная строка
 *
 * @kunina Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class ComLine {
/**
 * logger
 */
private static Logger log = Logger.getLogger("LOGGER");

/**
 * forbidden
 */
private ComLine() {

}

/**
 * Разбор командной строки
 *
 * @param args параметры командной строки
 */
public static void main(String[] args) {
    try {
        final Class aClass = Class.forName("ComLine." + args[0]);
        final Method[] methods = aClass.getMethods();
        Method main = null;
        for (int i = 0; i < methods.length; i++) {
            if ("main".equalsIgnoreCase(methods[i].getName()))
                main = methods[i];
        }
        final Object[] Args = {args};
        if (main != null) {
            main.invoke(null, Args);
        }

    } catch (ArrayIndexOutOfBoundsException e) {
        log.info(HELP);
    } catch (ClassNotFoundException e) {
        log.info("Name of function is case sensitive\n" + HELP);
    } catch (NoClassDefFoundError e) {
        log.info("Name of function is case sensitive\n" + HELP);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * @param com параметр
 * @param arg аргументы командной строки (пары парамтр значение параметра)
 * @param parDef значение параметра по умолчанию
 * @return значение параметра
 */
public static String getValue(String com, String[] arg, String parDef) {
    String par = null;
    int i;
    for (i = 0; i < arg.length - 1; i++)
        if (arg[i].equalsIgnoreCase(com) &&
                !"-".equals(arg[i + 1].substring(0, 1))) {
            par = arg[i + 1];
        }
    if (par == null) par = parDef;

    return par;
}

/**
 * @param com параметр
 * @param arg аргументы командной строки (пары парамтр значение параметра)
 * @param parDef значение параметра по умолчанию
 * @return значение параметра
 */
public static String getBooleanValue(String com, String[] arg, String parDef) {
    String par = null;
    int i;
    for (i = 0; i < arg.length; i++)
        if (arg[i].equalsIgnoreCase(com)) {
            par = "true";
        }
    if (par == null) par = parDef;

    return par;
}

/**
 * @param com параметр
 * @param arg аргументы командной строки (пары парамтр значение параметра)
 * @return присутствует или нет
 */
public static boolean getFunc(String com, String[] arg) {
    int i;
    boolean par = false;
    for (i = 0; i < arg.length; i++)
        if (arg[i].equalsIgnoreCase(com)) {
            par = true;
        }
    return par;
}

//----------------------------------------------------------------------------//
/**
 * Аргументы командной строки
 */
//работа с ключами и сетификатами
public static final String CertStore = "CertStore";
public static final String HDImageStore = "HDImageStore";
public static final String FloppyStore = "FloppyStore";
public static final String RTStore = "RutokenStore";
public static final String J6CFStore = "J6CFStore";

public static final String GOST_TLS = ru.CryptoPro.ssl.Provider.ALGORITHM;
public static final String GOST_TLS_12 = ru.CryptoPro.ssl.Provider.ALGORITHM_12;

public static final String JCSP_HD_IMAGE = ResolveProvider.ALTERNATIVE_HD_IMAGE;
public static final String JCSP_REGISTRY = ResolveProvider.ALTERNATIVE_REGISTRY;

public static final String JCSP_DEFAULT_STORE_TYPE =
    Platform.isWindows() ? JCSP_REGISTRY : JCSP_HD_IMAGE;

public static final String NO_STORE = "NONE";
public static final String ALIAS = "-alias";
public static final String storetype = "-storetype";
public static final String storeprovider = "-storeprovider";
public static final String storepath = "-storepath";
public static final String storepass = "-storepass";
public static final String keypass = "-keypass";
public static final String dname = "-dname";
public static final String signAlgorithm = "-signAlgorithm";
public static final String signpath = "-signpath";
public static final String filepath = "-filepath";
public static final String alg = "-alg";
public static final String keyAlgorithm = "-keyAlgorithm";
public static final String nameEx = "CN=author,OU=Security,O=CryptoPro,C=RU";
public static final String reqCertpath = "-reqCertpath";
public static final String http = "-http";
public static final String certpath = "-certpath";
public static final String certs = "-certs";
public static final String isServer = "-isServer";
public static final String encoding = "-encoding";
public static final String test = "-test";
public static final String bioSimulator = "-simulator";
public static final String noOrderProviders = "-noOrder";
//пуск клиента/сервера
public static final String PORT = "-port";
public static final String SERVER = "-server";
public static final String protocol = "-protocol";
public static final String keyStoreType = "-keyStoreType";
public static final String keyStoreAlias = "-keyStoreAlias";
public static final String trustStoreType = "-trustStoreType";
public static final String trustStorePath = "-trustStorePath";
public static final String trustStorePassword = "-trustStorePassword";
public static final String keyStorePassword = "-keyStorePassword";
public static final String fileget = "-fileget";
public static final String fileout = "-fileout";
public static final String auth = "-auth";
public static final String servDir = "-servDir";
//параметр для разделения потоков хендшейка и чтения (для тестирования sspi)
public static final String separateThread = "-separateThread";
public static final String separateHandshake = "-separateHandshake";
//параметр для досрочного закрытия сокета. Используется совместно с separateThread (для тестирования sspi)
public static final String testClose = "-testClose";
//функции
public static final String HCheckConf = "CheckConf";
public static final String HCheckConfFull = "CheckConfFull";
public static final String HKeyPairGen = "KeyPairGen";
public static final String HSignature = "Signature";
public static final String HSignatureVerif = "SignatureVerif";
public static final String HgetCert = "getCert";
public static final String HCerts = "Certs";
public static final String HServer = "Server";
public static final String HClient = "Client";
//вызов справки
public static final String help = "-help";
/**
 * HELP
 */
public static final String HELP = "HELP\n" +
        "Function" + "        Parameters\n\n" +
        HCheckConf + "        (without parameters)\n" +
        HCheckConfFull + "    <args>\n" +
        HKeyPairGen + "       args\n" +
        HSignature + "        args\n" +
        HSignatureVerif + "   args\n" +
        HgetCert + "          args\n" +
        HCerts + "            args\n" +
        HServer + "           args\n" +
        HClient + "           args\n" +
        "\nargs:\n" +
        help + "              call help\n" +
        "\nCall function for help...\n";

public static final String KeyPairGenHelpHD = "HELP\n" +
        "KeyPairGen \n" +
        ALIAS + "         alias                  (def: no def)\n" +
        storetype + "     storetype              (def: " + JCP.HD_STORE_NAME + ")\n" +
        storepath + "     storepath              (def: null)\n" +
        storepass + "     storepass              (def: null)\n" +
        storeprovider + " storeprovider          (def: " + JCP.PROVIDER_NAME + ")\n" +
        keypass + "       keypass                (def: null)\n" +
        isServer + "      isServer               (def: false)\n" +
        dname + "         subject of certificate (def: no def)\n" +
        reqCertpath + (Platform.isWindows()
                    ? "   C:/*.*                 (def: no def)\n"
                    : "   /home/*.*              (def: no def)\n") +
        encoding + "      der/base64             (def: der)\n" +
        keyAlgorithm + "  key algorithm          (def: " + JCP.GOST_EL_2012_256_NAME + ")\n\n" +
        help + "        call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String SignGenHelpHD = "HELP\n" +
        "Signature\n" +
        ALIAS + "         alias         (def: no def)\n" +
        storetype + "     storetype     (def: " + JCP.HD_STORE_NAME + ")\n" +
        storeprovider + " storeprovider (def: " + JCP.PROVIDER_NAME + ")\n" +
        storepath + "     storepath     (def: null)\n" +
        storepass + "     storepass     (def: null)\n" +
        keypass + "       keypass       (def: null)\n" +
        signpath + (Platform.isWindows()
                 ? "      C:/*.*        (def: no def)\n"
                 : "      /home/*.*     (def: no def)\n") +
        filepath + (Platform.isWindows()
                 ? "      C:/*.*        (def: no def)\n\n"
                 : "      /home/*.*     (def: no def)\n\n") +
        help + "          call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String SignVerHelpHD = "HELP\n" +
        "SignatureVerif\n" +
        ALIAS + "         alias         (def: no def)\n" +
        storetype + "     storetype     (def: " + JCP.HD_STORE_NAME + ")\n" +
        storeprovider + " storeprovider (def: " + JCP.PROVIDER_NAME + ")\n" +
        storepath + "     storepath     (def: null)\n" +
        storepass + "     storepass     (def: null)\n" +
        signpath + (Platform.isWindows()
                 ? "      C:/*.*        (def: no def)\n"
                 : "      /home/*.*     (def: no def)\n") +
        filepath + (Platform.isWindows()
                 ? "      C:/*.*        (def: no def)\n\n"
                 : "      /home/*.*     (def: no def)\n\n") +
        help + "          call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String GetCertHelpHD = "HELP\n" +
        "getCert\n" +
        ALIAS + "         alias                   (def: no def)\n" +
        storetype + "     storetype               (def: " + JCP.HD_STORE_NAME + ")\n" +
        storeprovider + " storeprovider           (def: " + JCP.PROVIDER_NAME + ")\n" +
        storepath + "     storepath               (def: null)\n" +
        storepass + "     storepass               (def: null)\n" +
        http + "          center of certification (def: no def)\n" +
        reqCertpath + (Platform.isWindows()
                    ? "   C:/*.*                  (def: no def)\n"
                    : "   /home/*.*               (def: no def)\n") +
        certpath + (Platform.isWindows()
                 ? "      C:/*.cer                (def: no def)\n"
                 : "      /home/*.*               (def: no def)\n") +
        encoding + "      der/base64              (def: der)\n\n" +
        help + "          call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String CertsHelpHD = "HELP\n" +
        "Certs\n" +
        ALIAS + "         alias                              (def: no def)\n" +
        storetype + "     storetype                          (def: " + JCP.HD_STORE_NAME + ")\n" +
        storeprovider + " storeprovider                      (def: " + JCP.PROVIDER_NAME + ")\n" +
        storepath + "     storepath                          (def: null)\n" +
        storepass + "     storepass                          (def: null)\n" +
        keypass + "       keypass                            (def: null)\n" +
        certs + (Platform.isWindows()
              ? "         C:/my.cer,C:/*.cer,...             (def: no def)\n\n"
              : "         /home/my.cer,/home/*.cer,...       (def: no def)\n\n") +
        help + "          call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String HELP_SERV = "HELP\n" +
        "Server\n" +
        protocol + "           protocol           (def: " + ComLine.GOST_TLS + ")\n" +
        PORT + "               port               (def: 443)\n" +
        auth + "               auth. of client    (def: false)\n" +
        keyStoreType + "       keyStoreType       (def: " + JCP.HD_STORE_NAME + ")\n" +
        trustStoreType + "     trustStoreType     (def: " + JCP.CERT_STORE_NAME + ")\n" +
        trustStorePath + "     trustStorePath     (def: no def)\n" +
        trustStorePassword + " trustStorePassword (def: no def)\n" +
        keyStoreAlias + "      keyStoreAlias      (def: null)\n" +
        keyStorePassword + "   keyStorePassword   (def: null)\n" +
        servDir + "            serverWorkDir      (def: current)\n\n" +
        help + "               call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String HELP_CLIENT = "HELP\n" +
        "Client\n" +
        protocol + "           protocol             (def: " + ComLine.GOST_TLS_12 + ")\n" +
        PORT + "               port                 (def: 443)\n" +
        SERVER + "             server name          (def: \"localhost\")\n" +
        keyStoreType + "       keyStoreType         (def: " + JCP.HD_STORE_NAME + ")\n" +
        trustStoreType + "     trustStoreType       (def: " + JCP.CERT_STORE_NAME + ")\n" +
        trustStorePath + "     trustStorePath       (def: no def)\n" +
        trustStorePassword + " trustStorePassword   (def: no def)\n" +
        keyStoreAlias + "      keyStoreAlias        (def: null)\n" +
        keyStorePassword + "   keyStorePassword     (def: null)\n" +
        fileget + "            name of getting file (def: index.html)\n" +
        fileout + "            path to output file  (def: out.html)\n\n" +
        help + "               call help\n" +
        "\n parameters with (def: no def) must be defined necessarily\n";

public static final String CheckConfFullHelp = "HELP\n" +
        "CheckConfFull\n" +
        servDir + "     serverWorkDir (def: current)\n" +
        test + "        (ALL, or JCP, or JAVA_CSP, def: ALL)\n" +
        help + "        call help\n";

public static String getKeyGenProvider(String keyAlg, String storeProvider) {
    if (storeProvider.equals(JCP.PROVIDER_NAME)) {
        // DH -> Crypto
        if (keyAlg.equalsIgnoreCase(JCP.GOST_EL_DH_NAME) ||
            keyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME) ||
            keyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
            return "Crypto";
        }
        else {
            return storeProvider;
        }

    }
    else {
        return storeProvider; // Java CSP
    }
}

public static String getSignAlgorithm(String keyAlg) {

    if (keyAlg.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
        keyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
        return JCP.GOST_SIGN_2012_256_NAME;
    }

    if (keyAlg.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
        keyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
        return JCP.GOST_SIGN_2012_512_NAME;
    }

    if (keyAlg.equalsIgnoreCase(JCP.ECDH_NAME) ||
        keyAlg.equalsIgnoreCase(JCP.ECDSA_NAME)) {
        return JCP.SIGN_SHA256_ECDSA_NAME;
    }

    if(keyAlg.equalsIgnoreCase(JCP.EDDSA_NAME)) {
        return JCP.SIGN_EDDSA_NAME;
    }

    if (keyAlg.equalsIgnoreCase(JCP.RSA_NAME)) {
        return JCP.SIGN_SHA256_RSA_NAME;
    }

    return JCP.GOST_EL_SIGN_NAME;

}

    /**
     * Проверяет корректность введенного типа хранилища с учетом
     * выбранного провайдера для примеров с ключами и сертификатами.
     * В случае неверного типа хранилища возвращает тип хранилища
     * по умолчанию.
     *
     * @param storeType тип хранилища
     * @param providerName имя провайдера
     * @return корректный тип хранилища
     */
    public static String verifyKeyStoreType(String storeType, String providerName)
    {
        if (providerName.equalsIgnoreCase(DefaultProviders.DEFAULT_PROVIDER_NAME) &&
                (storeType.equalsIgnoreCase(HDImageStore) ||
                        storeType.equalsIgnoreCase(FloppyStore) ||
                        storeType.equalsIgnoreCase(RTStore) ||
                        storeType.equalsIgnoreCase(J6CFStore))){
        } else if (providerName.equalsIgnoreCase(DefaultProviders.ALTERNATIVE_PROVIDER_NAME)  ||
                providerName.equalsIgnoreCase(DefaultProviders.RSA_PROVIDER_NAME) ||
                providerName.equalsIgnoreCase(DefaultProviders.ECDSA_PROVIDER_NAME) ||
                providerName.equalsIgnoreCase(DefaultProviders.EDDSA_PROVIDER_NAME) &&
                (storeType.equalsIgnoreCase(JCSP_HD_IMAGE) ||
                        storeType.equalsIgnoreCase(JCSP_DEFAULT_STORE_TYPE))) {
        } else
        {
            //При неверном вводе присваивается значение по умолчанию (HDImageStore для JCP, REGISTRY для JCSP).
            if (providerName.equalsIgnoreCase(DefaultProviders.DEFAULT_PROVIDER_NAME))
                return HDImageStore;
            if (providerName.equalsIgnoreCase(DefaultProviders.ALTERNATIVE_PROVIDER_NAME) ||
                providerName.equalsIgnoreCase(DefaultProviders.RSA_PROVIDER_NAME) ||
                providerName.equalsIgnoreCase(DefaultProviders.ECDSA_PROVIDER_NAME) ||
                providerName.equalsIgnoreCase(DefaultProviders.EDDSA_PROVIDER_NAME))
                return JCSP_HD_IMAGE;
        }
        //При верном вводе возвращается исходное значение
        return storeType;
    }

    /**
     * Проверяет корректность введенного типа хранилища с учетом
     * выбранного провайдера для примеров с JavaTLS. В случае
     * неверного типа хранилища возвращает тип хранилища по умолчанию.
     *
     * @param storeType тип хранилища
     * @param providerName имя провайдера
     * @return корректный тип хранилища
     */
    public static String verifyKeyStoreTypeJavaTLS(String storeType, String providerName)
    {
        if(storeType.equalsIgnoreCase(NO_STORE))
            return storeType;
        return verifyKeyStoreType(storeType, providerName);
    }

// public static final String HELP_1 = HELP + KeyPairGenHelpHD + SignGenHelpHD +
// SignVerHelpHD + GetCertHelpHD + CertsHelpHD + HELP_SERV + HELP_CLIENT;

}
