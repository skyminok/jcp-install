package tls;

import ru.CryptoPro.JCP.tools.ActionTools;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class HttpsClient extends HttpsCommon {

    public static final String PARAM_url = "url"; // адрес подключения
    public static final String PARAM_out = "out"; // путь к файлу для сохранения результата

    static {
        parameters.put(PARAM_url, "");
        parameters.put(PARAM_out, "");
    }

    public static void main(String[] args) throws Exception {
        clearParameters();
        parseArguments(args);
        registerProviders();
        SSLContext context = createContext(false);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(str(PARAM_url)).openConnection();
        connection.setSSLSocketFactory(new ClientSSLSocketFactory(context.getSocketFactory()));
        connection.setHostnameVerifier((hostname, session) -> true);
        ConnectionParameters connectionParameters = new ConnectionParameters();
        ActionTools.ActionResult result = ActionTools.executeWithTime(() -> {
            try {
                connection.connect();
                readData(connection);
                connectionParameters.responseCode = connection.getResponseCode();
                connectionParameters.cipherSuite = connection.getCipherSuite();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                connection.disconnect();
            }
            return null;
        });
        System.out.println("Response HTTP code: " + connectionParameters.responseCode +
            " (" + connectionParameters.cipherSuite + "), total time: " + result.getTime() + " ms.");
    }

    static class ClientSSLSocketFactory extends SSLSocketFactory {

        private final SSLSocketFactory delegate;

        ClientSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            SSLSocket sslSocket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
            setSSLParameters(sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            SSLSocket sslSocket = (SSLSocket) delegate.createSocket(host, port);
            setSSLParameters(sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            SSLSocket sslSocket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
            setSSLParameters(sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            SSLSocket sslSocket = (SSLSocket) delegate.createSocket(host, port);
            setSSLParameters(sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            SSLSocket sslSocket = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
            setSSLParameters(sslSocket);
            return sslSocket;
        }

    }

    private static void readData(HttpsURLConnection connection) throws Exception {
        InputStream inputStream = connection.getInputStream();
        OutputStream outputStream = null;
        if (strOrNull(PARAM_out) != null) {
            outputStream = new FileOutputStream(str(PARAM_out));
        }
        try {
            fromTo(inputStream, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

}
