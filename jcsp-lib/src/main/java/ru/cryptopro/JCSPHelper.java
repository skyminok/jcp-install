package ru.cryptopro;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public final class JCSPHelper {

    private static final List<String> JCSP_PROVIDER_SET;

    static {
        JCSP_PROVIDER_SET = new ArrayList<>();
        JCSP_PROVIDER_SET.add("ru.CryptoPro.JCSP.JCSP");
        JCSP_PROVIDER_SET.add("ru.CryptoPro.JCSP.JCSPRSA");
        JCSP_PROVIDER_SET.add("ru.CryptoPro.JCSP.JCSPECDSA");
        JCSP_PROVIDER_SET.add("ru.CryptoPro.JCSP.JCSPEDDSA");
    }


    private JCSPHelper() {
        // nothing to do
    }

    public static void initJCSP() {
        setupJUL();
        JCSP_PROVIDER_SET.forEach(JCSPHelper::addProvider);
    }

    public static void initJCSPWithTLS() {
        initJCSP();
        addProvider("ru.CryptoPro.sspiSSL.SSPISSL");
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
        } catch (Exception | LinkageError e) {
            throw new IllegalStateException("Error initializing class: " + className, e);
        }
    }
}
