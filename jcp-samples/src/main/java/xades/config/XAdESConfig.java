/**
 * $RCSfileXAdESConfig.java,v $
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

import CAdES.configuration.container.*;

import ru.CryptoPro.JCP.JCP;

import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESSigner;
import ru.CryptoPro.XAdES.XAdESType;

import util.ResolveProvider;

import java.security.cert.X509Certificate;

/**
 * Служебный класс конфигурации для создания/проверки подписи формата XAdES.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XAdESConfig implements IXAdESConfig {

    /**
     * Провайдер по умолчанию.
     */
    private final String defaultProvider;

    /**
     * Тип контейнера.
     */
    private final String keyStoreType;

    /**
     * Контейнер подписи.
     */
    private final ISignatureContainer signatureContainer;

    /**
     * Конструктор.
     *
     * @param provider Провайдер по умолчанию.
     * @param type Тип контейнера.
     * @param container Контейнер подписи.
     */
    public XAdESConfig(String provider, String type, ISignatureContainer container) {
        defaultProvider = provider;
        keyStoreType = type;
        signatureContainer = container;
    }

    @Override
    public String getDefaultProvider() {
        return defaultProvider;
    }

    @Override
    public String getKeyStoreType() {
        return keyStoreType;
    }

    @Override
    public ISignatureContainer getSignatureContainer() {
        return signatureContainer;
    }

    @Override
    public String getDigestMethod() {
        if (signatureContainer instanceof IXAdESContainer) {
            return ((IXAdESContainer)signatureContainer).getDigestMethod();
        }
        return null;
    }

    @Override
    public String getSignatureMethod() {
        if (signatureContainer instanceof IXAdESContainer) {
            return ((IXAdESContainer)signatureContainer).getSignatureMethod();
        }
        return null;
    }

    public static class Default extends XAdESConfig {
        private static final String PROVIDER_NAME = ResolveProvider.JCSPEnabled ? ResolveProvider.ALTERNATIVE_PROVIDER : JCP.PROVIDER_NAME;
        private static final String KEYSTORE_TYPE = ResolveProvider.JCSPEnabled ? ResolveProvider.ALTERNATIVE_HD_IMAGE : JCP.HD_STORE_NAME;
        public Default(ISignatureContainer container) {
            super(PROVIDER_NAME, KEYSTORE_TYPE, container);
        }
        /**
         * Контейнер с паролем подписи ГОСТ Р 34.10-2001.
         * @deprecated
         */
        public static final IXAdESConfig CONFIG_2001_S_WITH_PASS = new Default(new Container2001());

        /**
         * Контейнер подписи ГОСТ Р 34.10-2001.
         * @deprecated
         */
        public static final IXAdESConfig CONFIG_2001_S = new Default(new ContainerXAdES2001());

        /**
         * Контейнер с паролем  подписи ГОСТ Р 34.10-2012 (256).
         */
        public static final IXAdESConfig CONFIG_2012_S_WITH_PASS = new Default(new Container2012_256());

        /**
         * Контейнер подписи ГОСТ Р 34.10-2012 (256).
         */
        public static final IXAdESConfig CONFIG_2012_S = new Default(new ContainerXAdES2012_256());

        /**
         * Контейнер с паролем подписи ГОСТ Р 34.10-2012 (512).
         */
        public static final IXAdESConfig CONFIG_2012_L_WITH_PASS = new Default(new Container2012_512());

        /**
         * Контейнер подписи ГОСТ Р 34.10-2012 (512).
         */
        public static final IXAdESConfig CONFIG_2012_L = new Default(new ContainerXAdES2012_512());

        /**
         * Контейнер подписи ГОСТ Р 34.10-2001 с отозванным сертификатом (промежуточного УЦ).
         */
        public static final IXAdESConfig CONFIG_2001_R = new Default(new RevokedContainer2001());
    }

    /**
     * Вывод информации о подписи: кто подписал, тип подписи, штампы времени.
     *
     * @param signature XAdES подпись.
     */
    public static void printSignatureInfo(XAdESSignature signature) {
        System.out.println("$$$ Print signature information $$$");
        int signerIndex = 1;
        for (XAdESSigner signer : signature.getXAdESSignerInfos()) {
            printSignerInfo(signer, signerIndex++, "");
        }
    }
    /**
     * Вывод информации об отдельной подписи.
     *
     * @param signer Подпись.
     * @param index Индекс подписи.
     * @param tab Отступ для удобства печати.
     */
    private static void printSignerInfo(XAdESSigner signer, int index, String tab) {
        X509Certificate signerCert = signer.getSignerCertificate();
        System.out.println(tab + " Signature #" + index + " (" +
            XAdESType.getSignatureTypeName(signer.getSignatureType()) + ")" +
                (signerCert != null ? (" verified by " + signerCert.getSubjectDN()) : ""));
    }


}
