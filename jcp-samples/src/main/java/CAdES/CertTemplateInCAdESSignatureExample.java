package CAdES;

import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.CollectionStore;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;

import ru.CryptoPro.JCP.ASN.CA_Definitions.EnrollmentNameValuePair;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.Encoder;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Пример добавления подписанного атрибута 1.3.6.1.4.1.311.13.2.1
 * в CAdES-подпись для УЦ 2.0.
 *
 */
public class CertTemplateInCAdESSignatureExample {

    public static void main(String[] args) throws Exception {

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // Загрузка ключа и сертификата из ключевого контейнера.

        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME);
        keyStore.load(null, null);

        JCPProtectionParameter parameter = new JCPProtectionParameter("password".toCharArray());
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry("alias", parameter);

        PrivateKey privateKey = entry.getPrivateKey();
        X509Certificate cert  = (X509Certificate) entry.getCertificate();

        // Подготовка подписи.

        CAdESSignature cAdESSignature = new CAdESSignature(false); // совмещенная подпись
        List<X509CertificateHolder> certs = new ArrayList<>();

        certs.add(new X509CertificateHolder(cert.getEncoded()));
        CollectionStore certificateStore = new CollectionStore(certs);

        cAdESSignature.setCertificateStore(certificateStore); // добавляем сертификат подписанта в подпись

        // Пример создания подписанного атрибута 1.3.6.1.4.1.311.13.2.1.

        EnrollmentNameValuePair enrolmentNameValuePair1 = new EnrollmentNameValuePair("CertificateTemplate", "User");
        EnrollmentNameValuePair enrolmentNameValuePair2 = new EnrollmentNameValuePair("CpRaRequesterProfile", "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8UHJvZmlsZUF0dHJpYnV0ZXM+DQogIDxBdHRyaWJ1dGUgT2lkPSIyLjUuNC4zIiBWYWx1ZT0idGVzdCAyOC0wNC0yMDIyIiAvPg0KPC9Qcm9maWxlQXR0cmlidXRlcz4=");

        Asn1BerEncodeBuffer buffer1 = new Asn1BerEncodeBuffer();
        enrolmentNameValuePair1.encode(buffer1);

        Asn1BerEncodeBuffer buffer2 = new Asn1BerEncodeBuffer();
        enrolmentNameValuePair2.encode(buffer2);

        ASN1Primitive seq1 = ASN1Sequence.fromByteArray(buffer1.getMsgCopy());
        ASN1Primitive seq2 = ASN1Sequence.fromByteArray(buffer2.getMsgCopy());

        Attribute attribute = new Attribute(new ASN1ObjectIdentifier("1.3.6.1.4.1.311.13.2.1"), new DERSet(new ASN1Encodable[] {seq1, seq2}));

        // Создание совмещенной подписи CAdES-BES с дополнительными подписанными атрибутами.

        cAdESSignature.addSigner(
            JCP.PROVIDER_NAME,
            null,
            null,
            privateKey,
            Collections.singletonList(cert),
            CAdESType.CAdES_BES,
            null,
            false,
            new AttributeTable(attribute), // дополнительные атрибуты
            null
        );

        ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
        cAdESSignature.open(signatureStream);

        // Передаем на подпись данные (запрос в формате PKCS10).

        cAdESSignature.update("request".getBytes()); // тело запроса

        cAdESSignature.close();
        signatureStream.close();

        // Выводим полученную подпись.

        byte[] signature = signatureStream.toByteArray();
        Encoder encoder = new Encoder();

        String encodedSignature = encoder.encode(signature);
        System.out.println("Attached CAdES-BES [BASE64]:\n" + encodedSignature);

        // Проверка подписи.

        cAdESSignature = new CAdESSignature(signature, null, null);
        cAdESSignature.verify(null);

    }

}
