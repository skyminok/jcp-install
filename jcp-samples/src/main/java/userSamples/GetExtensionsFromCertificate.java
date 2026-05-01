/**
 * Copyright 2004-2013 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import ru.CryptoPro.JCP.ASN.CertificateExtensions.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Extension;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Extensions;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.TBSCertificate;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.File;
import java.util.Arrays;

/**
 * Пример получения расширений из сертификата.
 *
 * @author 11/11/2013
 *
 */
public class GetExtensionsFromCertificate {

    /**
     * разделитель
     */
    public static final String SEPAR = File.separator;
    /**
     * рабочая директория
     */
    public static String TEST_PATH = System.getProperty("user.dir") + SEPAR + "data";

    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        byte[] certEncoded = Array.readFile(TEST_PATH +  SEPAR + "gostUsr.cer");

        Asn1BerDecodeBuffer buffer = new Asn1BerDecodeBuffer(certEncoded);
        Certificate cert = new Certificate();
        cert.decode(buffer);

        TBSCertificate tbsCert = cert.tbsCertificate;
        Extensions extensions = tbsCert.extensions;

        Extension[] extensionList = extensions.elements;

        for (Extension extension : extensionList) {

            Asn1ObjectIdentifier extensionOid = extension.extnID;

            // CRL Distribution Points

            if (Arrays.equals(extensionOid.value,
                    ALL_CertificateExtensionsValues.id_ce_cRLDistributionPoints)) {

                Asn1OctetString extensionValue = extension.extnValue;
                buffer = new Asn1BerDecodeBuffer(extensionValue.value);

                CRLDistPointsSyntax dpsSyntax = new CRLDistPointsSyntax();
                dpsSyntax.decode(buffer);

                DistributionPoint[] dps = dpsSyntax.elements;

                if (dps != null) {

                    for (DistributionPoint dp : dps) {

                        DistributionPointName dpn = dp.distributionPoint;
                        GeneralNames dpNames = (GeneralNames) dpn.getElement();

                        for (GeneralName dpName : dpNames.elements) {
                            String dpUrl = String.valueOf(dpName.getElement());
                            System.out.println(dpUrl);
                        } // for

                    } // for

                } // if

            } // if

            // Extended Key Usage

            if (Arrays.equals(extensionOid.value,
                ALL_CertificateExtensionsValues.id_ce_extKeyUsage)) {

                Asn1OctetString extensionValue = extension.extnValue;
                buffer = new Asn1BerDecodeBuffer(extensionValue.value);

                _extKeyUsage_ExtnType extKeyUsage = new _extKeyUsage_ExtnType();
                extKeyUsage.decode(buffer);

                KeyPurposeId[] keyPurposeIds = extKeyUsage.elements;
                for (KeyPurposeId keyPurposeId : keyPurposeIds) {
                    OID purposeOid = new OID(keyPurposeId.value);
                    System.out.println(purposeOid);
                }

            } // if

        } // for

    }

}
