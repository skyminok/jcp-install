/**
 * $RCSfileServerSigContainer2012_512.java,v $
 * version $Revision: 36379 $
 * created 17.12.2014 11:42 by afevma
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

/**
 * Служебный класс ServerContainer2012_512 предоставляет
 * алиас и пароль для доступа к контейнеру с серверным сертификатом
 * и ключом подписи на алгоритме ГОСТ Р 34.10-2012 (512 бит).
 * Бит allowDh установлен.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ServerSigContainer2012_512 implements ISignatureContainer {

    @Override
    public String getAlias() {
        return "srv512ad";
    }

    @Override
    public char[] getPassword() {
        return "3".toCharArray();
    }

    @Override
    public String getTsaAddress() {
        return null;
    }
}
