/**
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package wss4j.config;

import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;

/**
 * Класс для определения контейнера подписи XML документа.
 *
 */
public class XmlContainer {

    /**
     * Алгритмы ключей.
     */
    public enum KeyType { kt2001, kt2012_256, kt2012_512, kt2001_pswd, kt2012_256_pswd, kt2012_512_pswd };

    /**
     * Получение параметров контейнера подписи.
     *
     * @param kt Алгоритм ключа.
     * @return параметры контейнера.
     */
    public static IXAdESConfig createContainer(KeyType kt) {
        switch (kt) {
            case kt2001:          return XAdESConfig.Default.CONFIG_2001_S;
            case kt2012_256:      return XAdESConfig.Default.CONFIG_2012_S;
            case kt2012_512:      return XAdESConfig.Default.CONFIG_2012_L;
            case kt2001_pswd:     return XAdESConfig.Default.CONFIG_2001_S_WITH_PASS;
            case kt2012_256_pswd: return XAdESConfig.Default.CONFIG_2012_S_WITH_PASS;
            case kt2012_512_pswd: return XAdESConfig.Default.CONFIG_2012_L_WITH_PASS;
        }
        throw new RuntimeException();
    }

}
