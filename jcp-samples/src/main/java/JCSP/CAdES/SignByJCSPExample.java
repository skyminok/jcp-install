/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.CAdES;

import CAdES.SignExample;
import CAdES.configuration.Configuration;
import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2001;
import CAdES.configuration.container.ISignatureContainer;

import ru.CryptoPro.AdES.AdESConfig;
import ru.CryptoPro.JCP.Util.JCPInit;
import util.ResolveProvider;

/**
 * Пример создания CAdES подписи с помощью
 * провайдера Java CSP.
 *
 * 19/12/2012
 *
 */
public class SignByJCSPExample {

    /**
     * Создание CAdES подписи.
     *
     * @param container Описание используемого ключевого контейнера.
     * @param useStream True, если следует использовать поток данных и подписи.
     */
    public static void signExample(ISignatureContainer container, boolean useStream) {

        try {

            /**
             * Для того, чтобы использовать другой провайдер подписи и
             * хеширования, например, Java CSP вместо JCP, можно передать
             * его имя как в статическую функцию:
             * AdESConfig.setDefaultProvider("JCSP");
             *
             * либо как параметр:
             * System.setProperty("ru.CryptoPro.defaultProv", "JCSP");
             * По умолчанию всегда используется провайдер, заданный в
             * панели JCP.
             * Следует также учитывать, что ф. addSigner вызвана без
             * явного указания провайдера, поэтому используется провайдер
             * по умолчанию. Если в ф. addSigner явно указать имя
             * провайдера, то оно будет использоваться вместо переданных
             * ранее в AdESConfig и System.
             */

            // Задаем провайдер подписи и хеширования Java CSP.
            System.setProperty(AdESConfig.DEFAULT_PROVIDER, ResolveProvider.ALTERNATIVE_PROVIDER);

            IConfiguration configAttached = new SimpleConfiguration(
                container, false, useStream);

            SignExample.createMixedSignatureWith2Signers(
                configAttached, SimpleConfiguration.getTempFileName(null));

        } catch (Exception e) {
            Configuration.printCAdESException(e);
        }

    }

    /**
     * @param args
     * @deprecated
     */
    public static void main(String[] args) {
        JCPInit.initProviders(true);
        // signExample(new Container2001(), false);
    }
}

