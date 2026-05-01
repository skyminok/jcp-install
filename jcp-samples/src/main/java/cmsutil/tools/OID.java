package cmsutil.tools;

/**
 * Класс, реализующий функционал работы с объектными идентификаторами.
 */
public class OID {
    //Константы
    public final static OID dataOID = new OID("1.2.840.113549.1.7.1");
    public final static OID envelopedDataOID = new OID("1.2.840.113549.1.7.3");

    public final static OID gost28147OID = new OID("1.2.643.2.2.21");
    public final static OID gostR3410OID = new OID("1.2.643.2.2.19");
    public final static OID gostR3410ESDHOID = new OID("1.2.643.2.2.96");
    public final static OID gostR3410ESDHOID_2012 = new OID("1.2.643.7.1.1.6.1");
    public final static OID noKeyWrapOID = new OID("1.2.643.2.2.13.0");
    public final static OID cryptoProKeyWrapOID = new OID("1.2.643.2.2.13.1");

    /**
     * Идентификатор тестовых параметров для алгоритма шифрования ГОСТ Р 28147-89.
     */
    public static final OID OID_Crypt_Test = new OID("1.2.643.2.2.31.0");
    /**
     * Идентификатор параметров шифрования по умолчанию для алгоритма шифрования
     * ГОСТ Р 28147-89.
     */
    public static final OID OID_Crypt_VerbaO = new OID("1.2.643.2.2.31.1");
    /**
     * Идентификатор параметров шифрования 1 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_Var_1 = new OID("1.2.643.2.2.31.2");
    /**
     * Идентификатор параметров шифрования 2 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_Var_2 = new OID("1.2.643.2.2.31.3");
    /**
     * Идентификатор параметров шифрования 3 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_Var_3 = new OID("1.2.643.2.2.31.4");
    /**
     * Идентификатор параметров шифрования Оскар 1.1 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_OSCAR = new OID("1.2.643.2.2.31.5");
    /**
     * Идентификатор параметров шифрования Оскар 1.0 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_TestHash = new OID("1.2.643.2.2.31.6");
    /**
     * Идентификатор параметров шифрования РИК 1 для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Crypt_RIC1 = new OID("1.2.643.2.2.31.7");
    /**
     * Идентификатор параметров шифрования Росстандарт-ТК26 Z для алгоритма шифрования ГОСТ Р
     * 28147-89.
     */
    public static final OID OID_Gost28147_89_Rosstandart_TC26_Z_ParamSet =
        new OID("1.2.643.7.1.2.5.1.1");
    /**
     * Идентификатор тестовых параметров для алгоритма хеширования ГОСТ Р 34.11-94.
     */
    public static final OID OID_HashTest = new OID("1.2.643.2.2.30.0");
    /**
     * Идентификатор параметров хеширования по умолчанию для алгоритма хеширования
     * ГОСТ Р 34.11-94.
     */
    public static final OID OID_HashVerbaO = new OID("1.2.643.2.2.30.1");
    /**
     * Идентификатор параметров хеширования 1 для алгоритма хеширования ГОСТ Р
     * 34.11-94.
     */
    public static final OID OID_HashVar_1 = new OID("1.2.643.2.2.30.2");
    /**
     * Идентификатор параметров хеширования 2 для алгоритма хеширования ГОСТ Р
     * 34.11-94.
     */
    public static final OID OID_HashVar_2 = new OID("1.2.643.2.2.30.3");
    /**
     * Идентификатор параметров хеширования 3 для алгоритма хеширования ГОСТ Р
     * 34.11-94.
     */
    public static final OID OID_HashVar_3 = new OID("1.2.643.2.2.30.4");

    /**
     * Идентификатор тестовых параметров подписи.
     */
    public static final OID OID_ECCTest3410 = new OID("1.2.643.2.2.35.0");
    /**
     * Идентификатор параметров подписи по умолчанию.
     */
    public static final OID OID_ECCSignDHPRO = new OID("1.2.643.2.2.35.1");
    /**
     * Идентификатор параметров подписи Оскар.
     */
    public static final OID OID_ECCSignDHOSCAR = new OID("1.2.643.2.2.35.2");
    /**
     * Идентификатор параметров подписи 1.
     */
    public static final OID OID_ECCSignDHVar_1 = new OID("1.2.643.2.2.35.3");
    /**
     * Идентификатор параметров обмена по умолчанию.
     */
    public static final OID OID_ECCDHPRO = new OID("1.2.643.2.2.36.0");
    /**
     * Идентификатор параметров обмена 1.
     */
    public static final OID OID_ECCDHPVar_1 = new OID("1.2.643.2.2.36.1");

