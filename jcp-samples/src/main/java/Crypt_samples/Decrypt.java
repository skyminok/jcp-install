/**
 * $RCSfile$
 * version $Revision$
 * created 16.02.2009 12:39:55 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2009.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package Crypt_samples;

import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1Exception;
import ru.CryptoPro.Crypto.Cipher.GostCoreCipher;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_EncryptedKey;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_Key;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_MAC;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

/**
 * Пример расшифрования сессионного ключа и текста на нем.
 * <br>
 * Совместим с Encrypt.java и примером из CSP (...\samples\CSP\EncryptFile)
 * <br>
 * Контейнер получателя и сертификат отправителя должны быть уже созданы
 * (см. GenKeys.java)
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Decrypt {
/**
 * Секретный ключ получателя
 */
private static PrivateKey responderPrivateKey;
/**
 * Открытый ключ отправителя (из сертификата)
 */
private static PublicKey senderPublicKey;
/**
 * Синхропосылка
 */
private static IvParameterSpec sv;
/**
 * Вектор инициализации
 */
private static IvParameterSpec iv;
/**
 * рабочая директория
 */
private static final String W_PATH = GenKeys.W_PATH;
/**
 * имя получателя
 */
private static final String RESPONDER = GenKeys.RESPONDER;
/**
 * имя отправителя
 */
private static final String SENDER = GenKeys.SENDER;
/**
 * разделитель
 */
private static final String F_SEP = File.separator;

/**
 * @param args *
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {

    JCPInit.initProviders(false);
    main_(args);

}

public static void main_(String[] args) throws Exception {

    prepareForSample(W_PATH);

    unWrapAndDecrypt(JCP.GOST_EL_DH_NAME,
        "GOST28147/" + GostCoreCipher.STR_PRO_EXPORT
            + "/NoPadding", W_PATH);

}

/**
 * получение сертификата из файла
 *
 * @param filePath файл сертификата
 * @return Certificate
 * @throws FileNotFoundException /
 * @throws CertificateException /
 */
private static Certificate generateCert(String filePath)
        throws FileNotFoundException, CertificateException, IOException {
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    try (FileInputStream is = new FileInputStream(filePath)) {
        return cf.generateCertificate(is);
    }
}

/**
 * Ключи и случайные данные
 *
 * @throws NoSuchAlgorithmException /
 * @throws KeyStoreException /
 * @throws IOException /
 * @throws CertificateException /
 * @throws UnrecoverableKeyException /
 */
public static void prepareForSample(String w_path)
        throws NoSuchAlgorithmException, KeyStoreException, IOException,
        CertificateException, UnrecoverableKeyException {
    //создание ключей для примера (см. GenKeys.java)
    //сторона получателя
    final KeyStore ks = KeyStore.getInstance(JCP.HD_STORE_NAME);
    ks.load(null, null);
    responderPrivateKey = (PrivateKey) ks.getKey(RESPONDER, null);
    //сертификат отправителя
    final Certificate responderCert =
            generateCert(w_path + F_SEP + SENDER + GenKeys.CERT_EXT);
    senderPublicKey = responderCert.getPublicKey();
    //на строне отправителя были выработаны случайные вектор инициализации
    //и синхропосылка
    sv = new IvParameterSpec(Array.readFile(w_path + F_SEP + "session_SV.bin"));
    iv = new IvParameterSpec(Array.readFile(w_path + F_SEP + "vector.bin"));
}

/**
 * Расшифрование ключа и текста на нем
 *
 * @throws NoSuchAlgorithmException /
 * @throws InvalidAlgorithmParameterException /
 * @throws InvalidKeyException /
 * @throws NoSuchPaddingException /
 * @throws IOException /
 * @throws Asn1Exception /
 * @throws BadPaddingException /
 * @throws IllegalBlockSizeException /
 */
public static void unWrapAndDecrypt(String agreeAlgName,
    String encAlgName, String w_path) throws Exception {

    System.out.println("w_path: " + w_path +
        "\nagree algorithm: " + agreeAlgName +
        "\nkey encryption algorithm: " + encAlgName);

    //выработка ключа согласования для расшифрования ключа key
    final KeyAgreement keyAgree = KeyAgreement.getInstance(agreeAlgName);
    keyAgree.init(responderPrivateKey, sv, null);
    keyAgree.doPhase(senderPublicKey, true);
    final SecretKey secretKey = keyAgree.generateSecret("GOST28147");

    //создание шифратора
    Cipher cipher = Cipher.getInstance(encAlgName);

    //key
    final Gost28147_89_EncryptedKey ek = new Gost28147_89_EncryptedKey();
    final byte[] enc =
            Array.readFile(w_path + F_SEP + "session_EncryptedKey.bin");
    ek.encryptedKey = new Gost28147_89_Key(enc);
    //mac
    final byte[] mc = Array.readFile(w_path + F_SEP + "session_MacKey.bin");
    ek.macKey = new Gost28147_89_MAC(mc);

    final Asn1BerEncodeBuffer ebuf = new Asn1BerEncodeBuffer();
    ek.encode(ebuf);
    final byte[] wrap = ebuf.getMsgCopy();
    //расшифрование ключа
    cipher.init(Cipher.UNWRAP_MODE, secretKey, sv);
    final SecretKey key_ =
            (SecretKey) cipher.unwrap(wrap, null, Cipher.SECRET_KEY);
    final byte[] encr = Array.readFile(w_path + F_SEP + "encrypt.bin");

    //расшифрование данных на ключе key_
    cipher = Cipher.getInstance("GOST28147");
    cipher.init(Cipher.DECRYPT_MODE, key_, iv);
    final byte[] decr_ = cipher.doFinal(encr);

    final byte[] txt = Array.readFile(w_path + F_SEP + "source.txt");

    if (Arrays.equals(txt, decr_))
        System.out.println("OK");
    else
        throw new Exception("Corrupted data");
}
}
