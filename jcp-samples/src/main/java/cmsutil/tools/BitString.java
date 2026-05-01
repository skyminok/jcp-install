package cmsutil.tools;

/**
 * Класс, реализующий работу с битовыми строками.
 */
public class BitString {
    /** Число неиспользуемых битов */
    private int unusedBits;
    /** Значение */
    private byte[] value;

    /**
     * Конструктор по значению
     * @param newVal Байтовые массив, содержащий значение.
     * @param unused Число неспользуемых бит.
     */
    public BitString(byte[] newVal, int unused) {
        this.unusedBits = unused;
        this.value = new byte[newVal.length];
        System.arraycopy(newVal, 0, value, 0, value.length);
    }

    /**
     * Конструктор по байтовому представлению.
     * @param val Байтовое представление.
     */
    public BitString(byte[] val) {
        if (val == null) {
            unusedBits = 0;
            value = null;
            return;
        }
        unusedBits = val[1];
        if (val.length == 1) {
            value = null;
            return;
        }
        value = new byte[val.length - 1];
        System.arraycopy(val, 1, value, 0, value.length);
    }

    /**
     * Метод, осуществляющий получение байтового представления битовой строки.
     * @return Байтовое представление в виде массива байт.
     */
    public byte[] getBytes() {
        if (value == null)
            return null;
        byte[] res = new byte[value.length + 1];
        res[0] = (byte)unusedBits;
        System.arraycopy(value, 0, res, 1, value.length);
        return res;
    }

    /**
     * Метод, осуществляющий получение числа неиспользуемых бит.
     * @return Число неиспользуемых бит.
     */
    public int getUnusedBits() {
        return unusedBits;
    }

    /**
     * Метод, осуществляющий получение значения.
     * @return Значение в виде массива байт.
     */
    public byte[] getValue() {
        if (value == null)
            return null;
        byte[] res = new byte[value.length];
        System.arraycopy(value, 0, res, 0, value.length);
        return res;
    }
}
