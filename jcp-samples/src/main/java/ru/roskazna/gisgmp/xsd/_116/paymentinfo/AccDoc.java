
package ru.roskazna.gisgmp.xsd._116.paymentinfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AccDocNo" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="\d{1,6}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AccDocDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accDocNo",
    "accDocDate"
})
@XmlRootElement(name = "AccDoc")
public class AccDoc {

    @XmlElement(name = "AccDocNo")
    protected String accDocNo;
    @XmlElement(name = "AccDocDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar accDocDate;

    /**
     * Gets the value of the accDocNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccDocNo() {
        return accDocNo;
    }

    /**
     * Sets the value of the accDocNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccDocNo(String value) {
        this.accDocNo = value;
    }

    /**
     * Gets the value of the accDocDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAccDocDate() {
        return accDocDate;
    }

    /**
     * Sets the value of the accDocDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAccDocDate(XMLGregorianCalendar value) {
        this.accDocDate = value;
    }

}
