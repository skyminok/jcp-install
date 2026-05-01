/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import javax.net.ssl.*;

public class TestHttpsConnection {

	// Just add these two functions in your program 
    public static class miTM implements javax.net.ssl.TrustManager,
    	javax.net.ssl.X509TrustManager {

        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
 
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs)
        {
            return true;
        }
 
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs)
        {
            return true;
        }
 
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException
        {
            return;
        }
 
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException
        {
            return;
        }
    }
    
    private static void trustAllHttpsCertificates() throws Exception {
 
        // Create a trust manager that does not validate certificate chains:
 
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
 
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
 
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
    }

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		System.setProperty("com.sun.security.enableCRLDP", "true");
		System.setProperty("com.ibm.security.enableCRLDP", "true");
		// System.setProperty("javax.net.debug", "ssl,handshake,data,trustmanager");

		System.setProperty("javax.net.ssl.keyStoreType", "HDImageStore");
        // System.setProperty("javax.net.ssl.keyStore", "client_exch");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

		System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
		System.setProperty("javax.net.ssl.trustStore", "uc_cryptopro.store");
	    System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		/*
		System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
		System.setProperty("javax.net.ssl.trustStore", "C:\\server.store");
	    System.setProperty("javax.net.ssl.trustStorePassword", "Pass1234");
		*/
		/*
		System.setProperty("javax.net.ssl.supportGVO","true");
		System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
		System.setProperty("javax.net.ssl.trustStore", "icrsStore");
	    System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		*/
		// System.setProperty("javax.net.ssl.supportGVO","true");
		/*
		System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
		System.setProperty("javax.net.ssl.trustStore", "CATrustStore");
	    System.setProperty("javax.net.ssl.trustStorePassword", "Pass1234");
		*/

        /*"https://gost1.stonesoft.com/wa/Hello.html"*/
        /*"https://cpca.cryptopro.ru/tls/tls-cli.asp"*/
        /*"https://cpca.cryptopro.ru/"*/
        /*"https://icrs.nbki.ru"*/
        /*"https://localhost:8443/index.jsp"*/
        /*"HTTPS", "192.168.214.5", 8443, "index.jsp"*/
        // tls/tls-cli.asp
        URL url = new URL("https://www.rb-ei.com/welcome_ru.htm");

		/*
		// Now you are telling the JRE to ignore the hostname
        HostnameVerifier hv = new HostnameVerifier()
        {

        	public boolean verify(String arg0, SSLSession arg1) {
        		
        		 try {
					
        			 Certificate[] peer = arg1.getPeerCertificates();
				
        			 HostnameChecker checker = HostnameChecker.getInstance(
        					 HostnameChecker.TYPE_TLS);
        			 
        			 checker.match(arg0, (X509Certificate) peer[0]);
        		 
        		 } catch (SSLPeerUnverifiedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		System.out.println("Warning: URL Host: " + arg0 + " vs. "
        	             + arg1.getPeerHost());
        	    return true;
        	}
        };
		*/

        // System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		// trustAllHttpsCertificates();
		// HttpsURLConnection.setDefaultHostnameVerifier(hv);

        System.out.println(url);
		SSLContext s;

		class MySSLSocketFactory extends SSLSocketFactory {

			String[] cipherSuites = {
					"TLS_CIPHER_2012",
					"TLS_CIPHER_2001",
					"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"
			};

			SSLSocketFactory factory = null;

			public MySSLSocketFactory() {
				try {
					factory = SSLContext.getInstance("Default", "JTLS").getSocketFactory();//new SSLSocketFactoryImpl();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String[] getDefaultCipherSuites() {
				return factory.getDefaultCipherSuites();
			}

			@Override
			public String[] getSupportedCipherSuites() {
				return factory.getSupportedCipherSuites();
			}

			@Override
			public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {

				SSLSocket factorySocket = (SSLSocket) factory.createSocket(socket, s, i, false);
				factorySocket.setEnabledCipherSuites(cipherSuites);

				return factorySocket;
			}

			@Override
			public Socket createSocket(String s, int i) throws IOException {

				SSLSocket factorySocket = (SSLSocket) factory.createSocket(s, i);
				factorySocket.setEnabledCipherSuites(cipherSuites);

				return factorySocket;

			}

			@Override
			public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {

				SSLSocket factorySocket = (SSLSocket) factory.createSocket(s, i, inetAddress, i1);
				factorySocket.setEnabledCipherSuites(cipherSuites);

				return factorySocket;

			}

			@Override
			public Socket createSocket(InetAddress inetAddress, int i) throws IOException {

				SSLSocket factorySocket = (SSLSocket) factory.createSocket(inetAddress, i);
				factorySocket.setEnabledCipherSuites(cipherSuites);

				return factorySocket;

			}

			@Override
			public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {

				SSLSocket factorySocket = (SSLSocket) factory.createSocket(inetAddress, i, inetAddress1, i1);
				factorySocket.setEnabledCipherSuites(cipherSuites);

				return factorySocket;

			}
		}

		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    	// connection.setSSLSocketFactory(new MySSLSocketFactory());
		connection.connect();
		
		/*
    	System.out.println("cipher suit: " + connection.getCipherSuite());
    	
    	Certificate[] serverCerts = connection.getServerCertificates();
    	Certificate[] sentCerts = connection.getLocalCertificates();
    	
    	for ( Certificate s : serverCerts ) {
    		System.out.println("#################################################");
    		System.out.println("server cert: " + s);
    		System.out.println("#################################################");
        }
    	
    	if (sentCerts != null) {
    		for ( Certificate s : sentCerts ) {
    			System.out.println("#################################################");
    			System.out.println("sent cert: " + s);
    			System.out.println("#################################################");
    		}
    	}
    	*/
		
    	TLSUtility.print_content(connection, null);
    	connection.disconnect();

	}

}
