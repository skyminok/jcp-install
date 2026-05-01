/**
 * $RCSfileServerContainer2001.java,v $
 * version $Revision: 36379 $
 * created 23.07.2014 17:08 by afevma
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
package CAdES.configuration.container;

import CAdES.configuration.Configuration;

/**
 * Служебный класс ServerContainer2001 предоставляет
 * алиас и пароль для доступа к контейнеру с серверным
 * сертификатом и ключом обмена на алгоритме ГОСТ Р
 * 34.10-2001 DH.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @deprecated
 */
public class ServerContainer2001 implements ISignatureContainer {

    @Override
    public String getAlias() {
        return "serverTLS";
    }

    @Override
    public char[] getPassword() {
        return "1".toCharArray();
    }

    @Override
    public String getTsaAddress() {
        return Configuration.TSA_DEFAULT_ADDRESS;
    }

}
