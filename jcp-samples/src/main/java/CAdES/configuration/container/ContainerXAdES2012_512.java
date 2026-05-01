/**
 * $RCSfileContainerXAdES2012_512.java,v $
 * version $Revision: 36379 $
 * created 16.09.2015 10:57 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
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
 * Служебный класс ContainerXAdES2012_512 для подписи
 * на алгоритме ГОСТ Р 34.10-2012 (512) формата XAdES.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ContainerXAdES2012_512 extends Container2012_512
    implements IXAdESContainer {

    @Override
    public String getDigestMethod() {
        return Consts.URN_GOST_DIGEST_2012_512;
    }

    @Override
    public String getSignatureMethod() {
        return Consts.URN_GOST_SIGN_2012_512;
    }

}
