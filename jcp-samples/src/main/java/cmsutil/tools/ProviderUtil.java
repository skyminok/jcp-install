/**
 * $RCSfileProviderUtil.java,v $
 * version $Revision: 36379 $
 * created 25.05.2017 13:22 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package cmsutil.tools;

import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;

/**
 * INSERT BRIEF DESCRIPTION HERE!.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ProviderUtil {

    public static String findEncryptionProvider(String provider) {
        if (provider == null || provider.equals(JCP.PROVIDER_NAME)) {
            return CryptoProvider.PROVIDER_NAME;
        }
        return "JCSP";
    }

}
