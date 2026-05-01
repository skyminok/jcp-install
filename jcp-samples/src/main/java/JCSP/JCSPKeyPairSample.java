/**
 * $RCSfileJCSPKeyPairSample.java,v $
 * version $Revision: 36379 $
 * created 24.10.2019 9:47 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2019.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;
import userSamples.JCPKeyPairSample;

/**
 * Пример использования класса JCPKeyPair для проверки
 * соответствия открытого и закрытого ключей.
 *
 * @author Copyright 2004-2019 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCSPKeyPairSample {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(true);

        // генерация ключей

        JCPKeyPairSample.matchExample(JCP.GOST_EL_DEGREE_NAME,   JCSP.PROVIDER_NAME, null, null, null, null, null);
        JCPKeyPairSample.matchExample(JCP.GOST_EL_2012_256_NAME, JCSP.PROVIDER_NAME, null, null, null, null, null);
        JCPKeyPairSample.matchExample(JCP.GOST_EL_2012_512_NAME, JCSP.PROVIDER_NAME, null, null, null, null, null);

        // чтение ключей

        JCPKeyPairSample.matchExample(null, JCSP.PROVIDER_NAME, JCSP.HD_STORE_NAME, "clientTLS", "1".toCharArray(), "serverTLS", "1".toCharArray());
        JCPKeyPairSample.matchExample(null, JCSP.PROVIDER_NAME, JCSP.HD_STORE_NAME, "le-30bc1465-456b-4317-9876-153e265bcc8d", "2".toCharArray(), "le-704999da-69b2-4c7f-ada7-49d6cad6c2c2", "2".toCharArray());
        JCPKeyPairSample.matchExample(null, JCSP.PROVIDER_NAME, JCSP.HD_STORE_NAME, "le-5584fd0c-4670-46ab-9b40-a39700e5e851", "3".toCharArray(), "le-4b88f437-e0f7-4d57-b1e3-9404792dc0ed", "3".toCharArray());

    }

}
