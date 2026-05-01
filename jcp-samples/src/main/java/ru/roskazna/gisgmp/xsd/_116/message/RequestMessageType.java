
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
import ru.roskazna.gisgmp.xsd._116.chargecreationrequest.ChargeCreationRequestType;
import ru.roskazna.gisgmp.xsd._116.doacknowledgment.DoAcknowledgmentRequestType;
import ru.roskazna.gisgmp.xsd._116.packagestatusrequest.PackageStatusRequestType;
import ru.roskazna.gisgmp.xsd._116.pgu_datarequest.DataRequest;
import ru.roskazna.gisgmp.xsd._116.pgu_importrequest.ImportRequestType;
import ru.roskazna.gisgmp.xsd._116.selfadministration.ImportCertificateRequestType;


/**
 * <p>Java class for RequestMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestMessageType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://roskazna.ru/gisgmp/xsd/116/MessageData}RequestMessageData"/&gt;
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}ID"&gt;
 *             &lt;maxLength value="50"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="timestamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="senderIdentifier" use="required" type="{http://roskazna.ru/gisgmp/xsd/116/Common}URNType" /&gt;
 *       &lt;attribute name="senderRole" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;maxLength value="10"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="callBackURL" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestMessageType", propOrder = {
    "requestMessageData",
    "signature"
})
public class RequestMessageType {

    @XmlElementRef(name = "RequestMessageData", namespace = "http://roskazna.ru/gisgmp/xsd/116/MessageData", type = JAXBElement.class)
    protected JAXBElement<?> requestMessageData;
    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected SignatureType signature;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "timestamp", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlAttribute(name = "senderIdentifier", required = true)
    protected String senderIdentifier;
    @XmlAttribute(name = "senderRole", required = true)
    protected String senderRole;
    @XmlAttribute(name = "callBackURL")
    @XmlSchemaType(name = "anyURI")
    protected String callBackURL;

    /**
     * Gets the value of the requestMessageData property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ImportRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DataRequest }{@code >}
     *     {@link JAXBElement }{@code <}{@link ImportCertificateRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DoAcknowledgmentRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link ChargeCreationRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PackageStatusRequestType }{@code >}
     *     
     */
    public JAXBElement<?> getRequestMessageData() {
        return requestMessageData;
    }

    /**
     * Sets the value of the requestMessageData property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ImportRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DataRequest }{@code >}
     *     {@link JAXBElement }{@code <}{@link ImportCertificateRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DoAcknowledgmentRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link ChargeCreationRequestType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PackageStatusRequestType }{@code >}
     *     
     */
    public void setRequestMessageData(JAXBElement<?> value) {
        this.requestMessageData = value;
    }

    /**
     * ÝÏ
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

    /**
     * Gets the value of the senderRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderRole() {
        return senderRole;
    }

    /**
     * Sets the value of the senderRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderRole(String value) {
        this.senderRole = value;
    }

    /**
     * Gets the value of the callBackURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallBackURL() {
        return callBackURL;
    }

    /**
     * Sets the value of the callBackURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallBackURL(String value) {
        this.callBackURL = value;
    }

}
