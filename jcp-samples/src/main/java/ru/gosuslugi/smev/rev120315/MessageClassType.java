
package ru.gosuslugi.smev.rev120315;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MessageClassType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MessageClassType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="REQUEST"/&gt;
 *     &lt;enumeration value="RESPONSE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "MessageClassType")
@XmlEnum
public enum MessageClassType {


    /**
     * Запрос от потребителя к поставщику 
     * 
     */
    REQUEST,

    /**
     * Ответ поставщика потребителю
     * 
     */
    RESPONSE;

    public String value() {
        return name();
    }

    public static MessageClassType fromValue(String v) {
        return valueOf(v);
    }

}
