
package ru.gosuslugi.smev.rev120315;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TypeCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TypeCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="GSRV"/&gt;
 *     &lt;enumeration value="GFNC"/&gt;
 *     &lt;enumeration value="OTHR"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "TypeCodeType")
@XmlEnum
public enum TypeCodeType {


    /**
     * Взаимодействие в рамках оказания государственных услуг
     * 					
     * 
     */
    GSRV,

    /**
     * Взаимодействие в рамках исполнения государственных функций
     * 					
     * 
     */
    GFNC,

    /**
     *  Взаимодействие в иных целях, предусмотренных
     * 						законодательством Российской Федерации 
     * 
     */
    OTHR;

    public String value() {
        return name();
    }

    public static TypeCodeType fromValue(String v) {
        return valueOf(v);
    }

}
