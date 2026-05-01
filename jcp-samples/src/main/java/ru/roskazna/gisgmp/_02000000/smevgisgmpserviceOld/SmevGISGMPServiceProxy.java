package ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld;

import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;

public class SmevGISGMPServiceProxy implements ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType {
  private String _endpoint = null;
  private ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType smevGISGMPService_PortType = null;
  
  public SmevGISGMPServiceProxy() {
    _initSmevGISGMPServiceProxy();
  }
  
  public SmevGISGMPServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initSmevGISGMPServiceProxy();
  }
  
  private void _initSmevGISGMPServiceProxy() {
    try {
      smevGISGMPService_PortType = (new ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_ServiceLocator()).getSmevGISGMPServiceSOAP();
      if (smevGISGMPService_PortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)smevGISGMPService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)smevGISGMPService_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (smevGISGMPService_PortType != null)
      ((javax.xml.rpc.Stub)smevGISGMPService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public ru.roskazna.gisgmp._02000000.smevgisgmpserviceOld.SmevGISGMPService_PortType getSmevGISGMPService_PortType() {
    if (smevGISGMPService_PortType == null)
      _initSmevGISGMPServiceProxy();
    return smevGISGMPService_PortType;
  }
  
  public SOAPEnvelope GISGMPTransferMsg(Message msg) throws java.rmi.RemoteException{
    if (smevGISGMPService_PortType == null)
      _initSmevGISGMPServiceProxy();
    return smevGISGMPService_PortType.GISGMPTransferMsg(msg);
  }
  
  
}