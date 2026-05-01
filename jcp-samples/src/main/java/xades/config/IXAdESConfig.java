/**
 * $RCSfileIXAdESConfig.java,v $
 * version $Revision: 36379 $
 * created 04.06.2015 9:19 by afevma
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
package xades.config;

import CAdES.configuration.container.ISignatureContainer;

/**
 * Служебный интерфейс с описанием контейнера для
 * создания/проверки подписи формата XAdES.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public interface IXAdESConfig extends IXAdESContainer {

    /**
     * Провайдер по умолчанию.
     *
     * @return провайдер.
     */
    public String getDefaultProvider();

    /**
     * Тип контейнера.
     *
     * @return тип.
     */
    public String getKeyStoreType();

    /**
     * Контейнер для подписи.
     *
     * @return контейнер.
     */
    public ISignatureContainer getSignatureContainer();

}
