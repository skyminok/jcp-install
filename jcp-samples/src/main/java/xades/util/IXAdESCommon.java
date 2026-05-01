/**
 * $RCSfileIXAdESCommon.java,v $
 * version $Revision: 36379 $
 * created 11.06.2015 15:50 by afevma
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
package xades.util;

import java.io.File;
import java.security.Provider;

/**
 * Служебный интерфейс с константами.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public interface IXAdESCommon {

    /**
     * Рабочая папка.
     */
    public static final String TRUST_DIR = System.getProperty("user.dir") +
            File.separator + "data" + File.separator;

    /**
     * Рабочая папка.
     */
    public static final String WORK_DIR = System.getProperty("user.dir") +
            File.separator + "temp" + File.separator;

    /**
     * Хранилище корневых сертификатов для построения
     * и проверки цепочек.
     */
    public static final String TRUST_STORE = TRUST_DIR + "xadesTrustStore";//"xadesTrustStoreExtended";

    /**
     * Пароль к хранилищу.
     */
    public static final char[] TRUST_PASSWORD = "1".toCharArray();

    /**
     * Класс провайдера XMLDSigRI.
     */
    public static String providerName = "ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI";

    /**
     * Провайдер XMLDSigRI.
     */
    public static final Provider xmlDSigRi = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

    /**
     * Идентификатор Security.
     */
    public static final String ACTOR = "http://smev.gosuslugi.ru/actors/smev";
    /**
     * Адрес тестового сервиса СМЭВ.
     */
    public final static String SMEV_SERVICE = "http://smev-mvf.test.gosuslugi.ru:7777/gateway/services/SID0003038";

    /**
     * Идентификатор отправителя.
     */
    public static final String SENDER_EXAMPLE_1 = "000147";

    /**
     * Идентификатор отправителя (другой).
     */
    public static final String SENDER_EXAMPLE_2 = "0000a1";

    /**
     * Роль отправителя (7 - оператор по переводу денежных средств).
     */
    public static final String SENDER_ROLE = "7";

    /**
     * Идентификатор подписываемого узла.
     */
    public static final String SIGNING_ID = "P_a1234567-bcf8-90de-f123-4567890abcde";

    /**
     * Идентификатор запроса.
     */
    public static final String REQUEST_MESSAGE_ID = "P_a7654321-8bcf-de90-123f-abcde0987654";

}
