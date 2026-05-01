/**
 * $RCSfile$ version $Revision$ created 04.07.2007 14:31:39 by kunina last
 * modified $Date$ by $Author$ (C) ООО Крипто-Про 2004-2007.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package ComLine;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.logging.Logger;

/**
 * Проверка установки и настроек провайдеров.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CheckConf {

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**
     *
     */
    private CheckConf() {
    }

    /**
     * CheckConf (без параметров)
     *
     * @param args без параметров
     * @throws Exception /
     */
    public static void main(String[] args) throws Exception {

        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.HCheckConf + "   (without parameters)");
        else {

            // инициализация провайдеров
            final String provider = ComLine.getValue(ComLine.storeprovider,
                    args, JCP.PROVIDER_NAME);

            JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME));

            main_(args);

        }

    }

    public static void main_(String[] args) throws Exception {

        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.HCheckConf + "   (without parameters)");
        else {
            check();
        }

    }

    /**
     * Проверка установки компонентов
     *
     * @throws Exception ошибки
     */
    private static void check() throws Exception {

        //Проверка наличия провайдеров
        final Provider[] provs = Security.getProviders();
        int flag_jcp = 0;
        int jcp_id = 0;
        int flag_jcsp = 0;
        int jcsp_id = 0;
        int flag_crypto = 0;
        int flag_jtls = 0;
        String s = "";
        for (int i = 0; i < provs.length; i++) {
            //System.out.println(provs[i].getName());
            if ("JCP".equals(provs[i].getName()))
            {
                flag_jcp = 1;
                jcp_id = i;
            }
            if ("JCSP".equals(provs[i].getName()))
            {
                flag_jcsp = 1;
                jcsp_id = i;
            }
            if ("Crypto".equals(provs[i].getName()))
                flag_crypto |= 1;
            if ("JTLS".equals(provs[i].getName()))
                flag_jtls |= 1;
        }
        s += "\n";
        s +=  "Провайдер JCP " + ((flag_jcp == 1) ? "" : "не ") + "найден\n";
        s +=  "Провайдер JCSP " + ((flag_jcsp == 1) ? "" : "не ") + "найден\n";
        if (flag_jcp == 1 && flag_jcsp == 1)
            s +=  ((jcp_id < jcsp_id) ? "JCP" : "JCSP") + " является дефолтным криптопровайдером\n";
        s +=  "Провайдер Crypto " + ((flag_crypto == 1) ? "" : "не ") + "найден\n";
        s +=  "Провайдер JTLS " + ((flag_jtls == 1) ? "" : "не ") + "найден\n";
        log.info(s + "\n\nПроверка настроек:\n");

        // проверка правильности работы JCP
        final String[] sout = new String[7];
        int i = 0;
        if (flag_jcp == 1) {
            try {
                final java.security.Signature sig =
                        java.security.Signature.getInstance("GOST3411withGOST3410EL", "JCP");
                sout[i] = "Провайдер JCP настроен верно";
                i++;
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                sout[i] = "Провайдер JCP настроен неверно.";
                i++;
            }
        }
        //проверка правильности работы JCSP
        if (flag_jcsp == 1) {
            try{
                final java.security.Signature sig =
                        java.security.Signature.getInstance("GOST3411withGOST3410EL", "JCSP");
                sout[i] = "Провайдер JCSP настроен верно";
                i++;
            } catch (NoSuchAlgorithmException | NoSuchProviderException  e) {
                sout[i] = "Провайдер JCSP настроен неверно.";
                i++;
            }
        }
        // проверка правильности работы Crypto
        if (flag_crypto == 1) {
            try {
                final KeyPairGenerator keyGen =
                        KeyPairGenerator.getInstance(JCP.GOST_EL_DH_EPH_NAME, "Crypto");
                sout[i] = "Провайдер Crypto настроен верно";
                i++;
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                sout[i] = "Провайдер Crypto настроен неверно.";
                i++;
            }
        }
        // считывание настроек TLS
        if (flag_jtls == 1) {
            final String factoryProvider =
                    Security.getProperty("ssl.SocketFactory.provider");
            if (factoryProvider != null) {
                sout[i] = "Значение ssl.SocketFactory.provider в java.security: " + factoryProvider;
                i++;
            }
            final String serverProvider = Security
                    .getProperty("ssl.ServerSocketFactory.provider");
            if (serverProvider != null) {
                sout[i] = "Значение ssl.ServerSocketFactory.provider в java.security: " + serverProvider;
                i++;
            }
            final String keyManager = Security
                    .getProperty("ssl.KeyManagerFactory.algorithm");
            if (keyManager != null) {
                sout[i] = "Значение ssl.KeyManagerFactory.algorithm в java.security: " + keyManager;
                i++;
            }
            final String trust = Security
                    .getProperty("ssl.TrustManagerFactory.algorithm");
            if (trust != null) {
                sout[i] = "Значение ssl.TrustManagerFactory.algorithm в java.security: " + trust;
                i++;
            }
        }

        final int imax = i - 1;
        String Sout = "\n" + sout[0];
        for (i = 1; i <= imax; i++) {
            if (sout[i] !=null)
                Sout = Sout + "\n" + sout[i];
        }
        log.info(Sout + "\n");
    }
}
