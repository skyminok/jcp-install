/**
 * SmevGISGMPService_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld;

public class SmevGISGMPService_ServiceLocator extends org.apache.axis.client.Service implements ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_Service {

    public SmevGISGMPService_ServiceLocator() {
    }


    public SmevGISGMPService_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SmevGISGMPService_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SmevGISGMPServiceSOAP
    private java.lang.String SmevGISGMPServiceSOAP_address = "http://smev-mvf.test.gosuslugi.ru:7777/gateway/services/SID0003663/1.00";

    public java.lang.String getSmevGISGMPServiceSOAPAddress() {
        return SmevGISGMPServiceSOAP_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SmevGISGMPServiceSOAPWSDDServiceName = "SmevGISGMPServiceSOAP";

    public java.lang.String getSmevGISGMPServiceSOAPWSDDServiceName() {
        return SmevGISGMPServiceSOAPWSDDServiceName;
    }

    public void setSmevGISGMPServiceSOAPWSDDServiceName(java.lang.String name) {
        SmevGISGMPServiceSOAPWSDDServiceName = name;
    }

    public ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType getSmevGISGMPServiceSOAP() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SmevGISGMPServiceSOAP_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSmevGISGMPServiceSOAP(endpoint);
    }

    public ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType getSmevGISGMPServiceSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPServiceSOAPStub _stub = new ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPServiceSOAPStub(portAddress, this);
            _stub.setPortName(getSmevGISGMPServiceSOAPWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSmevGISGMPServiceSOAPEndpointAddress(java.lang.String address) {
        SmevGISGMPServiceSOAP_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPServiceSOAPStub _stub = new ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPServiceSOAPStub(new java.net.URL(SmevGISGMPServiceSOAP_address), this);
                _stub.setPortName(getSmevGISGMPServiceSOAPWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SmevGISGMPServiceSOAP".equals(inputPortName)) {
            return getSmevGISGMPServiceSOAP();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/", "SmevGISGMPService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://roskazna.ru/gisgmp/02000000/SmevGISGMPService/", "SmevGISGMPServiceSOAP"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SmevGISGMPServiceSOAP".equals(portName)) {
            setSmevGISGMPServiceSOAPEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
