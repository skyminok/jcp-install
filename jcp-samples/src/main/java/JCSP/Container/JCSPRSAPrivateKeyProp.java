/**
 * JCSPRSAPrivateKeyProp.java,v $
 * version $
 * created 25.11.2020 20:18 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Container;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSPRSA;
import userSamples.PrivateKeyProp;

/**
 * Класс позволяет получить некоторые свойства
 * закрытого ключа и сертификата на алгоритме
 * RSA с помощью провайдера Java CSP RSA.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCSPRSAPrivateKeyProp extends PrivateKeyProp {

    @Override
    public String getKeyStoreType() {
        return JCSPRSA.HD_STORE_NAME;
    }

    @Override
    public String getProviderName() {
        return JCSPRSA.PROVIDER_NAME;
    }

    /**
     * Пример.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true); // JCSP по умолчанию.

        PrivateKeyProp keyProp = new JCSPRSAPrivateKeyProp();
        keyProp.load();

        // Различные контейнеры (RSA).

        keyProp.getKeyInfo("rsa",    "123".toCharArray(), SPEC_KEY_EXCHANGE);
        keyProp.getKeyInfo("rsasig", "123".toCharArray(), SPEC_SIGNATURE);

    }

}
