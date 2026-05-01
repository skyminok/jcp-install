/**
 * $RCSfileEnvContainer2001.java,v $
 * version $Revision: 36379 $
 * created 23.10.2014 12:09 by afevma
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
 * Служебный класс EnvContainer2001 предоставляет алиас и
 * пароль для доступа к контейнеру отправителя/получателя
 *  с ключом обмена на алгоритме ГОСТ Р 34.10-2001 DH.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @deprecated
 */
public class EnvContainer2001 implements ISignatureContainer {

    @Override
    public String getAlias() {
        return "signencr";
    }

    @Override
    public char[] getPassword() {
        return "Pass1234".toCharArray();
    }

    @Override
    public String getTsaAddress() {
        return null;
    }

}
