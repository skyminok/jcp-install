/**
 * $RCSfile$
 * version $Revision$
 * created 27.05.2009 18:45:02 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2021.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.reprov.array.DerValue;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.*;

/**
 * Примеры работы с расширениями сертификатов.
 * В частности, определяется расширение IdentificationKind.
 * Примеры сертификатов можно найти в папке data\CERTS\extension.
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CertificateExtension {

    /*
    * Общий путь к сертификатам сертификатам.
    */
    private static final String CERT_PATH =
        System.getProperty("user.home") + File.separator + "CERTS";

    /**
     * Путь к серфтификату с расширением identificationKind personal(0).
     */
    private static final String CERT_0 =
        CERT_PATH + File.separator + "cert_0.cer";

    /**
     * Путь к серфтификату с расширением identificationKind remote_cert(1).
     */
    private static final String CERT_1 =
            CERT_PATH + File.separator + "cert_1.cer";

    /**
     * Путь к серфтификату с расширением identificationKind remote_passport(2).
     */
    private static final String CERT_2 =
            CERT_PATH + File.separator + "cert_2.cer";

    /**
     * Путь к серфтификату с расширением identificationKind remote_system(3).
     */
    private static final String CERT_3 =
            CERT_PATH + File.separator + "cert_3.cer";

    /**
     * OID расширения identificationKind (Тип идентификации при выдаче сертификата).
     */
    private static final String IdentificationKindOid = "1.2.643.100.114";

    /**
     * Чтение сертификатов и печать расширения identificationKind.
     * @param args null
     * @throws Exception /
    */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        readIdentificationKindExt(CERT_0);
        readIdentificationKindExt(CERT_1);
        readIdentificationKindExt(CERT_2);
        readIdentificationKindExt(CERT_3);
    }

    /**
     * Пример чтения расширения "Тип идентификации при выдаче сертификата".
     *
     * @param certPath Путь к сертификату.
     * @throws Exception
    */
    public static void readIdentificationKindExt(String certPath) throws Exception {

        // Читаем сертификат из файла.
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert;
        try (FileInputStream is = new FileInputStream(certPath)) {
            cert = (X509Certificate) cf.generateCertificate(is);
        }
        // Получаем расширение по OIDу.
        byte[] encodedId = cert.getExtensionValue(IdentificationKindOid);
        if (encodedId == null) {
            System.out.println("No IdentificationKind found");
            return;
        }

        // Декодируем расширение, получаем значение.
        DerValue val = new DerValue(encodedId);
        if (val.tag != DerValue.tag_OctetString)
            throw new Exception("Invalid extension format");
        byte[] octetString = val.getOctetString();
        val = new DerValue(octetString);
        if (val.tag != DerValue.tag_Integer)
            throw new Exception("Invalid extension format");
        int extValue = val.getInteger();

        // Определяем тип по значению.
        String identType;
        switch (extValue){
            case 0:
                identType = "При личном присутствии";  //personal(0)
                break;
            case 1:
                identType = "Без личного присутствия с использованием квалифицированной ЭП"; //remote_cert(1)
                break;
            case 2:
                identType = "Без личного присутствия с использованием персональных данных, записанных на электронный носитель из заграничного паспорта"; //remote_passport(2)
                break;
            case 3:
                identType = "Без личного присутствия с использованием сведений из ЕСИА и ЕБС"; //remote_system(3)
                break;
            default:
                identType = "Неизвестный тип";
                break;
        }
        System.out.println("Тип идентификации при выдаче сертификата: " + identType);
    }

}
