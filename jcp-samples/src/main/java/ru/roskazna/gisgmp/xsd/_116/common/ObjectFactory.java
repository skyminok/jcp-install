
package ru.roskazna.gisgmp.xsd._116.common;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.roskazna.gisgmp.xsd._116.common package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AdditionalData_QNAME = new QName("http://roskazna.ru/gisgmp/xsd/116/Common", "AdditionalData");
    private final static QName _PayerIdentifier_QNAME = new QName("http://roskazna.ru/gisgmp/xsd/116/Common", "PayerIdentifier");
    private final static QName _TransKind_QNAME = new QName("http://roskazna.ru/gisgmp/xsd/116/Common", "TransKind");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.roskazna.gisgmp.xsd._116.common
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EsiaUserInfoType }
     * 
     */
    public EsiaUserInfoType createEsiaUserInfoType() {
        return new EsiaUserInfoType();
    }

    /**
     * Create an instance of {@link EsiaUserInfoType.Person }
     * 
     */
    public EsiaUserInfoType.Person createEsiaUserInfoTypePerson() {
        return new EsiaUserInfoType.Person();
    }

    /**
     * Create an instance of {@link AdditionalDataType }
     * 
     */
    public AdditionalDataType createAdditionalDataType() {
        return new AdditionalDataType();
    }

    /**
     * Create an instance of {@link ChangeStatus }
     * 
     */
    public ChangeStatus createChangeStatus() {
        return new ChangeStatus();
    }

    /**
     * Create an instance of {@link EsiaUserInfoType.IndividualBussnes }
     * 
     */
    public EsiaUserInfoType.IndividualBussnes createEsiaUserInfoTypeIndividualBussnes() {
        return new EsiaUserInfoType.IndividualBussnes();
    }

    /**
     * Create an instance of {@link EsiaUserInfoType.Person.DocumentIdentity }
     * 
     */
    public EsiaUserInfoType.Person.DocumentIdentity createEsiaUserInfoTypePersonDocumentIdentity() {
        return new EsiaUserInfoType.Person.DocumentIdentity();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdditionalDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://roskazna.ru/gisgmp/xsd/116/Common", name = "AdditionalData")
    public JAXBElement<AdditionalDataType> createAdditionalData(AdditionalDataType value) {
        return new JAXBElement<AdditionalDataType>(_AdditionalData_QNAME, AdditionalDataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://roskazna.ru/gisgmp/xsd/116/Common", name = "PayerIdentifier")
    public JAXBElement<String> createPayerIdentifier(String value) {
        return new JAXBElement<String>(_PayerIdentifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://roskazna.ru/gisgmp/xsd/116/Common", name = "TransKind")
    public JAXBElement<String> createTransKind(String value) {
        return new JAXBElement<String>(_TransKind_QNAME, String.class, null, value);
    }

}
