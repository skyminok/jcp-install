/**
 * SmevGISGMPServiceSOAPStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld;

import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;

public class SmevGISGMPServiceSOAPStub extends org.apache.axis.client.Stub implements ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[1];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GISGMPTransferMsg");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "Message"), org.apache.axis.description.ParameterDesc.INOUT, new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageType"), ru.gosuslugi.smev.rev120315.MessageType.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageData"), org.apache.axis.description.ParameterDesc.INOUT, new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageDataType"), ru.gosuslugi.smev.rev120315.MessageDataType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

    }

    public SmevGISGMPServiceSOAPStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public SmevGISGMPServiceSOAPStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public SmevGISGMPServiceSOAPStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "AppDataType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.AppDataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "AppDocumentType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.AppDocumentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "BaseMessageType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.BaseMessageType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "HeaderType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.HeaderType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "idType");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageClassType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.MessageClassType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageDataType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.MessageDataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.MessageType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MnemonicType");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "orgExternalType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.OrgExternalType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "PacketIdsType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.PacketIdType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "Id");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "PacketIdType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.PacketIdType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "ReferenceType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.ReferenceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "ServiceType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.ServiceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "StatusType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.StatusType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "SubMessagesType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.SubMessageType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "SubMessage");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "SubMessageType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.SubMessageType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "TypeCodeType");
            cachedSerQNames.add(qName);
            cls = ru.gosuslugi.smev.rev120315.TypeCodeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "VersionType");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethodType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.CanonicalizationMethodType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "CryptoBinary");
            cachedSerQNames.add(qName);
            cls = byte[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DigestMethodType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.DigestMethodType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DigestValueType");
            cachedSerQNames.add(qName);
            cls = byte[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "DSAKeyValueType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.DSAKeyValueType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "HMACOutputLengthType");
            cachedSerQNames.add(qName);
            cls = java.math.BigInteger.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfoType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.KeyInfoType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "KeyValueType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.KeyValueType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "ManifestType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.ReferenceType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Reference");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "ObjectType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.ObjectType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "PGPDataType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.PGPDataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "ReferenceType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.ReferenceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "RetrievalMethodType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.RetrievalMethodType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "RSAKeyValueType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.RSAKeyValueType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureMethodType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignatureMethodType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignaturePropertiesType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignaturePropertyType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureProperty");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignaturePropertyType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignaturePropertyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignatureType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignatureValueType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignatureValueType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SignedInfoType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SignedInfoType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "SPKIDataType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.SPKIDataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "TransformsType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.TransformType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "Transform");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "TransformType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.TransformType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509DataType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.X509DataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2000/09/xmldsig#", "X509IssuerSerialType");
            cachedSerQNames.add(qName);
            cls = org.w3._2000._09.xmldsig_.X509IssuerSerialType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2004/08/xop/include", "Include");
            cachedSerQNames.add(qName);
            cls = org.w3._2004._08.xop.include.Include.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public SOAPEnvelope GISGMPTransferMsg(Message msg) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/GISGMPTransferMsg");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/", "GISGMPTransferMsg"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {
     java.lang.Object _resp = _call.invoke(msg);

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {

            SOAPEnvelope envResult = (SOAPEnvelope) _resp;
            return envResult;
            /*
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();

            Set<Map.Entry> entries =_output.entrySet();
            for (Map.Entry entry : entries) {
                System.out.println(entry.getKey().toString());
                System.out.println(entry.getValue().toString());
            }
            */
            /*
            try {
                message.value = (gisgmp.ru.gosuslugi.smev.rev120315.MessageType) _output.get(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "Message"));
            } catch (java.lang.Exception _exception) {
                message.value = (gisgmp.ru.gosuslugi.smev.rev120315.MessageType) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "Message")), gisgmp.ru.gosuslugi.smev.rev120315.MessageType.class);
            }
            try {
                messageData.value = (gisgmp.ru.gosuslugi.smev.rev120315.MessageDataType) _output.get(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageData"));
            } catch (java.lang.Exception _exception) {
                messageData.value = (gisgmp.ru.gosuslugi.smev.rev120315.MessageDataType) org.apache.axis.utils.JavaUtils.convert(_output.get(new javax.xml.namespace.QName("http://smev.gosuslugi.ru/rev120315", "MessageData")), gisgmp.ru.gosuslugi.smev.rev120315.MessageDataType.class);
            }
            */
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
