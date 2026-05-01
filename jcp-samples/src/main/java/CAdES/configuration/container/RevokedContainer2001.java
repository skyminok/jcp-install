/**
 * $RCSfileRevokedContainer2001.java,v $
 * version $Revision: 36379 $
 * created 04.06.2015 15:07 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
 * Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CAdES.configuration.container;

import ru.CryptoPro.JCPxml.Consts;
import xades.config.IXAdESContainer;

/**
 * Служебный класс RevokedContainer2001 предоставляет
 * алиас и пароль для доступа к контейнеру с ключом
 * подписи на алгоритме ГОСТ Р 34.10-2001 и отозванным
 * сертификатом (промежуточного УЦ). Используется также
 * для попытки создания подписи XAdES.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class RevokedContainer2001 implements ISignatureContainer,
    IXAdESContainer {

    @Override
    public String getAlias() {
        return "unixCadesTestCerts-CAR-USR";
    }

    @Override
    public char[] getPassword() {
        return "r1".toCharArray();
    }

    @Override
    public String getTsaAddress() {
        return null;
    }

    @Override
    public String getDigestMethod() {
        return Consts.URI_GOST_DIGEST;
    }

    @Override
    public String getSignatureMethod() {
        return Consts.URI_GOST_SIGN;
    }

}
