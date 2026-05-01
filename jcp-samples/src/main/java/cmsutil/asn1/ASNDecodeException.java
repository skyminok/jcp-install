package cmsutil.asn1;

/**
 * Класс исключения об ошибке разбора структур ASN1.
 */
public class ASNDecodeException extends Exception {
    /** Сообщение об ошибке*/
    private String msg;

    /**
     * Конструктор
     * @param message Сообщение об ошибке
     */
    public ASNDecodeException(String message) {
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
