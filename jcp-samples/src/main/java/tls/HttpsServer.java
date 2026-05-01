package tls;

import ru.CryptoPro.JCP.tools.ActionTools;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpsServer extends HttpsCommon {

    public static final String PARAM_work_dir = "work_dir"; // рабочая папка сервера с файлами
    public static final String PARAM_port = "port"; // порт сервера для подключения

    static {
        parameters.put(PARAM_work_dir, "");
        parameters.put(PARAM_port, "10443");
    }

    public static void main(String[] args) throws Exception {
        clearParameters();
        parseArguments(args);
        registerProviders();
        HttpsServerImpl httpsServer = new HttpsServerImpl();
        httpsServer.start();
    }

    static class HttpsServerImpl extends Thread {

        private final ExecutorService clientThreadPool = Executors.newFixedThreadPool(4);
        private final SSLServerSocket serverSocket;

        public HttpsServerImpl() throws Exception {
            SSLContext context = createContext(true);
            SSLServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
            serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(number(PARAM_port));
            setSSLParameters(serverSocket);
            if (bool(PARAM_client_auth)) {
                serverSocket.setNeedClientAuth(true);
            }
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    HttpsClientImpl httpsClient = new HttpsClientImpl(clientSocket, this);
                    clientThreadPool.submit(httpsClient);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                interrupt();
            } finally {
                super.finalize();
            }
        }

        @Override
        public void interrupt() {
            try {
                super.interrupt();
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                    }
                }
                try {
                    clientThreadPool.shutdownNow();
                    clientThreadPool.awaitTermination(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                }
            }
        }

    }

    static class HttpsClientImpl implements Runnable {

        private final SSLSocket clientSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public HttpsClientImpl(SSLSocket clientSocket, Thread serverThread) throws Exception {
            this.clientSocket = clientSocket;
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        }

        @Override
        public void run() {
            ConnectionParameters connectionParameters = new ConnectionParameters();
            ActionTools.ActionResult result = ActionTools.executeWithTime(() -> {
                try {
                    byte[] request = readHeader(inputStream, HTTP_SEPARATOR.getBytes());
                    String fileName = readFileNameFromRequest(new String(request));
                    if (fileName == null || fileName.trim().isEmpty()) {
                        connectionParameters.responseCode = 200;
                        sendOk();
                        outputStream.flush();
                    } else {
                        File localFile = strOrNull(PARAM_work_dir) != null ? new File(str(PARAM_work_dir), fileName) : new File(fileName);
                        if (localFile.exists()) {
                            FileInputStream fileInputStream = null;
                            try {
                                fileInputStream = new FileInputStream(localFile);
                                connectionParameters.responseCode = 200;
                                sendOk();
                                fromTo(fileInputStream, outputStream);
                                outputStream.flush();
                            } catch (Exception e) {
                                connectionParameters.responseCode = 404;
                                sendError(404, "File " + fileName + " not available, error: " + e.getMessage());
                            } finally {
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                            }
                        } else {
                            connectionParameters.responseCode = 404;
                            sendError(404, "File " + fileName + " not found.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeAll();
                }
                if (clientSocket.getSession() != null) {
                    connectionParameters.cipherSuite = clientSocket.getSession().getCipherSuite();
                }
                return null;
            });
            System.out.println("Response HTTP code: " + connectionParameters.responseCode +
                " (" + connectionParameters.cipherSuite + "), total time: " + result.getTime() + " ms.");
        }

        private void closeAll() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {}
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {}
            }
            try {
                clientSocket.close();
            } catch (Exception e) {}
        }

        private void sendOk() throws Exception {
            String http_answer = "HTTP/1.0 200 OK\r\n\r\n";
            outputStream.write(http_answer.getBytes());
        }

        private void sendError(int code, String errorMessage) throws Exception {
            String http_answer = "HTTP/1.0 " + code + " " + errorMessage + "\r\n\r\n";
            outputStream.write(http_answer.getBytes());
            outputStream.flush();
        }

    }

    private static byte[] readHeader(InputStream inputStream, byte[] end) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            int conformity = 0;
            int next;
            do {
                next = inputStream.read();
                if (next == -1) {
                    throw new IOException("Error occurred during reading of HTTP header.");
                }
                outputStream.write(next);
                if (next == end[conformity]) {
                    conformity++;
                } else {
                    conformity = 0;
                }
            } while (conformity != end.length);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {}
        }
        return outputStream.toByteArray();
    }

    private static String readFileNameFromRequest(String request) throws IOException {
        String filename = null;
        final String[] newStr = request.split(" ");
        if (!newStr[0].equals("GET")) {
            throw new IOException("Unknown request: " + newStr[0]);
        }
        if (!newStr[1].isEmpty() && newStr[1].charAt(0) == '/') {
            filename = newStr[1].substring(1);
        }
        return filename;
    }

}
