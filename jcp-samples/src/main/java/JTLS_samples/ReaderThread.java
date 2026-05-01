package JTLS_samples;

import ru.CryptoPro.JCP.tools.JCPLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReaderThread implements Runnable {

    InputStream stream;

    public ReaderThread(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        String input;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    stream, "windows-1251"));
            while (true) {
                if ((input = br.readLine()) == null)
                    break;
                System.out.println(input);
            }
            stream.close();
        } catch (IOException exception) {
            JCPLogger.fatal(exception);
        }
    }
}
