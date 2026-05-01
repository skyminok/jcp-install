/**
 * $RCSfileIEnvelopedData.java,v $
 * version $Revision: 36379 $
 * created 24.07.2014 12:35 by afevma
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
package CAdES.enveloped;

import java.io.File;

/**
 * Общие свойства примеров EnvelopedSignature.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public interface IEnvelopedData {

    /**
     * Папка с исходными данными.
     */
    public static final String DATA_DIR = System.getProperty("user.dir") +
        File.separator + "data" + File.separator;

    /**
     * Папка для сохранения промежуточных данных.
     */
    public static final String TEMP_DIR = System.getProperty("user.dir") +
        File.separator + "temp" + File.separator;

    /**
     * Файл с данными (plain text) для зашифрования или подписи.
     */
    public static final String DATA_FILE = "data.file";

    /**
     * Типы алгоритмов контейнеров.
     */
    public static enum AlgorithmType {at2001, at2012Short, at2012Long};

    /**
     * Строка для зашифрования или подписи.
     */
    public static final byte[] DATA = ("SecuritySecuritySecuritySecuritySecuritySecurity" +
            "SecuritySecuritySecuritySecuritySecuritySecuritySecuritySecuritySecurity" +
            "SecuritySecuritySecuritySecuritySecuritySecuritySecuritySecuritySecurity" +
            "SecuritySecuritySecuritySecuritySecuritySecuritySecuritySecuritySecurity" +
            "SecuritySecuritySecuritySecuritySecuritySecuritySecuritySecuritySecurity").getBytes();

    /**
     * Файл для сохранения Signed CMS.
     */
    public static final String SIGNED_CMS_FILE = "signed.cms";

}
