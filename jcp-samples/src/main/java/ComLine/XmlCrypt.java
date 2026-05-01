package ComLine;

import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.w3c.dom.Document;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.DefaultProviders;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.JCPxml.Utils;
import ru.CryptoPro.JCPxml.XmlInit;
import util.ResolveProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


/**
 *  Зашифровывание/расшифрование .xml документа из командной строки. Обращается к {@code ru.CryptoPro.JCPxml.Utils}.
 */
public class XmlCrypt {
    /**
     * Parameters.
     */
    private static final String encrypt = "-encrypt";
    private static final String decrypt = "-decrypt";
    private static final String provider = "-provider";
    /**
     * Help.
     */
    private static final String validArgumentsHelp =
                    "HELP\n" +
                    "XmlCrypt\n\n" +
                    "Modes (mutually exclusive):\n" +
                    encrypt + "\n" +
                    decrypt + "\n\n" +
                    "Options:\n" +
                    ComLine.filepath + "         (def: no def)\n" +
                    ComLine.fileout +  "          (def: no def)\n" +
                    provider + "         (def: " + JCP.PROVIDER_NAME + ")\n\n" +
                    ComLine.storetype + "        (def: " + JCP.HD_STORE_NAME + ")\n" +
                    ComLine.keyStoreAlias + "    (def: no def)\n" +
                    ComLine.storepass + "        (def: null)\n\n" +
                    ComLine.certpath + "         (def: null)   used only in encrypt mode\n\n" +
                    "parameters with (def: no def) must be defined necessarily\n";

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger("LOGGER");

    /**
     * XmlCrypt {-encrypt | -decrypt} -filepath C:/*.* -fileout C:/*.* [-storeprovider JCP] [-storetype HDImageStore]
     * -keyStoreAlias name_of_key -storepass null
     * <br>
     * <DL> <DT><b> -encrypt/-decrypt </b>  <DD>зашифровать/расшифровать</DD></DT>
     * <DT><b> -filepath</b>  <DD>файл для чтения</DD></DT>
     * <DT><b> -fileout</b>  <DD>файл для записи</DD></DT>
     * <DT><b> -storeprovider</b>  <DD>провайдер ключевого носителя<DD>(по умолчанию JCP)</DD></DT>
     * <DT><b> -storetype</b>  <DD>тип ключевого носителя<DD>(по умолчанию HDImageStore)</DD></DT>
     * <DT><b> -alias </b>  <DD>уникальное имя записываемого ключа</DD></DT>
     * <DT><b> -storepass </b>  <DD>пароль на ключ<DD>(по умолчанию null)</DD></DT> </DL>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args){

        List<String> argList = Arrays.asList(args);
        if(argList.contains(ComLine.help)) {
            log.info(validArgumentsHelp);
            return;
        }

        boolean isEncrypt = argList.contains(encrypt);

        if(isEncrypt)
        {
            if(argList.contains(decrypt)) {
                log.info("-encrypt and -decrypt are mutually exclusive.\n\n" + validArgumentsHelp);
                return;
            }

            if(argList.contains(ComLine.certpath) && (argList.contains(ComLine.storetype) ||
                argList.contains(ComLine.keyStoreAlias) || argList.contains(ComLine.storepass)))
            {
                log.info("Mutually exclusive arguments are present.\n\n" + validArgumentsHelp);
                return;
            }
        }

        if(!isEncrypt && argList.contains(ComLine.certpath))
        {
            log.info(ComLine.certpath + " is an illegal argument for decrypt mode.\n\n" + validArgumentsHelp);
            return;
        }

        try{
            String providerName = ComLine.getValue(provider, args, DefaultProviders.DEFAULT_PROVIDER_NAME);
            boolean JCSPEnabled = providerName.equals(ResolveProvider.ALTERNATIVE_PROVIDER);
            JCPInit.initProviders(JCSPEnabled);
            XmlInit.init();

            Document document = Utils.readDoc(ComLine.getValue(ComLine.filepath, args, null));

            if(isEncrypt && argList.contains(ComLine.certpath)) {
                encrypt(document, ComLine.getValue(ComLine.certpath, args, ""),
                JCSPEnabled ? ResolveProvider.ALTERNATIVE_PROVIDER : CryptoProvider.PROVIDER_NAME);
            } else {
                crypt(isEncrypt, document, providerName,
                    JCSPEnabled ? ResolveProvider.ALTERNATIVE_PROVIDER : CryptoProvider.PROVIDER_NAME,
                    JCSPEnabled? ResolveProvider.ALTERNATIVE_HD_IMAGE : JCP.HD_STORE_NAME,
                    ComLine.getValue(ComLine.keyStoreAlias, args, null),
                    ComLine.getValue(ComLine.storepass, args, "").toCharArray());
            }
            try (FileOutputStream os = new FileOutputStream(ComLine.getValue(ComLine.fileout, args, null))) {
                Utils.writeDoc(document, os);
            }
        } catch(Exception e) {
            log.info(e +"\n\n" + validArgumentsHelp);
        }
    }

    /**
     * @param isEncrypt true - зашифровывание, false - расшифрование
     * @param document документ для зашифровывания
     * @param storeProvider провайдер ключевого носителя
     * @param cryptoProvider криптопровайдер
     * @param storeType тип ключевого носителя
     * @param alias уникальное имя ключа
     * @param storepass пароль на ключ
     * @throws Exception
     */
    private static void crypt(boolean isEncrypt, Document document, String storeProvider, String cryptoProvider, String storeType,
        String alias, char[] storepass) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(storeType, storeProvider);
        keyStore.load(null, null);
        JCPProtectionParameter parameter = new JCPProtectionParameter(storepass, true, false);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)keyStore.getEntry(alias, parameter);

        if(isEncrypt)
            encrypt(document, (X509Certificate)entry.getCertificate(), cryptoProvider);
        else
            Utils.decrypt(document, entry.getPrivateKey(), cryptoProvider);

    }

    /**
     * @param document документ для зашифровывания
     * @param certpath путь к файлу сертификата сертификат
     * @param cryptoProvider криптопровайдер
     * @throws Exception
     */
    private static void encrypt(Document document, String certpath, String cryptoProvider) throws Exception
    {
        FileInputStream fileInputStream = new FileInputStream(certpath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
        encrypt(document, certificate, cryptoProvider);
        fileInputStream.close();
    }

    /**
     * @param document документ для зашифровывания
     * @param cert сертификат
     * @param cryptoProvider криптопровайдер
     * @throws Exception
     */
    private static void encrypt(Document document, X509Certificate cert, String cryptoProvider) throws Exception
    {
        KeyGenerator kg = KeyGenerator.getInstance(JCP.GOST_CIPHER_NAME, cryptoProvider);
        kg.init(CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
        SecretKey sessionKey = kg.generateKey();

        String transformAlg = Consts.URI_GOST_TRANSPORT;
        XMLCipher keyCipher = XMLCipher.getProviderInstance(transformAlg, cryptoProvider);
        keyCipher.init(XMLCipher.WRAP_MODE, cert.getPublicKey());


        //создание KeyInfo с сертификатом
        KeyInfo certKeyInfo = new KeyInfo(document);
        X509Data x509data = new X509Data(document);
        x509data.addCertificate(cert);
        certKeyInfo.add(x509data);

        //зашифрование ключа
        EncryptedKey encryptedKey = keyCipher.encryptKey(document, sessionKey);
        encryptedKey.setKeyInfo(certKeyInfo);

        Utils.encrypt(document, sessionKey, encryptedKey, cryptoProvider);
    }

}
