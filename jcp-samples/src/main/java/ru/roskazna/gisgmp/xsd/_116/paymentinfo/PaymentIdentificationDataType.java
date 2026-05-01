
package ru.roskazna.gisgmp.xsd._116.paymentinfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import ru.roskazna.gisgmp.xsd._116.organization.BankType;


/**
 * Данные для идентификации платежа
 * 
 * <p>Java class for PaymentIdentificationDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PaymentIdentificationDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Bank" type="{http://roskazna.ru/gisgmp/xsd/116/Organization}BankType"/&gt;
 *           &lt;element name="Other"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                 &lt;enumeration value="CASH"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="UFK"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                 &lt;minLength value="1"/&gt;
 *                 &lt;maxLength value="36"/&gt;
 *                 &lt;whiteSpace value="preserve"/&gt;
 *                 &lt;pattern value="\d{4}"/&gt;
 *                 &lt;pattern value="[a-zA-Z0-9]{6}"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="SystemIdentifier"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://roskazna.ru/gisgmp/xsd/116/Common}SystemIdentifierType"&gt;
 *               &lt;pattern value="\w{32}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentIdentificationDataType", propOrder = {
    "bank",
    "other",
    "ufk",
    "systemIdentifier"
})
public class PaymentIdentificationDataType {

    @XmlElement(name = "Bank")
    protected BankType bank;
    @XmlElement(name = "Other")
    protected String other;
    @XmlElement(name = "UFK")
    protected String ufk;
    @XmlElement(name = "SystemIdentifier", required = true)
    protected String systemIdentifier;

    /**
     * Gets the value of the bank property.
     * 
     * @return
     *     possible object is
     *     {@link BankType }
     *     
     */
    public BankType getBank() {
        return bank;
    }

    /**
     * Sets the value of the bank property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankType }
     *     
     */
    public void setBank(BankType value) {
        this.bank = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOther(String value) {
        this.other = value;
    }

    /**
     * Gets the value of the ufk property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUFK() {
        return ufk;
    }

    /**
     * Sets the value of the ufk property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUFK(String value) {
        this.ufk = value;
    }

    /**
     * Gets the value of the systemIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    /**
     * Sets the value of the systemIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemIdentifier(String value) {
        this.systemIdentifier = value;
    }

}
