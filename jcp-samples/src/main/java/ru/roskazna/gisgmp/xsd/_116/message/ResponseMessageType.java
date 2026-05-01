
package ru.roskazna.gisgmp.xsd._116.message;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3._2000._09.xmldsig_.SignatureType;
import ru.roskazna.gisgmp.xsd._116.chargecreationresponse.ChargeCreationResponseType;
import ru.roskazna.gisgmp.xsd._116.doacknowledgment.DoAcknowledgmentResponseType;
import ru.roskazna.gisgmp.xsd._116.exportpaymentsresponse.ExportPaymentsResponseType;
import ru.roskazna.gisgmp.xsd._116.exportquittanceresponse.ExportQuittanceResponseType;
import ru.roskazna.gisgmp.xsd._116.pgu_chargesresponse.ExportChargesResponseType;
import ru.roskazna.gisgmp.xsd._116.ticket.TicketType;


/**
 * <p>Java class for ResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseMessageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://roskazna.ru/gisgmp/xsd/116/MessageData}ResponseMessageData"/&gt;
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}ID"&gt;
 *             &lt;maxLength value="50"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="rqId" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="timestamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="senderIdentifier" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;minLength value="6"/&gt;
 *             &lt;maxLength value="32"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseMessageType", propOrder = {
    "responseMessageData",
    "signature"
})
public class ResponseMessageType {

    @XmlElementRef(name = "ResponseMessageData", namespace = "http://roskazna.ru/gisgmp/xsd/116/MessageData", type = JAXBElement.class)
    protected JAXBElement<?> responseMessageData;
    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected SignatureType signature;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "rqId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String rqId;
    @XmlAttribute(name = "timestamp", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlAttribute(name = "senderIdentifier", required = true)
    protected String senderIdentifier;

    /**
     * Данные ответа
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link TicketType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ChargeCreationResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportPaymentsResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportQuittanceResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DoAcknowledgmentResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportChargesResponseType }{@code >}
     *     
     */
    public JAXBElement<?> getResponseMessageData() {
        return responseMessageData;
    }

    /**
     * Sets the value of the responseMessageData property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link TicketType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ChargeCreationResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportPaymentsResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportQuittanceResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DoAcknowledgmentResponseType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExportChargesResponseType }{@code >}
     *     
     */
    public void setResponseMessageData(JAXBElement<?> value) {
        this.responseMessageData = value;
    }

    /**
     * ЭП
     * 
     * @return
     *     possible object is
     *     {@link SignatureType }
     *     
     */
    public SignatureType getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureType }
     *     
     */
    public void setSignature(SignatureType value) {
        this.signature = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the rqId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRqId() {
        return rqId;
    }

    /**
     * Sets the value of the rqId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRqId(String value) {
        this.rqId = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the senderIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderIdentifier() {
        return senderIdentifier;
    }

    /**
     * Sets the value of the senderIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderIdentifier(String value) {
        this.senderIdentifier = value;
    }

}
