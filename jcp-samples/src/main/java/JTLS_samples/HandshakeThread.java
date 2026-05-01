package JTLS_samples;

import ru.CryptoPro.JCP.tools.JCPLogger;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HandshakeThread implements Runnable {

    SSLSocket soс;

    public HandshakeThread(SSLSocket soс) {
        this.soс = soс;
    }

    @Override
    public void run() {
        try {
            soс.startHandshake();
        } catch (IOException exception) {
            JCPLogger.fatal(exception);
        }
    }
}
