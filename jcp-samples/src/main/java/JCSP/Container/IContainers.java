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
package JCSP.Container;

/**
 * Служебный интерфейс IContainers содержит
 * определения нескольких контейнеров, использующихся
 * в различных примерах.
 *
 */
public interface IContainers {

    /**
     * Алиас ключа и сертификата (ГОСТ Р 34.10-2001).
     * @deprecated
     */
    // public static final String ALIAS_01 = "Test_Container_01";
    /**
     * Пароль для доступа к ключу.
     */
    public static final char[] PASSWORD_01 = "Pass1234".toCharArray();

    /**
     * Алиас ключа и сертификата (ГОСТ Р 34.10-2012 (256)).
     */
    public static final String ALIAS_2012_256 = "Test_Container_12_256";
    /**
     * Пароль для доступа к ключу.
     */
    public static final char[] PASSWORD_2012_256 = PASSWORD_01;

    /**
     * Алиас ключа и сертификата (ГОСТ Р 34.10-2012 (512)).
     */
    public static final String ALIAS_2012_512 = "Test_Container_12_512";
    /**
     * Пароль для доступа к ключу.
     */
    public static final char[] PASSWORD_2012_512 = PASSWORD_01;

}
