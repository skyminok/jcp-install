
package ru.roskazna.gisgmp.xsd._116.budgetindex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Дополнительные реквизиты платежа, предусмотренные приказом Минфина России от 12 ноября 2013 г. №107н
 * 
 * <p>Java class for BudgetIndexType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BudgetIndexType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Status"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="2"/&gt;
 *               &lt;enumeration value="01"/&gt;
 *               &lt;enumeration value="02"/&gt;
 *               &lt;enumeration value="03"/&gt;
 *               &lt;enumeration value="04"/&gt;
 *               &lt;enumeration value="05"/&gt;
 *               &lt;enumeration value="06"/&gt;
 *               &lt;enumeration value="07"/&gt;
 *               &lt;enumeration value="08"/&gt;
 *               &lt;enumeration value="09"/&gt;
 *               &lt;enumeration value="10"/&gt;
 *               &lt;enumeration value="11"/&gt;
 *               &lt;enumeration value="12"/&gt;
 *               &lt;enumeration value="13"/&gt;
 *               &lt;enumeration value="15"/&gt;
 *               &lt;enumeration value="16"/&gt;
 *               &lt;enumeration value="17"/&gt;
 *               &lt;enumeration value="18"/&gt;
 *               &lt;enumeration value="19"/&gt;
 *               &lt;enumeration value="20"/&gt;
 *               &lt;enumeration value="21"/&gt;
 *               &lt;enumeration value="22"/&gt;
 *               &lt;enumeration value="23"/&gt;
 *               &lt;enumeration value="24"/&gt;
 *               &lt;enumeration value="25"/&gt;
 *               &lt;enumeration value="26"/&gt;
 *               &lt;enumeration value="27"/&gt;
 *               &lt;enumeration value="28"/&gt;
 *               &lt;enumeration value="14"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Purpose"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="2"/&gt;
 *               &lt;enumeration value="ТП"/&gt;
 *               &lt;enumeration value="ЗД"/&gt;
 *               &lt;enumeration value="БФ"/&gt;
 *               &lt;enumeration value="ТР"/&gt;
 *               &lt;enumeration value="РС"/&gt;
 *               &lt;enumeration value="ОТ"/&gt;
 *               &lt;enumeration value="РТ"/&gt;
 *               &lt;enumeration value="ПБ"/&gt;
 *               &lt;enumeration value="ПР"/&gt;
 *               &lt;enumeration value="АП"/&gt;
 *               &lt;enumeration value="АР"/&gt;
 *               &lt;enumeration value="ИН"/&gt;
 *               &lt;enumeration value="ТЛ"/&gt;
 *               &lt;enumeration value="ЗТ"/&gt;
 *               &lt;enumeration value="ДЕ"/&gt;
 *               &lt;enumeration value="ПО"/&gt;
 *               &lt;enumeration value="КТ"/&gt;
 *               &lt;enumeration value="ИД"/&gt;
 *               &lt;enumeration value="ИП"/&gt;
 *               &lt;enumeration value="ТУ"/&gt;
 *               &lt;enumeration value="БД"/&gt;
 *               &lt;enumeration value="КП"/&gt;
 *               &lt;enumeration value="ВУ"/&gt;
 *               &lt;enumeration value="ДК"/&gt;
 *               &lt;enumeration value="ПК"/&gt;
 *               &lt;enumeration value="КК"/&gt;
 *               &lt;enumeration value="ТК"/&gt;
 *               &lt;enumeration value="ПД"/&gt;
 *               &lt;enumeration value="КВ"/&gt;
 *               &lt;enumeration value="00"/&gt;
 *               &lt;enumeration value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TaxPeriod"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="МС\.(0[0-9]|1[012])\.\d{4}"/&gt;
 *               &lt;pattern value="КВ\.0[1-4]\.\d{4}"/&gt;
 *               &lt;pattern value="ПЛ\.0[1-2]\.\d{4}"/&gt;
 *               &lt;pattern value="ГД\.00\.\d{4}"/&gt;
 *               &lt;pattern value="(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.\d{4}"/&gt;
 *               &lt;pattern value="\d{8}"/&gt;
 *               &lt;pattern value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TaxDocNumber"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="15"/&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;whiteSpace value="preserve"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TaxDocDate"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.\d{4}"/&gt;
 *               &lt;pattern value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PaymentType" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value=".{1,2}"/&gt;
 *               &lt;pattern value="0"/&gt;
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
@XmlType(name = "BudgetIndexType", propOrder = {
    "status",
    "purpose",
    "taxPeriod",
    "taxDocNumber",
    "taxDocDate",
    "paymentType"
})
public class BudgetIndexType {

    @XmlElement(name = "Status", required = true)
    protected String status;
    @XmlElement(name = "Purpose", required = true, defaultValue = "0")
    protected String purpose;
    @XmlElement(name = "TaxPeriod", required = true, defaultValue = "0")
    protected String taxPeriod;
    @XmlElement(name = "TaxDocNumber", required = true, defaultValue = "0")
    protected String taxDocNumber;
    @XmlElement(name = "TaxDocDate", required = true, defaultValue = "0")
    protected String taxDocDate;
    @XmlElement(name = "PaymentType", defaultValue = "0")
    protected String paymentType;

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the purpose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Sets the value of the purpose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPurpose(String value) {
        this.purpose = value;
    }

    /**
     * Gets the value of the taxPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPeriod() {
        return taxPeriod;
    }

    /**
     * Sets the value of the taxPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPeriod(String value) {
        this.taxPeriod = value;
    }

    /**
     * Gets the value of the taxDocNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxDocNumber() {
        return taxDocNumber;
    }

    /**
     * Sets the value of the taxDocNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxDocNumber(String value) {
        this.taxDocNumber = value;
    }

    /**
     * Gets the value of the taxDocDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxDocDate() {
        return taxDocDate;
    }

    /**
     * Sets the value of the taxDocDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxDocDate(String value) {
        this.taxDocDate = value;
    }

    /**
     * Gets the value of the paymentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentType() {
        return paymentType;
    }

    /**
     * Sets the value of the paymentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentType(String value) {
        this.paymentType = value;
    }

}
