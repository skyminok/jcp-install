/**
 * $RCSfileASN1BMPString.java,v $
 * version $Revision: 36379 $
 * created 14.06.2017 15:58 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования BMP строк
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ASN1BMPString extends ASNPrimitive {

    private char[] value;

    public ASN1BMPString(byte[] string) throws ASNEncodeException {

        char[]  cs = new char[string.length / 2];
        for (int i = 0; i != cs.length; i++) {
            cs[i] = (char)((string[2 * i] << 8) | (string[2 * i + 1] & 0xff));
        }

        this.value = cs;
        encode();

    }

    public ASN1BMPString(char[] string) {
        this.value = string;
    }

    public ASN1BMPString(String string) {
        this.value = string.toCharArray();
    }

    public ASN1BMPString(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    @Override
    public Object getValue() {
        return new String(value);
    }

    @Override
    protected byte[] getByteValue() throws ASNEncodeException {

        byte[] cs = new byte[value.length * 2];
        for (int i = 0, n = 0; i != value.length; i++, n += 2) {

            char c = value[i];
            cs[n]  = ((byte)(c >> 8));
            cs[n + 1] = ((byte)c);

        }

        return cs;

    }

    @Override
    protected void setValue(byte[] val) throws ASNEncodeException {

        char[]  cs = new char[val.length / 2];
        for (int i = 0; i != cs.length; i++) {
            cs[i] = (char)((val[2 * i] << 8) | (val[2 * i + 1] & 0xff));
        }

        this.value = cs;

    }

    @Override
    public int getTag() {
        return ASN1BMPString;
    }

}
