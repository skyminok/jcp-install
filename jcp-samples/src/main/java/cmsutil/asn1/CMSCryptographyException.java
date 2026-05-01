package cmsutil.asn1;

/**
 * Класс исключения об ошибках криптографических операций.
 */
public class CMSCryptographyException extends Exception {
    /** Сообщение об ошибке*/
    private String msg;

    /**
     * Конструктор
     * @param message Сообщение об ошибке
     */
    public CMSCryptographyException(String message) {
        super(message);
        msg = message;
    }

    /**
     * Метод, возвращающий сообщение об ошибке.
     * @return Сообщение об ошибке.
     */
    public String getMessage()  {
        return msg;
    }
}
