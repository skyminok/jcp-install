package ru.cryptopro;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public final class JCPHelper {

    private static final List<String> JCP_PROVIDER_SET;

    static {
        JCP_PROVIDER_SET = new ArrayList<>();
        JCP_PROVIDER_SET.add("ru.CryptoPro.JCP.JCP");
        JCP_PROVIDER_SET.add("ru.CryptoPro.reprov.RevCheck");
        JCP_PROVIDER_SET.add("ru.CryptoPro.Crypto.CryptoProvider");
    }


    private JCPHelper() {
        // nothing to do
    }

    public static void initJCP() {
        setupJUL();
        JCP_PROVIDER_SET.forEach(JCPHelper::addProvider);
    }

    public static void initJCPWithTLS() {
        initJCP();
        addProvider("ru.CryptoPro.ssl.Provider");
    }


    private static void setupJUL() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static void addProvider(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Provider instance = (Provider) clazz.getDeclaredConstructor().newInstance();
            Security.removeProvider(instance.getName());
            Security.addProvider(instance);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
