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
package JCSP.Container;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;

import java.security.KeyStore;

/**
 * Пример удаления контейнера по его алиасу с помощью
 * провайдера JCSP.
 */
public class DeleteContainerExample implements IContainers {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // 1. Читаем контейнер просто так, чтобы убедиться,
        // что он существует.

        KeyStore keyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME,
            JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        // ГОСТ Р 34.10-2001
        // System.out.println("Delete: " + ALIAS_01);
        // keyStore.deleteEntry(ALIAS_01);

        // ГОСТ Р 34.10-2012 (256)
        System.out.println("Delete: " + ALIAS_2012_256);
        keyStore.deleteEntry(ALIAS_2012_256);

        // ГОСТ Р 34.10-2012 (512)
        System.out.println("Delete: " + ALIAS_2012_512);
        keyStore.deleteEntry(ALIAS_2012_512);

    }

}
