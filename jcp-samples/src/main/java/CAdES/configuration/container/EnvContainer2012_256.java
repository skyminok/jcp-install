/**
 * $RCSfileEnvContainer2012_256.java,v $
 * version $Revision: 36379 $
 * created 23.10.2014 12:11 by afevma
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
 * Служебный класс EnvContainer2012_256 предоставляет алиас
 * и пароль для доступа к контейнеру отправителя/получателя
 * с ключом подписи на алгоритме ГОСТ Р 34.10-2012 (256 бит)
 * DH.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class EnvContainer2012_256 implements ISignatureContainer {

    @Override
    public String getAlias() {
        return "le-30bc1465-456b-4317-9876-153e265bcc8d";
    }

    @Override
    public char[] getPassword() {
        return "2".toCharArray();
    }

    @Override
    public String getTsaAddress() {
        return null;
    }

}
