/**
 * $RCSfileXAdESUtility.java,v $
 * version $Revision: 36379 $
 * created 03.06.2015 16:27 by afevma
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

import CAdES.configuration.Configuration;

import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import ru.CryptoPro.JCP.JCP;

import ru.CryptoPro.JCP.tools.Platform;
import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.JCPxml.utility.DocumentIdResolver;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;

import java.security.Provider;
import java.security.Security;
import java.util.*;

/**
 * Служебный интерфейс с константами и вспомогательными
 * функциями.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class GostXAdESUtility extends XMLUtility implements IXAdESCommon {

    /**
     * Адрес тестового сервиса.
     */
    private final static String GIS_GMP_SERVICE =
        "http://smev-mvf.test.gosuslugi.ru:7777/gateway/services/SID0003663";

    /**
     * Список пар "oid_алгоритма_хеширования=urn_алгоритма_хеширования".
     */
    public static final List<Map.Entry<String, String>> MAP_DIGEST_OID_2_DIGEST_URN =
        new LinkedList<Map.Entry<String, String>>() {{

        add(new AbstractMap.SimpleEntry<String, String>(JCP.GOST_DIGEST_OID, Consts.URI_GOST_DIGEST));
        add(new AbstractMap.SimpleEntry<String, String>(JCP.GOST_DIGEST_OID, Consts.URN_GOST_DIGEST));
        add(new AbstractMap.SimpleEntry<String, String>(JCP.GOST_DIGEST_2012_256_OID, Consts.URN_GOST_DIGEST_2012_256)); // >= JCP 2.0
        add(new AbstractMap.SimpleEntry<String, String>(JCP.GOST_DIGEST_2012_512_OID, Consts.URN_GOST_DIGEST_2012_512)); // >= JCP 2.0

    }};

    /**
     * Список пар "oid_алгоритма_ключа=urn_алгоритма_подписи".
     */
    public static final Map<String, String> MAP_KEY_ALG_2_SIGN_URN =
        new LinkedHashMap<String, String>() {{

            put("GOST3410",   Consts.URI_GOST_SIGN); // < JCP 2.0
            put("GOST3410DH", Consts.URI_GOST_SIGN); // < JCP 2.0
            put(JCP.GOST_EL_DEGREE_NAME, Consts.URI_GOST_SIGN);
            put(JCP.GOST_EL_DH_NAME,     Consts.URI_GOST_SIGN);
            put(JCP.GOST_EL_2012_256_NAME, Consts.URN_GOST_SIGN_2012_256);  // >= JCP 2.0
            put(JCP.GOST_DH_2012_256_NAME, Consts.URN_GOST_SIGN_2012_256);  // >= JCP 2.0
            put(JCP.GOST_EL_2012_512_NAME, Consts.URN_GOST_SIGN_2012_512);  // >= JCP 2.0
            put(JCP.GOST_DH_2012_512_NAME, Consts.URN_GOST_SIGN_2012_512);  // >= JCP 2.0

    }};

    /**
     * Список пар "oid_алгоритма_ключа=urn_алгоритма_хеширования".
     */
    public static final Map<String, String> MAP_KEY_ALG_2_DIGEST_URN =
        new LinkedHashMap<String, String>() {{

            put("GOST3410",   Consts.URI_GOST_DIGEST); // < JCP 2.0
            put("GOST3410DH", Consts.URI_GOST_DIGEST); // < JCP 2.0
            put(JCP.GOST_EL_DEGREE_NAME, Consts.URI_GOST_DIGEST);
            put(JCP.GOST_EL_DH_NAME,     Consts.URI_GOST_DIGEST);
            put(JCP.GOST_EL_2012_256_NAME, Consts.URN_GOST_DIGEST_2012_256); // >= JCP 2.0
            put(JCP.GOST_DH_2012_256_NAME, Consts.URN_GOST_DIGEST_2012_256); // >= JCP 2.0
            put(JCP.GOST_EL_2012_512_NAME, Consts.URN_GOST_DIGEST_2012_512); // >= JCP 2.0
            put(JCP.GOST_DH_2012_512_NAME, Consts.URN_GOST_DIGEST_2012_512); // >= JCP 2.0

    }};

    /**
     * Список пар "oid_алгоритма_хеширования=url_tsp_службы".
     */
    public static final Map<String, String> MAP_DIGEST_OID_2_TSA_URL =
        new LinkedHashMap<String, String>() {{

            put(JCP.GOST_DIGEST_OID, Configuration.TSA_DEFAULT_ADDRESS);
            put(JCP.GOST_DIGEST_2012_256_OID, Configuration.TSA_DEFAULT_ADDRESS); // >= JCP 2.0
            put(JCP.GOST_DIGEST_2012_512_OID, Configuration.TSA_DEFAULT_ADDRESS); // > >= JCP 2.0

    }};

    static {

        // BasicConfigurator.configure(); // для wss4j

        // Инициализация JCP XML провайдера.
        // com.sun.org.apache.xml.internal.security.Init.init(); < JCP 2.0

        try {
            if (!JCPXMLDSigInit.isInitialized()) {
                JCPXMLDSigInit.init();
            } // if
        }
        catch(Throwable th){
            th.printStackTrace();
        }

        // ID resolver, JCPxml < 2.0

        if (!Platform.isAndroid) {
            DocumentIdResolver idResolver = new DocumentIdResolver();
            ResourceResolver.register(idResolver, true);
        } // if

        // Загрузка провайдера XML DSig (для wss4j).

        Security.insertProviderAt(xmlDSigRi, 1);

        // Переопределяем свойства встроенного XML DSig провайдера (для wss4j).

        Provider provider = Security.getProvider("XMLDSig");
        if (provider != null) {

            Security.getProvider("XMLDSig").put("XMLSignatureFactory.DOM",
                "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMXMLSignatureFactory");

            Security.getProvider("XMLDSig").put("KeyInfoFactory.DOM",
                "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMKeyInfoFactory");

        }

    }

    /**
     * Функция поиска oid'а алгоритма хеширования по urn
     * алгоритма хеширования.
     *
     * @param digestUri Urn алгоритма хеширования.
     * @return oid алгоритма.
     */
    public static String digestUri2Digest(ASN1ObjectIdentifier digestUri) {
        return digestUri2Digest(digestUri.getId());
    }

    /**
     * Функция поиска oid'а алгоритма хеширования по urn
     * алгоритма хеширования.
     *
     * @param digestUri Urn алгоритма хеширования.
     * @return oid алгоритма.
     */
    public static String digestUri2Digest(String digestUri) {

        for (Map.Entry<String, String> entry : MAP_DIGEST_OID_2_DIGEST_URN) {
            if (entry.getValue().equalsIgnoreCase(digestUri)) {
                return entry.getKey();
            }
        } // for

        return digestUri;

    }

    /**
     * Функция поиска urn алгоритма подписи по алгоритму ключа.
     *
     * @param keyAlg Алгоритм ключа.
     * @return urn алгоритма.
     */
    public static String key2SignatureUrn(String keyAlg) {

        if (MAP_KEY_ALG_2_SIGN_URN.containsKey(keyAlg)) {
            return MAP_KEY_ALG_2_SIGN_URN.get(keyAlg);
        } // if

        return keyAlg;

    }

    /**
     * Функция поиска urn алгоритма хеширования по алгоритму ключа.
     *
     * @param keyAlg Алгоритм ключа.
     * @return urn алгоритма.
     */
    public static String key2DigestUrn(String keyAlg) {

        if (MAP_KEY_ALG_2_DIGEST_URN.containsKey(keyAlg)) {
            return MAP_KEY_ALG_2_DIGEST_URN.get(keyAlg);
        } // if

        return keyAlg;

    }

    /**
     * Функция поиска адреса TSP службы по oid'у алгоритма хеширования.
     *
     * @param tsaMap Список пар "oid_алгоритма_хеширования=адрес_tsp_службы".
     * @param digestOid Oid алгоритма хеширования.
     * @return адрес TSP службы.
     */
    public static String digestOid2TsaUrl(Map<String, String> tsaMap,
        String digestOid) {

        if (tsaMap.containsKey(digestOid)) {
            return tsaMap.get(digestOid);
        } // if

        return digestOid;

    }

}
