/**
 * $RCSfileTLSProxyConstants.java,v $
 * version $Revision: 36379 $
 * created 03.10.2017 10:26 by afevma
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
package tls_proxy;

/**
 * Интерфейс TLSProxyConstants содержит служебные константы.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public interface TLSProxyConstants {

    /**
     * Статус запускаемого потока.
     */
    int CONNECTION_STARTING = 0;

    /**
     * Статус запущенного потока.
     */
    int CONNECTION_STARTED = 1;

    /**
     * Статус остановленного потока.
     */
    int CONNECTION_STOPPED = 2;

    /**
     * Статус ошибки потока.
     */
    int CONNECTION_ERROR = 3;

}
