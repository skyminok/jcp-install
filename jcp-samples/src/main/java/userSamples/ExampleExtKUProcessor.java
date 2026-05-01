/**
 * $RCSfile$
 * version $Revision$
 * created 19.07.2006 19:20:54 by borodin
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2006.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.params.OIDName;
import ru.CryptoPro.JCP.tools.CertReader.Extension;
import ru.CryptoPro.JCP.tools.CertReader.ExtensionProcessor;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Реализация обработчика расширения "Дополнительное использование ключа".
 * Пример использования классов Extension и ExtensionProcessor.
 * <br>
 * <br>
 * Необходимо запустить класс для регистрации, после чего запускать контрольную
 * панель, прописывая путь к данному классу. Расширение "Дополнительное
 * использование ключа" в сертификатах будет выводиться данным обработчиком.
 */
public class ExampleExtKUProcessor extends ExtensionProcessor {

/**
 * Идентификатор расширения "дополнительное ипользование ключа".
 */
private static final String OID_EXT_KEY_USAGE = "2.5.29.37";

/**
 * public - конструктор, нужен, чтобы объект класса мог быть создан
 * автоматически.
 */
public ExampleExtKUProcessor() {
}

/**
 * Определение метода
 *
 * @return строка OID
 */
public String getOID() {
    return OID_EXT_KEY_USAGE;
}

/**
 * Возвращает подрасширение для описания одного использования
 *
 * @param oid ID объекта использования
 * @return подрасширение, представляющее собой заголовок-название OID'а, с
 *         подстрокой - OID'ом, взятым в скобки, или, если такой OID неизвестен,
 *         только заголовок - OID в скобках.
 */
static Extension getOneUsage(String oid) {
    Extension ret;
    /**
     * выясняем имя
     */
    String name = OIDName.getName(oid);
    /**
     * формируем Extension - OID в скобках
     */
    Extension down = new Extension(Extension.O_BRAKE_SPACE.concat(oid)
            .concat(Extension.C_BRAKE));
    /**
     * если не смогли найти имя объекта идентификатора,
     */
    if (name.equals(oid)) {
        /**
         * возвращаем только OID,
         */
        ret = down;
    } else {
        /**
         * иначе возвращаем подрасширение, состоящее из заголовка-имени OID'а,
         * подстроки - OID'a в скобках. Оно будет отображаться с заголовком, с
         * подстрокой с табуляцией.
         */
        ret = new Extension(name, down, Extension.STANDARD_WITH_TITLE);
    }
    return ret;
}

/**
 * реализация метода возвращения текстового представления расширения
 *
 * @param cert сертификат, расширение которого рассматривается
 * @return текстовое представление расширения
 */
public Extension getExtension(X509Certificate cert) {
    /**
     * делаем вектор подрасширений
     */
    Vector strings = new Vector(0);
    try {
        /**
         * считываем список идентификаторов дополнительных использований.
         */
        List usg = cert.getExtendedKeyUsage();
        /**
         * если список не пуст, добавляем использования в вектор подрасширений
         */
        if (usg != null) {
            for (Iterator iter = usg.iterator(); iter.hasNext();) {
                strings.add(iter.next().toString());
            }
        } else {
            /**
             * иначе сообщаем об ошибке при прочтении расширения
             */
            strings.add(new Extension(getErrorParamMessage()));
        }
    } catch (CertificateParsingException e) {
        /**
         * в случае исключения сообщаем об ошибке.
         */
        strings.add(new Extension(getErrorParamMessage()));
    }
    /**
     * возвращаем расширение, с именем getName() и спском подстрок-расширений
     * strings. getName() вернет имя объекта идентификатора, если JCP данный OID
     * известен.
     */
    return new Extension(getName(), strings);
}

/**
 * регистрирует данный обработчик как основной для расширения
 *
 * @param args не используется
 */
public static void main(String[] args) {
    new ExampleExtKUProcessor().registerNewProcessor();
}
}
