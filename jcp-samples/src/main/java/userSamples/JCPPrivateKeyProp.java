/**
 * JCPPrivateKeyProp.java,v $
 * version $
 * created 14.11.2020 11:39 by afevma
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
package userSamples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Класс позволяет получить некоторые свойства
 * закрытого ключа и сертификата на ГОСТ алгоритме
 * с помощью провайдера JCP.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPPrivateKeyProp extends PrivateKeyProp {

    @Override
    public String getKeyStoreType() {
        return JCP.HD_STORE_NAME;
    }

    @Override
    public String getProviderName() {
        return JCP.PROVIDER_NAME;
    }

    /**
     * Пример.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(false); // JCP по умолчанию.

        PrivateKeyProp keyProp = new JCPPrivateKeyProp();
        keyProp.load();

        // Различные контейнеры (ГОСТ).

        keyProp.getKeyInfo("cnt256ad",     "2".toCharArray(),        SPEC_SIGNATURE);
        keyProp.getKeyInfo("gost_exch",    "Pass1234".toCharArray(), SPEC_KEY_EXCHANGE);
        keyProp.getKeyInfo("testClnt2001", "c1234".toCharArray(),    SPEC_KEY_EXCHANGE);

    }

}
