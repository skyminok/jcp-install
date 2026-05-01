package ru.gosuslugi.smev.SignatureTool;

public class SignatureToolProxy implements ru.gosuslugi.smev.SignatureTool.SignatureTool {
  private String _endpoint = null;
  private ru.gosuslugi.smev.SignatureTool.SignatureTool signatureTool = null;
  
  public SignatureToolProxy() {
    _initSignatureToolProxy();
  }
  
  public SignatureToolProxy(String endpoint) {
    _endpoint = endpoint;
    _initSignatureToolProxy();
  }
  
  private void _initSignatureToolProxy() {
    try {
      signatureTool = (new ru.gosuslugi.smev.SignatureTool.SignatureToolServiceLocator()).getSignatureToolPort();
      if (signatureTool != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)signatureTool)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)signatureTool)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (signatureTool != null)
      ((javax.xml.rpc.Stub)signatureTool)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public ru.gosuslugi.smev.SignatureTool.SignatureTool getSignatureTool() {
    if (signatureTool == null)
      _initSignatureToolProxy();
    return signatureTool;
  }
  
  public ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureResponseType verifySignature(ru.gosuslugi.smev.SignatureTool.xsd.VerifySignatureRequestType parameters) throws java.rmi.RemoteException{
    if (signatureTool == null)
      _initSignatureToolProxy();
    return signatureTool.verifySignature(parameters);
  }
  
  public ru.gosuslugi.smev.SignatureTool.xsd.SignMessageResponseType signMessage(ru.gosuslugi.smev.SignatureTool.xsd.SignMessageRequestType parameters) throws java.rmi.RemoteException{
    if (signatureTool == null)
      _initSignatureToolProxy();
    return signatureTool.signMessage(parameters);
  }
  
  
}