/**
 * $RCSfile$ version $Revision$ created 25.06.2007 14:29:44 by kunina last
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Проверка электронной цифровой подписи в соответствии с алгоритмами ГОСТ Р
 * 34.10-94 и ГОСТ Р 34.10-2001.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class SignatureVerif {

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**
     * forbidden.
     */
    private SignatureVerif() {
    }

    /**
     * SignatureVerif -alias name_of_key [-storetype HDImageStore] [-storepath null]
     * [-storepass null] -signpath C:/*.* -filepath C:/*.*
     * <br>
     * </DD> <DL> <DT><b> -alias </b>  <DD>уникальное имя ключа</DD> <DT><b>
     * -keypass </b> <DD>пароль на записываемый ключ <DD>(по умолчанию null)<br>
     * <DT><b> -storetype </b> <DD>имя ключевого носителя HDImageStore (жесткий
     * диск), FloppyStore (дискета), OCFStore или J6CFStore (карточки), RutokenStore (Рутокен) <DD>(по
     * умолчанию HDImageStore)</DD> <DT><b>-storepath </b> <DD>путь к хранилищу
     * доверенных сертификатов <DD>(по умолчанию null)</DD> <DT><b> -storepass </b>
     * <DD>пароль на хранилище доверенных сертификатов <DD>(по умолчанию null)</DD>
     * <DT><b> -signpath </b> <DD>путь к файлу подписи </DD> <DT><b> -filepath</b>
     * <DD>путь к проверяемому файлу</DD></DT> </DL>
     *
     * @param args пары аргументов командной строки
     */
    public static void main(String[] args) {

        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.SignVerHelpHD);
        else {

            //Заполнение таблицы свойств значениями параметров командной строки
            //или значениями по умолчанию при отсутствии первых
            final Properties ArgList = new Properties();

            //провайдер ключевого носителя и подписи
            ArgList.setProperty(ComLine.storeprovider,
                    ComLine.getValue(ComLine.storeprovider, args,
                            JCP.PROVIDER_NAME));

            //Провайдер подписи и контейнера.
            final String provider =
                    ArgList.getProperty(ComLine.storeprovider);

            // инициализация провайдеров
            JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME));

            main_(args);
        }

    }

    public static void main_(String[] args) {
        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.SignVerHelpHD);
        else
            try {
                //Заполнение таблицы свойств значениями параметров командной строки
                //или значениями по умолчанию при отсутствии первых
                final Properties ArgList = new Properties();
                //уникальное имя ключа
                ArgList.setProperty(ComLine.ALIAS,
                        ComLine.getValue(ComLine.ALIAS, args, null));
                //тип ключевого носителя
                ArgList.setProperty(ComLine.storetype,
                        ComLine.getValue(ComLine.storetype, args,
                                ComLine.HDImageStore));
                //провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider,
                        ComLine.getValue(ComLine.storeprovider, args,
                                JCP.PROVIDER_NAME));
                //путь к хранилищу доверенных сертификатов
                ArgList.setProperty(ComLine.storepath,
                        ComLine.getValue(ComLine.storepath, args, "null"));
                //пароль на хранилище доверенных сертификатов
                ArgList.setProperty(ComLine.storepass,
                        ComLine.getValue(ComLine.storepass, args, "null"));
                //путь к файлу подписи
                ArgList.setProperty(ComLine.signpath,
                        ComLine.getValue(ComLine.signpath, args, null));
                //путь к подписываемому файлу
                ArgList.setProperty(ComLine.filepath,
                        ComLine.getValue(ComLine.filepath, args, null));

                //Провайдер подписи и контейнера.
                final String provider =
                        ArgList.getProperty(ComLine.storeprovider);

                //Проверка типа хранилища.
                final String ks;
                ks = ArgList.getProperty(ComLine.storetype);

                String resultingKeyStoreType = ComLine.verifyKeyStoreType(ks,provider);
                if (!ks.equalsIgnoreCase(resultingKeyStoreType))
                {
                    ArgList.setProperty(ComLine.storetype, resultingKeyStoreType);
                    log.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " + resultingKeyStoreType);
                }

                //Проверка ЭЦП
                SignVer(
                        ArgList.getProperty(ComLine.ALIAS),
                        ArgList.getProperty(ComLine.storetype),
                        ArgList.getProperty(ComLine.storeprovider),
                        ArgList.getProperty(ComLine.filepath),
                        ArgList.getProperty(ComLine.storepass),
                        ArgList.getProperty(ComLine.storepath),
                        ArgList.getProperty(ComLine.signpath));

            } catch (NullPointerException e) {
                //System.out.println(e.toString());
                log.info(ComLine.SignVerHelpHD);
            } catch (Exception e1) {
                final String sss = "java.lang.Exception:";
                log.info("\n" +
                        e1.toString()
                                .substring(sss.length(), e1.toString().length()) +
                        "\n" + ComLine.SignVerHelpHD);
            }
    }

    /**
     * Проверка ЭЦП.
     *
     * @param alias уникальное имя ключа
     * @param keystoreName имя ключевого носителя
     * @param filePath путь к подписанному файлу
     * @param keystorePass пароль на хранилище доверенных сертификатов
     * @param keystorePath путь к хранилищу доверенных сертификатов
     * @param signPath путь к файлу подписи
     * @throws Exception ...
     */
    private static void SignVer(String alias,
                                String keystoreName,
                                String provider,
                                String filePath,
                                String keystorePass,
                                String keystorePath,
                                String signPath)
            throws Exception {
        //чтение текста
        final byte[] text;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            text = new byte[fis.available()];
            int len;
            int tot = 0;
            do {
                len = fis.read(text, tot, text.length - tot);
                tot += len;
            } while (len > 0);
        } finally {
            if (fis != null) fis.close();
        }
        //log.info("Loading of a text is completed");

        //чтение подписи из файла
        final byte[] signature;
        FileInputStream fis1 = null;
        try {
            fis1 = new FileInputStream(signPath);
            signature = new byte[fis1.available()];
            int len;
            int tot = 0;
            do {
                len = fis1.read(signature, tot, signature.length - tot);
                tot += len;
            } while (len > 0);
        } finally {
            if (fis1 != null) fis1.close();
        }
        //log.info("Loading of a signature is completed");

        //загрузка открытого ключа из хранилища HDImageStore
        final KeyStore ks = KeyStore.getInstance(keystoreName, provider);
        char[] KeyStorePass = null;
        if (!"null".equalsIgnoreCase(keystorePass)) {
            KeyStorePass = keystorePass.toCharArray();
        }
        InputStream is = null;
        if (!"null".equalsIgnoreCase(keystorePath)) {
            is = new FileInputStream(keystorePath);
        }
        ks.load(is, KeyStorePass);
        if (is != null) is.close();
        final Certificate cert;
        cert = ks.getCertificate(alias);
        final PublicKey publicKey;
        publicKey = cert.getPublicKey();
        if (publicKey == null)
            throw new Exception("Key named \"" + alias + "\" not found");
        //log.info("Loading of a public key is completed");

        //проверка подписи
        final java.security.Signature sig;
        String signAlg = ComLine.getSignAlgorithm(publicKey.getAlgorithm());
        sig = Signature.getInstance(signAlg, provider);
        sig.initVerify(publicKey);
        sig.update(text);
        final boolean verifies;
        verifies = sig.verify(signature);
        final String s;
        if (verifies) s = "The signature is true";
        else s = "The signature is not true";
        log.info(s);

    }
}