    /** Массив SID*/
    private int[] value;

    /**
     * Конструктор по строковому значению
     * @param oid Строка, содержащая OID.
     * @throws NumberFormatException
     */
    public OID(String oid) throws NumberFormatException {
        if (oid == null) {
            value = null;
            return;
        }
        String[] subs = oid.split("\\.");
        if (subs.length < 3) {
            value = null;
            return;
        }
        value = new int[subs.length];
        for (int i = 0; i < subs.length; i++) {
            value[i] = Integer.parseInt(subs[i]);
        }
    }

    /**
     * Конструктор по байтовому значению.
     * @param oid Байтовое представление идентификатора.
     */
    public OID(byte[] oid) {
        int[] pre;
        boolean first = true;
        int sid, sidCount = 0;
        int i = 0;
        if (oid == null) {
            value = null;
            return;
        }
        pre = new int[oid.length + 1];
        while (i < oid.length) {
            sid = 0;
            do {
                sid = sid << 7;
                sid += (oid[i] & 0x7f);
            } while ((oid[i++] & 0x80) != 0);
            if (first) {
                first = false;
                pre[0] = sid / 40;
                pre[1] = sid % 40;
                sidCount = 2;
            }
            else
                pre[sidCount++] = sid;
        }
        value = new int[sidCount];
        System.arraycopy(pre, 0, value, 0, sidCount);
    }

    /**
     * Метод, осуществляющий получение длины закодированного SID.
     * @param sid SID
     * @return
     */
    private int getSIDLength(int sid) {
        int res = 0;
        if (sid == 0)
            return 1;
        while (sid > 0) {
            res++;
            sid = sid >>> 7;
        }
        return res;
    }

    /**
     * Метод, осуществляюший запись SID в массив.
     * @param sid SID
     * @param buffer Массив-приёмник
     * @param offset Сдвиг относительно начала, с которого необходимо начинать запись.
     */
    private void writeSID(int sid, byte[] buffer, int[] offset) {
        int start = offset[0], end;
        while (sid > 0) {
            buffer[offset[0]] = (byte)(sid % 128);
            sid = sid >>> 7;
            if (offset[0] != start)
                buffer[offset[0]] = (byte)(buffer[offset[0]] | 0x80);
            offset[0]++;
        }
        end = offset[0] - 1;
        Arrays.revertArray(buffer, start, end);
    }

    /**
     * Метод, возвращающий байтовое представление OID'а.
     * @return Массив байт, содержащий байтовое представление OID'а.
     */
    public byte[] getByteValue() {
        int totalLength;
        int firstSID;
        byte[] res = null;
        int[] offset = new int[1];
        if (value == null)
            return res;
        firstSID = value[0] * 40 + value[1];
        totalLength = getSIDLength(firstSID);
        for (int i = 2; i < value.length; i++) {
            totalLength += getSIDLength(value[i]);
        }
        res = new byte[totalLength];
        writeSID(firstSID, res, offset);
        for (int i = 2; i < value.length; i++) {
            writeSID(value[i], res, offset);
        }
        return res;
    }

    /**
     * Метод, сравнивающий два OID'а.
     * @param obj OID, с которым проивзодится сравнение.
     * @return true тогда и только тогда, когда значения OID'ов совпадают.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OID))
            return false;
        OID second = (OID)obj;
        boolean isNull = (value == null);
        boolean secIsNull = (second.value == null);
        if (isNull != secIsNull)
            return false;
        if (value.length != second.value.length)
            return false;
        for (int i = 0; i < value.length; i++) {
            if (value[i] != second.value[i])
                return false;
        }
        return true;
    }

    /**
     * Метод, получающий строковое значение OID'а.
     * @return Строковое значение OID'а в формате String.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        if (value == null)
            return stringBuilder.toString();
        for (int i = 0; i < value.length; i++) {
            stringBuilder.append(Integer.toString(value[i]));
            if (i != (value.length - 1))
                stringBuilder.append(".");
        }
        return stringBuilder.toString();
    }
}
