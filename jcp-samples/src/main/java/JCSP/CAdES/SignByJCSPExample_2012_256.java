/**
 * Copyright 2004-2013 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.CAdES;

import CAdES.configuration.container.Container2012_256;

/**
 * Пример создания CAdES подписи на алгоритме ГОСТ
 * Р 34.10-2012 (256 бит) с помощью провайдера Java CSP.
 *
 * 16/12/2013
 *
 */
public class SignByJCSPExample_2012_256 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SignByJCSPExample.signExample(new Container2012_256(), false);
    }

}
