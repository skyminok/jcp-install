package cmsutil.asn1;

/**
 * Created by afevma on 29.08.2016.
 */
public class ASNEncodeException extends ASNDecodeException {
    /**
     * Конструктор
     *
     * @param message Сообщение об ошибке
     */
    public ASNEncodeException(String message) {
        super(message);
    }
}
