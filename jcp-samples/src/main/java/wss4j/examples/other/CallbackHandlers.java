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
package wss4j.examples.other;

import org.apache.ws.security.WSPasswordCallback;
import ru.CryptoPro.JCP.Key.SecretKeySpec;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Список используемых callback классов при работе
 * java-клиента (wss4j).
 */
public class CallbackHandlers {

    public static class KeyStoreCallbackHandler implements CallbackHandler {

        private Map<String, String> users = new HashMap<String, String>();

        public void addUser(String alias, String password) {
            users.put(alias, password);
        }

        public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {

            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                    pc.setPassword(users.get(pc.getIdentifier()));
                } else {
                    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                }
            }
        }
    }

    public static class SecretKeyAndKeyStoreCallbackHandler implements CallbackHandler {

        private Map<String, SecretKeySpec> secrets = new HashMap<String, SecretKeySpec>();

        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            // Не используется
        }

        public void addSecretKey(String identifier, SecretKeySpec keySpec) {
            secrets.put(identifier, keySpec);
        }

        public SecretKeySpec getKey(String identifier) {
            return this.secrets.get(identifier);
        }

    }

}
