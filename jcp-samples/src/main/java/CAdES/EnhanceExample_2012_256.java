/**
 * $RCSfileEnhanceExample_2012_256.java,v $
 * version $Revision: 36379 $
 * created 08.05.2014 16:46 by afevma
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
package CAdES;

import CAdES.configuration.container.Container2012_256;

/**
 * Пример усовершенствования подписи CAdES-BES до
 * CAdES-X Long Type 1 на алгоритме ГОСТ Р 34.10-2012 (256 бит)
 * с помощью провайдера JCP.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class EnhanceExample_2012_256 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        EnhanceExample.enhanceSignatureExample(new Container2012_256(), false);
    }

}
