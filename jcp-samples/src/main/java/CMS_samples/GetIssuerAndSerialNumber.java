/**
 * $RCSfile$
 * version $Revision$
 * created 07.07.2008 17:47:14 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CMS_samples;

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.IssuerAndSerialNumber;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.reprov.x509.SerialNumber;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.security.auth.x500.X500Principal;

/**
 * get issuer and serial number
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class GetIssuerAndSerialNumber {
 /**/
private GetIssuerAndSerialNumber() {
}

/**
 * @param args /
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {

    JCPInit.initProviders(false);

    //cms for sample
    final byte[] buffer = CMS.CMSSign(Array.readFile(
        CMStools.DATA_FILE_PATH),
        CMStools.loadKey(
            CMStools.SIGN_KEY_NAME,
            CMStools.SIGN_KEY_PASSWORD
        ),
        CMStools.loadCertificate(CMStools.SIGN_KEY_NAME),
        false
    );
    //get issuer and serial number
    final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);
    final ContentInfo all = new ContentInfo();
    all.decode(asnBuf);
    final SignedData cms = (SignedData) all.content;
    Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
    cms.signerInfos.elements[0].sid.encode(encBuf);
    final Asn1BerDecodeBuffer decBuf =
            new Asn1BerDecodeBuffer(encBuf.getMsgCopy());
    IssuerAndSerialNumber isn = new IssuerAndSerialNumber();
    isn.decode(decBuf);
    //name
    encBuf.reset();
    isn.issuer.encode(encBuf);
    X500Principal name = new X500Principal(encBuf.getMsgCopy());
    if (CMStools.logger != null) {
        CMStools.logger.info("name = " + name.getName());
    }
    //serial number
    encBuf.reset();
    isn.serialNumber.encode(encBuf);
    SerialNumber sn = new SerialNumber(encBuf.getInputStream());
    if (CMStools.logger != null) {
        CMStools.logger.info("serial = " + sn.getNumber().toString());
    }
}
}
