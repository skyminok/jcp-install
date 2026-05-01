/**
 * TransportMagExample.java,v $
 * version $
 * created 01.10.2020 16:22 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2021.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Encryption;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;

/**
 * Пример шифрования и экспорта/импорта сессионного ключа с
 * алгоритмом Магма на ключе экспорта/импорта.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class TransportMagExample {

    /**
     * Запуск примера.
     *
     * @param args Параметры.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true);

        TransportKuzExample.encryptDecrypt(JCSP.GOST_TRANSPORT_M, JCP.GOST_M_CIPHER_NAME, JCP.GOST_EPH_DH_2012_256_NAME);

    }

}
