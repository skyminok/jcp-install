/**
 * $RCSfile$
 * version $Revision$
 * created 08.07.2008 17:06:43 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CMS_samples;

import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.Decoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Пример проверки подписи полученной с помощью CSignData.js.
 * <br>
 * Обратная процедура аналогична (на вход при генерировании отделенной подписи в
 * java необходимо подавать закодированные в UTF-16LE данные).
 * <br>
 * Процедура кодирования в UTF-16LE является только особенностью работы скрипта
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CSignDataUse {
 /**/
public CSignDataUse() {
}

/**
 * рабочая директория для данного примера
 */
private static final String DIR_NAME = "CsignDataSample";
private static final String DIR_PATH =
        CMStools.TEST_PATH + CMStools.SEPAR + DIR_NAME;

/**
 * файл cms сообщения
 */
private static final String CMS_FILE = "cap.cms";
private static final String CMS_FILE_PATH =
        DIR_PATH + CMStools.SEPAR + CMS_FILE;

/**
 * файл данных
 */
private static final String DATA_FILE = "text.txt";
private static final String DATA_FILE_PATH =
        DIR_PATH + CMStools.SEPAR + DATA_FILE;

/**
 * @param args /
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    //создание рабочей папки
    new File(DIR_PATH).mkdir();

    //подготовка текста для подписи
    Array.writeFile(DATA_FILE_PATH, "text".getBytes());

    //генерирование подписи с помощью CSignData.js (из командной строки)
    //cscript CSignData.js sign $DIR_PATH\text.txt $DIR_PATH\cap.cms

    //проверка
    if (new File(CMS_FILE_PATH).exists()) {
        //проверка подписи
        verify();
    }
}

/**
 * Проверка подписи, полученной с помощью CSignData.js
 *
 * @throws Exception /
 */
public static void verify()
        throws Exception {
    //чтение cms файла
    final byte[] cmsBase = Array.readFile(CMS_FILE_PATH);

    //декодирование из base64 в der
    final Decoder decoder = new Decoder();
    final byte[] cmsDer =
            decoder.decodeBuffer(new ByteArrayInputStream(cmsBase));

    //кодирование данных в UTF-16LE
    final byte[] data = Array.readFile(DATA_FILE_PATH);
    final Charset charset = Charset.forName("UTF-16LE");
    final CharsetEncoder encoder = charset.newEncoder();
    final byte[] encData =
            encoder.encode(CharBuffer.wrap(new String(data))).array();

    //проверка подписи
    CMSVerify.CMSVerify(cmsDer, null, encData);
}
}
