
package ru.roskazna.gisgmp.xsd._116.charge;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3._2000._09.xmldsig_.SignatureType;
import ru.roskazna.gisgmp.xsd._116.budgetindex.BudgetIndexType;
import ru.roskazna.gisgmp.xsd._116.common.AdditionalDataType;
import ru.roskazna.gisgmp.xsd._116.common.ChangeStatus;
import ru.roskazna.gisgmp.xsd._116.organization.OrganizationType;


/**
 * Данные начислений 
 * 
 * <p>Java class for ChargeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChargeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ValidUntil" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="DocDispatchDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="MainSupplierBillIDList" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="MainSupplierBillID" type="{http://roskazna.ru/gisgmp/xsd/116/Common}SupplierBillIDType" maxOccurs="9"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SupplierOrgInfo" type="{http://roskazna.ru/gisgmp/xsd/116/Organization}OrganizationType"/&gt;
 *         &lt;element name="BillFor"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="210"/&gt;
 *               &lt;minLength value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TotalAmount"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedLong"&gt;
 *               &lt;minInclusive value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://roskazna.ru/gisgmp/xsd/116/Common}ChangeStatus"/&gt;
 *         &lt;element name="KBK" type="{http://roskazna.ru/gisgmp/xsd/116/Common}KBKType"/&gt;
 *         &lt;element name="OKTMO" type="{http://roskazna.ru/gisgmp/xsd/116/Common}OKTMOType"/&gt;
 *         &lt;element name="BudgetIndex" type="{http://roskazna.ru/gisgmp/xsd/116/BudgetIndex}BudgetIndexType"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="UnifiedPayerIdentifier"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                 &lt;pattern value="2\d{14}[A-Z0-9]{2}\d{3}"/&gt;
 *                 &lt;pattern value="3\d{14}[A-Z0-9]{2}\d{3}|3\d{9}[A-Z0-9]{2}\d{3}"/&gt;
 *                 &lt;pattern value="4\d{12}"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="AltPayerIdentifier"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                 &lt;pattern value="((0[1-9])|(1[0-4])|(2[12456789])|(3[0]))[0-9a-zA-Zа-яА-Я]{20}\d{3}"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="TreasureBranch" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;maxLength value="255"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TOFK" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;length value="4"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="FOName" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;maxLength value="512"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LSvUFK" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="\w{11}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LSvFO" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;maxLength value="20"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AcptTerm" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer"&gt;
 *               &lt;totalDigits value="1"/&gt;
 *               &lt;minInclusive value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PaytCondition" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer"&gt;
 *               &lt;totalDigits value="1"/&gt;
 *               &lt;minInclusive value="1"/&gt;
 *               &lt;enumeration value="1"/&gt;
 *               &lt;enumeration value="2"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Origin" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value="PRIOR"/&gt;
 *               &lt;enumeration value="TEMP"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://roskazna.ru/gisgmp/xsd/116/Common}AdditionalData" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}ID"&gt;
 *             &lt;maxLength value="50"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="supplierBillID" use="required" type="{http://roskazna.ru/gisgmp/xsd/116/Common}SupplierBillIDType" /&gt;
 *       &lt;attribute name="billDate" use="required" type="{http://www.w3.org/2001/XMLSchema}date" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeType", propOrder = {
    "validUntil",
    "docDispatchDate",
    "mainSupplierBillIDList",
    "supplierOrgInfo",
    "billFor",
    "totalAmount",
    "changeStatus",
    "kbk",
    "oktmo",
    "budgetIndex",
    "unifiedPayerIdentifier",
    "altPayerIdentifier",
    "treasureBranch",
    "tofk",
    "foName",
    "lSvUFK",
    "lSvFO",
    "acptTerm",
    "paytCondition",
    "origin",
    "additionalData",
    "signature"
})
public class ChargeType {

    @XmlElement(name = "ValidUntil")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar validUntil;
    @XmlElement(name = "DocDispatchDate")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar docDispatchDate;
    @XmlElement(name = "MainSupplierBillIDList")
    protected ChargeType.MainSupplierBillIDList mainSupplierBillIDList;
    @XmlElement(name = "SupplierOrgInfo", required = true)
    protected OrganizationType supplierOrgInfo;
    @XmlElement(name = "BillFor", required = true)
    protected String billFor;
    @XmlElement(name = "TotalAmount", required = true)
    protected BigInteger totalAmount;
    @XmlElement(name = "ChangeStatus", namespace = "http://roskazna.ru/gisgmp/xsd/116/Common", required = true)
    protected ChangeStatus changeStatus;
    @XmlElement(name = "KBK", required = true)
    protected String kbk;
    @XmlElement(name = "OKTMO", required = true)
    protected String oktmo;
    @XmlElement(name = "BudgetIndex", required = true)
    protected BudgetIndexType budgetIndex;
    @XmlElement(name = "UnifiedPayerIdentifier")
    protected String unifiedPayerIdentifier;
    @XmlElement(name = "AltPayerIdentifier")
    protected String altPayerIdentifier;
    @XmlElement(name = "TreasureBranch")
    protected String treasureBranch;
    @XmlElement(name = "TOFK")
    protected String tofk;
    @XmlElement(name = "FOName")
    protected String foName;
    @XmlElement(name = "LSvUFK")
    protected String lSvUFK;
    @XmlElement(name = "LSvFO")
    protected String lSvFO;
    @XmlElement(name = "AcptTerm")
    protected BigInteger acptTerm;
    @XmlElement(name = "PaytCondition")
    protected BigInteger paytCondition;
    @XmlElement(name = "Origin")
    protected String origin;
    @XmlElement(name = "AdditionalData", namespace = "http://roskazna.ru/gisgmp/xsd/116/Common")
    protected List<AdditionalDataType> additionalData;
    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
    protected SignatureType signature;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "supplierBillID", required = true)
    protected String supplierBillID;
    @XmlAttribute(name = "billDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar billDate;

    /**
     * Gets the value of the validUntil property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValidUntil() {
        return validUntil;
    }

    /**
     * Sets the value of the validUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValidUntil(XMLGregorianCalendar value) {
        this.validUntil = value;
    }

    /**
     * Gets the value of the docDispatchDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDocDispatchDate() {
        return docDispatchDate;
    }

    /**
     * Sets the value of the docDispatchDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDocDispatchDate(XMLGregorianCalendar value) {
        this.docDispatchDate = value;
    }

    /**
     * Gets the value of the mainSupplierBillIDList property.
     * 
     * @return
     *     possible object is
     *     {@link ChargeType.MainSupplierBillIDList }
     *     
     */
    public ChargeType.MainSupplierBillIDList getMainSupplierBillIDList() {
        return mainSupplierBillIDList;
    }

    /**
     * Sets the value of the mainSupplierBillIDList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChargeType.MainSupplierBillIDList }
     *     
     */
    public void setMainSupplierBillIDList(ChargeType.MainSupplierBillIDList value) {
        this.mainSupplierBillIDList = value;
    }

    /**
     * Gets the value of the supplierOrgInfo property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationType }
     *     
     */
    public OrganizationType getSupplierOrgInfo() {
        return supplierOrgInfo;
    }

    /**
     * Sets the value of the supplierOrgInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationType }
     *     
     */
    public void setSupplierOrgInfo(OrganizationType value) {
        this.supplierOrgInfo = value;
    }

    /**
     * Gets the value of the billFor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBillFor() {
        return billFor;
    }

    /**
     * Sets the value of the billFor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBillFor(String value) {
        this.billFor = value;
    }

    /**
     * Gets the value of the totalAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the value of the totalAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalAmount(BigInteger value) {
        this.totalAmount = value;
    }

    /**
     * Gets the value of the changeStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeStatus }
     *     
     */
    public ChangeStatus getChangeStatus() {
        return changeStatus;
    }

    /**
     * Sets the value of the changeStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeStatus }
     *     
     */
    public void setChangeStatus(ChangeStatus value) {
        this.changeStatus = value;
    }

    /**
     * Gets the value of the kbk property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKBK() {
        return kbk;
    }

    /**
     * Sets the value of the kbk property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKBK(String value) {
        this.kbk = value;
    }

    /**
     * Gets the value of the oktmo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOKTMO() {
        return oktmo;
    }

    /**
     * Sets the value of the oktmo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOKTMO(String value) {
        this.oktmo = value;
    }

    /**
     * Gets the value of the budgetIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BudgetIndexType }
     *     
     */
    public BudgetIndexType getBudgetIndex() {
        return budgetIndex;
    }

    /**
     * Sets the value of the budgetIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BudgetIndexType }
     *     
     */
    public void setBudgetIndex(BudgetIndexType value) {
        this.budgetIndex = value;
    }

    /**
     * Gets the value of the unifiedPayerIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnifiedPayerIdentifier() {
        return unifiedPayerIdentifier;
    }

    /**
     * Sets the value of the unifiedPayerIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnifiedPayerIdentifier(String value) {
        this.unifiedPayerIdentifier = value;
    }

    /**
     * Gets the value of the altPayerIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAltPayerIdentifier() {
        return altPayerIdentifier;
    }

    /**
     * Sets the value of the altPayerIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAltPayerIdentifier(String value) {
        this.altPayerIdentifier = value;
    }

    /**
     * Gets the value of the treasureBranch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTreasureBranch() {
        return treasureBranch;
    }

    /**
     * Sets the value of the treasureBranch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTreasureBranch(String value) {
        this.treasureBranch = value;
    }

    /**
     * Gets the value of the tofk property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTOFK() {
        return tofk;
    }

    /**
     * Sets the value of the tofk property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTOFK(String value) {
        this.tofk = value;
    }

    /**
     * Gets the value of the foName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFOName() {
        return foName;
    }

    /**
     * Sets the value of the foName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFOName(String value) {
        this.foName = value;
    }

    /**
     * Gets the value of the lSvUFK property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLSvUFK() {
        return lSvUFK;
    }

    /**
     * Sets the value of the lSvUFK property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLSvUFK(String value) {
        this.lSvUFK = value;
    }

    /**
     * Gets the value of the lSvFO property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLSvFO() {
        return lSvFO;
    }

    /**
     * Sets the value of the lSvFO property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLSvFO(String value) {
        this.lSvFO = value;
    }

    /**
     * Gets the value of the acptTerm property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAcptTerm() {
        return acptTerm;
    }

    /**
     * Sets the value of the acptTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAcptTerm(BigInteger value) {
        this.acptTerm = value;
    }

    /**
     * Gets the value of the paytCondition property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPaytCondition() {
        return paytCondition;
    }

    /**
     * Sets the value of the paytCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPaytCondition(BigInteger value) {
        this.paytCondition = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Дополнительные поля начисления Gets the value of the additionalData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionalData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionalDataType }
     * 
     * 
     */
    public List<AdditionalDataType> getAdditionalData() {
        if (additionalData == null) {
            additionalData = new ArrayList<AdditionalDataType>();
        }
        return this.additionalData;
    }

    /**
     * ЭП xml-документа
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
     * Gets the value of the supplierBillID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierBillID() {
        return supplierBillID;
    }

    /**
     * Sets the value of the supplierBillID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierBillID(String value) {
        this.supplierBillID = value;
    }

    /**
     * Gets the value of the billDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBillDate() {
        return billDate;
    }

    /**
     * Sets the value of the billDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBillDate(XMLGregorianCalendar value) {
        this.billDate = value;
    }


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
     *         &lt;element name="MainSupplierBillID" type="{http://roskazna.ru/gisgmp/xsd/116/Common}SupplierBillIDType" maxOccurs="9"/&gt;
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
        "mainSupplierBillID"
    })
    public static class MainSupplierBillIDList {

        @XmlElement(name = "MainSupplierBillID", required = true)
        protected List<String> mainSupplierBillID;

        /**
         * Gets the value of the mainSupplierBillID property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the mainSupplierBillID property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMainSupplierBillID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getMainSupplierBillID() {
            if (mainSupplierBillID == null) {
                mainSupplierBillID = new ArrayList<String>();
            }
            return this.mainSupplierBillID;
        }

    }

}
