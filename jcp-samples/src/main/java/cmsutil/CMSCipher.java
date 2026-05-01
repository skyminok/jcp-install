package cmsutil;


import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.JCP;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.EnvelopedCMS;
import cmsutil.asn1.encrypted.CipherProcessor;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Класс, осуществляющий шифрование данных и формирование CMSEnveloped
 */
public class CMSCipher {
    /**
     * Создание или разбор CMS Enveloped сообщения.
     * @param parameters Параметры командной строки.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public static void process(CMSMain.Parameters parameters,
        boolean notAddProviders) throws CMSCryptographyException,
        ASNDecodeException {
        File file = new File(parameters.inFile);
        FileInputStream certStream = null;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        int read;
        long processed = 0;
        byte[] buf;
        byte[] structure;
        KeyStore keyStore;
        try {

            String provider = parameters.provider;

            if (!notAddProviders) {

                JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME));

            }

            if (parameters.mode == CMSMain.ENCRYPT) { //Зашифрование
                if (parameters.certStoreType == null) {
                    parameters.certStoreType = JCP.CERT_STORE_NAME;
                }
                //Получение хранилища сертификатов
                if (parameters.certStoreProvider == null) {
                    keyStore = KeyStore.getInstance(parameters.certStoreType);
                } else {
                    keyStore = KeyStore.getInstance(parameters.certStoreType,
                        parameters.certStoreProvider);
                }
                if (keyStore == null)
                    throw new CMSCryptographyException("Cannot open certificate store!");
                certStream = new FileInputStream(parameters.certStore);
                keyStore.load(certStream, (parameters.pass == null) ? null : parameters.pass.toCharArray());
                // Получаем сертификат получателя.
                X509Certificate recipientCertificateTmp = (X509Certificate)keyStore.getCertificate(parameters.alias);
                X509Certificate recipientCertificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(recipientCertificateTmp.getEncoded()));
                if (recipientCertificate == null)
                    throw new CMSCryptographyException("User certificate not found!");
                //Получаем Enveloped CMS структуру
                EnvelopedCMS msg = new EnvelopedCMS(file.length(), recipientCertificate, parameters.provider);
                outputStream = new FileOutputStream(parameters.outFile);
                outputStream.write(msg.getEncoded());
                // Шифруем данные
                CipherProcessor cipherProcessor = msg.getCipherProcessor();
                inputStream = new FileInputStream(file);
                buf = new byte[4096];
                while ((read = inputStream.read(buf)) == buf.length) {
                    outputStream.write(cipherProcessor.crypt(buf));
                }
                if (read >= 0)
                    outputStream.write(cipherProcessor.cryptFinal(buf, read));
            } else {
                inputStream = new FileInputStream(file);
                //Получаем размер непосредственно структуры, без шифртекста!
                read = (int)EnvelopedCMS.getCipherTextOffset(inputStream);
                if (read < 0)
                    throw new ASNDecodeException("CMS file is corrupted");
                structure = new byte[read];
                inputStream.close();
                inputStream = null;
                inputStream = new FileInputStream(file);
                read = inputStream.read(structure);
                if (read != structure.length)
                    throw new ASNDecodeException("CMS file is corrupted");
                keyStore = KeyStore.getInstance(parameters.keyStore, parameters.provider);
                if (keyStore == null)
                    throw new CMSCryptographyException("Cannot open key store!");
                keyStore.load(null, null);
                // Получаем секретный ключ получателя
                char[] password = (parameters.pass == null) ? null : parameters.pass.toCharArray();
                PrivateKey privateKey;
                X509Certificate certificate;
                JCPProtectionParameter pp = new JCPProtectionParameter(password, true, true);
                JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(parameters.alias, pp);
                privateKey = entry.getPrivateKey();
                certificate = (X509Certificate) entry.getCertificate();
                if (privateKey == null)
                    throw new CMSCryptographyException("User private key not found!");
                //Разбираем Enveloped CMS структуру
                EnvelopedCMS msg = new EnvelopedCMS(structure, privateKey, parameters.provider);
                // Если сертификат получателя есть, достаём и смотрим, что он совпадает.
                if (certificate != null) {
                    X500Principal issuerPrincipalName = certificate.getIssuerX500Principal();
                    BigInteger serial = certificate.getSerialNumber();
                    if (!issuerPrincipalName.getName().equalsIgnoreCase(msg.getRecipientCertInfo().getRecipientIssuerPrincipal().getName()))
                        throw new CMSCryptographyException("Certificate issuer mismatch!");
                    if (!serial.equals(msg.getRecipientCertInfo().getSerial()))
                        throw new CMSCryptographyException("Certificate serial number mismatch!");
                }
                // Расшифровываем данные
                CipherProcessor cipherProcessor = msg.getCipherProcessor();
                outputStream = new FileOutputStream(parameters.outFile);
                buf = new byte[4096];
                while ((read = inputStream.read(buf)) == buf.length) {
                    processed += read;
                    outputStream.write(cipherProcessor.crypt(buf));
                }
                if (read >=0) {
                    processed += read;
                    outputStream.write(cipherProcessor.cryptFinal(buf, read));
                }
                if (processed != cipherProcessor.getTextLength()) {
                    throw new ASNDecodeException("Ciphertext has incorrect value!");
                }
            }
        }
        catch (KeyStoreException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (FileNotFoundException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (IOException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (CertificateException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (ASNDecodeException e) {
            throw e;
        }
        catch (CMSCryptographyException e) {
            throw e;
        }
        catch (UnrecoverableKeyException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (UnrecoverableEntryException e) {
            throw new CMSCryptographyException(e.getMessage());
        } finally {
            try {
                if (certStream != null)
                    certStream.close();
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }
            catch (IOException e) {

            }
        }
    }
}

