package xades.gisgmp;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * Возращает пароль для контейнера (используется в примере GisGmpServiceNewExample)
 * Created by elvira on 24.12.2017.
 */
public class ClientPasswordCallback implements CallbackHandler {


    @Override
    public void handle(javax.security.auth.callback.Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        pc.setPassword("1");

    }
}