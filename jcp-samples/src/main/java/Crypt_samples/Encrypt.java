/**
 * $RCSfile$
 * version $Revision$
 * created 16.02.2009 12:39:34 by kunina
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

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1Exception;
import ru.CryptoPro.Crypto.Cipher.GostCoreCipher;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_EncryptedKey;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_Key;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_MAC;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.tools.Array;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Random;

/**
 * Пример зашифрования сессионного ключа и текста на нем.
 * <br>
 * Совместим с Decrypt.java и примером из CSP (...\samples\CSP\DecryptFile)
 * <br>
 * Контейнер отправителя и сертификат получателя должны быть уже созданы
 * (см. GenKeys.java)
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Encrypt {
/**
 * генератор случайных чисел
 */
private static final Random rnd = new Random();
/**
 * Секретный ключ отправителя
 */
private static PrivateKey senderPrivateKey;
/**
 * Открытый ключ получателя (из сертификата)
 */
private static PublicKey responderPublicKey;
/**
 * Синхропосылка
 */
private static IvParameterSpec sv;
/**
 * Вектор инициализации
 */
private static IvParameterSpec iv;
/**
 * текст
 */
private static final byte[] TEXT = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
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

    wrapAndEncrypt(W_PATH, JCP.GOST_EL_DH_NAME,
        "GOST28147/" + GostCoreCipher.STR_PRO_EXPORT
            + "/NoPadding");

}

/**
 * создание случайной синхропосылки или вектора инициализации
 *
 * @return IvParameterSpec
 */
private static IvParameterSpec generateNewSyncro() {
    final byte[] syncro = new byte[8];
    for (int j = 0; j < 8; j++) syncro[j] = (byte) rnd.nextInt();
    return new IvParameterSpec(syncro);
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
    //сторона отправителя
    final KeyStore ks = KeyStore.getInstance(JCP.HD_STORE_NAME);
    ks.load(null, null);
    senderPrivateKey = (PrivateKey) ks.getKey(SENDER, null);
    //сертификат получателя
    final Certificate responderCert =
            generateCert(w_path + F_SEP + RESPONDER + GenKeys.CERT_EXT);
    responderPublicKey = responderCert.getPublicKey();
    //на строне отправителя вырабатываются случайные вектор инициализации
    //и синхропосылка
    sv = generateNewSyncro();
    iv = generateNewSyncro();
}

/**
 * Зашифрование секретного ключа и текста
 *
 * @throws NoSuchAlgorithmException /
 * @throws NoSuchPaddingException /
 * @throws InvalidAlgorithmParameterException /
 * @throws InvalidKeyException /
 * @throws IllegalBlockSizeException /
 * @throws IOException /
 * @throws Asn1Exception /
 * @throws BadPaddingException /
 */
public static void wrapAndEncrypt(String w_path, String
    agreeAlgName, String encAlgName) throws Exception {

    System.out.println("w_path: " + w_path +
    "\nagree algorithm: " + agreeAlgName +
    "\nkey encryption algorithm: " + encAlgName);

    //случайный ключ
    final KeyGenerator kg = KeyGenerator.getInstance("GOST28147");
    if (agreeAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME) ||
        agreeAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
        kg.init(CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
    } // if

    final SecretKey key = kg.generateKey();

    //выработка ключа согласования для зашифрования ключа key
    final KeyAgreement keyAgree = KeyAgreement.getInstance(agreeAlgName);
    keyAgree.init(senderPrivateKey, sv, null);
    keyAgree.doPhase(responderPublicKey, true);
    final SecretKey secretKey = keyAgree.generateSecret("GOST28147");

    //создание шифратора
    Cipher cipher = Cipher.getInstance(encAlgName);
    //инициализация шифратора
    cipher.init(Cipher.WRAP_MODE, secretKey, sv);
    final byte[] wrap = cipher.wrap(key);

    final Asn1BerDecodeBuffer buf = new Asn1BerDecodeBuffer(wrap);
    final Gost28147_89_EncryptedKey ek = new Gost28147_89_EncryptedKey();
    ek.decode(buf);
    //key
    final Gost28147_89_Key enk = ek.encryptedKey;
    //mac
    final Gost28147_89_MAC mac = ek.macKey;

    //зашифрование данных на ключе key
    cipher = Cipher.getInstance("GOST28147");
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    final byte[] encr = cipher.doFinal(TEXT);

    Array.writeFile(w_path + F_SEP + "encrypt.bin", encr);
    Array.writeFile(w_path + F_SEP + "session_EncryptedKey.bin", enk.value);
    Array.writeFile(w_path + F_SEP + "session_MacKey.bin", mac.value);
    Array.writeFile(w_path + F_SEP + "source.txt", TEXT);
    Array.writeFile(w_path + F_SEP + "session_SV.bin", sv.getIV());
    Array.writeFile(w_path + F_SEP + "vector.bin", iv.getIV());

    //ПРОВЕРКА
    //расшифрование ключа key=key_
    cipher = Cipher.getInstance(encAlgName); // TODO: ориентироваться по secretKey
    cipher.init(Cipher.UNWRAP_MODE, secretKey, sv);
    final SecretKey key_ =
            (SecretKey) cipher.unwrap(wrap, null, Cipher.SECRET_KEY);

    //расшифрование данных на ключе key_
    cipher = Cipher.getInstance("GOST28147"); // TODO: надо без него сделать
    cipher.init(Cipher.DECRYPT_MODE, key_, iv);
    final byte[] decr_ = cipher.doFinal(encr);

    if (Arrays.equals(TEXT, decr_))
        System.out.println("OK");
    else
        throw new Exception("Corrupted data");
}

}
