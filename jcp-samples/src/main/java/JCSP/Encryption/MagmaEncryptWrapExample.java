/**
 * MagmaEncryptWrapExample.java,v $
 * version $
 * created 01.10.2021 16:22 by afevma
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
import userSamples.Constants;

/**
 * Низкоуровневый(!) пример шифрования с использованием секретного
 * ключа на алгоритме Магма и его экспорта/импорта на ключе
 * согласования с открытом ключом получателя на алгоритме ГОСТ
 * 2012 (XXX) DH.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class MagmaEncryptWrapExample {

    /**
     * Алгоритм ключа шифрования.
     */
    private static final String SECRET_KEY_ALGORITHM = JCP.GOST_M_CIPHER_NAME;

    /**
     * Алгоритм шифрования данных.
     */
    private static final String CIPHER_ALGORITHM = SECRET_KEY_ALGORITHM + "/CFB/NoPadding";

    /**
     * Алгоритм экспорта/импорта секретного ключа.
     */
    private static final String WRAP_ALGORITHM = SECRET_KEY_ALGORITHM + "/KEXP_2015_M_EXPORT/NoPadding";

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true);

        KuznechikEncryptWrapExample.main_( // ГОСТ 2012 (256)
            Constants.EXCH_KEY_PAIR_ALG_2012_256,
            JCP.GOST_EPH_DH_2012_256_NAME,
            SECRET_KEY_ALGORITHM,
            JCP.G28147_BLOCKLEN,
            CIPHER_ALGORITHM,
            WRAP_ALGORITHM
        );

        KuznechikEncryptWrapExample.main_( // ГОСТ 2012 (512)
            Constants.EXCH_KEY_PAIR_ALG_2012_512,
            JCP.GOST_EPH_DH_2012_512_NAME,
            SECRET_KEY_ALGORITHM,
            JCP.G28147_BLOCKLEN,
            CIPHER_ALGORITHM,
            WRAP_ALGORITHM
        );

    }

}
