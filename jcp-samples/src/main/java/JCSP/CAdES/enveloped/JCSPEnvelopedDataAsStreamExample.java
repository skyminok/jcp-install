/**
 * $RCSfileJCSPEnvelopedDataAsStreamExample.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 11:07 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 * Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.CAdES.enveloped;

import CAdES.enveloped.JCPEnvelopedDataAsStreamExample;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Пример создания и расшифрования подписи Enveloped CMS
 * из строки в потоке с помощью провайдера Java CSP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class JCSPEnvelopedDataAsStreamExample extends JCPEnvelopedDataAsStreamExample {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(true);
        main_group_exchange(DATA_DIR, TEMP_DIR, true);
        main_group_signature(DATA_DIR, TEMP_DIR, false);
    }

}
