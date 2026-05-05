package app;

import org.slf4j.bridge.SLF4JBridgeHandler;
import ru.CryptoPro.JCP.Util.JCPInit;

public final class Helper {

    public static void initJcp() {
        innerInit(false);
    }

    public static void initJcsp() {
        innerInit(true);
    }

    private static void innerInit(boolean jcsp) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        JCPInit.initProviders(jcsp);
    }

    private Helper() {
        // nothing to do
    }
}
