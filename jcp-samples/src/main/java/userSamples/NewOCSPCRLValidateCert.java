/**
 * $RCSfileNewOCSPCRLValidateCert.java,v $ version $Revision$ created 06.11.2020
 * 19:29 by afevma last modified $Date$ by $Author$ (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.ByteArrayInputStream;
import java.security.cert.*;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Пример построения цепочки сертификатов и проверки
 * их статусов с использованием OCSP службы или CRL.
 *
 *  Java 10+
 *
 *  Цепочка сертификатов зафиксирована в коде и состоит
 *  из трех сертификатов.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class NewOCSPCRLValidateCert {

    // Корневой сертификат тестового УЦ https://testca2012.cryptopro.ru/ui
    private static final String ROOT_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIFxzCCBXSgAwIBAgIRAjA/qgCtq0avT2VPjW00FTMwCgYIKoUDBwEBAwIwggFV\n" +
            "MSAwHgYJKoZIhvcNAQkBFhFpbmZvQGNyeXB0b3Byby5ydTEYMBYGBSqFA2QBEg0x\n" +
            "MDM3NzAwMDg1NDQ0MRowGAYIKoUDA4EDAQESDDAwNzcxNzEwNzk5MTELMAkGA1UE\n" +
            "BhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEVMBMGA1UEBwwM0JzQvtGB\n" +
            "0LrQstCwMS8wLQYDVQQJDCbRg9C7LiDQodGD0YnRkdCy0YHQutC40Lkg0LLQsNC7\n" +
            "INC0LiAxODElMCMGA1UECgwc0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIjFl\n" +
            "MGMGA1UEAwxc0KLQtdGB0YLQvtCy0YvQuSDQs9C+0LvQvtCy0L3QvtC5INCj0KYg\n" +
            "0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIiDQk9Ce0KHQoiAyMDEyICjQo9Cm\n" +
            "IDIuMCkwHhcNMjAwNDMwMTAwOTUxWhcNMzUwNDMwMTAwOTUxWjCCAVUxIDAeBgkq\n" +
            "hkiG9w0BCQEWEWluZm9AY3J5cHRvcHJvLnJ1MRgwFgYFKoUDZAESDTEwMzc3MDAw\n" +
            "ODU0NDQxGjAYBggqhQMDgQMBARIMMDA3NzE3MTA3OTkxMQswCQYDVQQGEwJSVTEY\n" +
            "MBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAx\n" +
            "LzAtBgNVBAkMJtGD0LsuINCh0YPRidGR0LLRgdC60LjQuSDQstCw0Lsg0LQuIDE4\n" +
            "MSUwIwYDVQQKDBzQntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4iMWUwYwYDVQQD\n" +
            "DFzQotC10YHRgtC+0LLRi9C5INCz0L7Qu9C+0LLQvdC+0Lkg0KPQpiDQntCe0J4g\n" +
            "ItCa0KDQmNCf0KLQni3Qn9Cg0J4iINCT0J7QodCiIDIwMTIgKNCj0KYgMi4wKTBm\n" +
            "MB8GCCqFAwcBAQEBMBMGByqFAwICIwEGCCqFAwcBAQICA0MABEC2spzU63SBkVbc\n" +
            "fQWxoLzvKDOJVqzjltjF4vTGthspRcXj3Sot1g6s2zw/x3IQ7rlId5PVsYCuW/iq\n" +
            "iTN9J1ceo4ICEjCCAg4wDgYDVR0PAQH/BAQDAgGGMB0GA1UdDgQWBBS0KWn/r9M4\n" +
            "iPwTQ0lmYd0zN71qCTAPBgNVHRMBAf8EBTADAQH/MCUGA1UdIAQeMBwwBgYEVR0g\n" +
            "ADAIBgYqhQNkcQEwCAYGKoUDZHECMIIBGgYFKoUDZHAEggEPMIIBCww00KHQmtCX\n" +
            "0JggItCa0YDQuNC/0YLQvtCf0YDQviBDU1AiICjQstC10YDRgdC40Y8gNC4wKQwx\n" +
            "0J/QkNCaICLQmtGA0LjQv9GC0L7Qn9GA0L4g0KPQpiIg0LLQtdGA0YHQuNC4IDIu\n" +
            "MAxP0KHQtdGA0YLQuNGE0LjQutCw0YIg0YHQvtC+0YLQstC10YLRgdGC0LLQuNGP\n" +
            "IOKEliDQodCkLzEyNC0zMzgwINC+0YIgMTEuMDUuMjAxOAxP0KHQtdGA0YLQuNGE\n" +
            "0LjQutCw0YIg0YHQvtC+0YLQstC10YLRgdGC0LLQuNGPIOKEliDQodCkLzEyOC0z\n" +
            "NTkyINC+0YIgMTcuMTAuMjAxODA/BgUqhQNkbwQ2DDTQodCa0JfQmCAi0JrRgNC4\n" +
            "0L/RgtC+0J/RgNC+IENTUCIgKNCy0LXRgNGB0LjRjyA0LjApMBEGCSsGAQQBgjcU\n" +
            "AgQEDAJDQTASBgkrBgEEAYI3FQEEBQIDAgACMB8GCSsGAQQBgjcVBwQSMBAGCCqF\n" +
            "AwICLgAAAgEBAgEAMAoGCCqFAwcBAQMCA0EAURkTYACvqsErRyEwz+uwCAL1sBdG\n" +
            "/crOhEsiPsPiyAGf5K2UZ6FsOAHMGw+pTRUtkx8/KXNEGpUowqdTPkeFMg==\n" +
            "-----END CERTIFICATE-----\n";

    // Промежуточный сертификат тестового УЦ https://testca2012.cryptopro.ru/ui
    private static final String CA_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIIJzCCB9SgAwIBAgIRAm3PtACtq0uIRg2Y+dz8Ly4wCgYIKoUDBwEBAwIwggFV\n" +
            "MSAwHgYJKoZIhvcNAQkBFhFpbmZvQGNyeXB0b3Byby5ydTEYMBYGBSqFA2QBEg0x\n" +
            "MDM3NzAwMDg1NDQ0MRowGAYIKoUDA4EDAQESDDAwNzcxNzEwNzk5MTELMAkGA1UE\n" +
            "BhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEVMBMGA1UEBwwM0JzQvtGB\n" +
            "0LrQstCwMS8wLQYDVQQJDCbRg9C7LiDQodGD0YnRkdCy0YHQutC40Lkg0LLQsNC7\n" +
            "INC0LiAxODElMCMGA1UECgwc0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIjFl\n" +
            "MGMGA1UEAwxc0KLQtdGB0YLQvtCy0YvQuSDQs9C+0LvQvtCy0L3QvtC5INCj0KYg\n" +
            "0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIiDQk9Ce0KHQoiAyMDEyICjQo9Cm\n" +
            "IDIuMCkwHhcNMjAwNDMwMTA0ODIyWhcNMzAwNDMwMTA1ODIyWjCCAVsxIDAeBgkq\n" +
            "hkiG9w0BCQEWEWluZm9AY3J5cHRvcHJvLnJ1MRgwFgYFKoUDZAESDTEwMzc3MDAw\n" +
            "ODU0NDQxGjAYBggqhQMDgQMBARIMMDA3NzE3MTA3OTkxMQswCQYDVQQGEwJSVTEY\n" +
            "MBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAx\n" +
            "LzAtBgNVBAkMJtGD0LsuINCh0YPRidGR0LLRgdC60LjQuSDQstCw0Lsg0LQuIDE4\n" +
            "MSUwIwYDVQQKDBzQntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4iMWswaQYDVQQD\n" +
            "DGLQotC10YHRgtC+0LLRi9C5INC/0L7QtNGH0LjQvdC10L3QvdGL0Lkg0KPQpiDQ\n" +
            "ntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4iINCT0J7QodCiIDIwMTIgKNCj0KYg\n" +
            "Mi4wKTBmMB8GCCqFAwcBAQEBMBMGByqFAwICIwEGCCqFAwcBAQICA0MABECHLOjy\n" +
            "tUQXix/Mo6wMZ+3xujU/9wK0GN28BxHJszi+9zZ+rcxBAmuBci2PkP0Mgh3HGPC1\n" +
            "lgiXLwcMbevuuq8Zo4IEbDCCBGgwPwYFKoUDZG8ENgw00KHQmtCX0JggItCa0YDQ\n" +
            "uNC/0YLQvtCf0YDQviBDU1AiICjQstC10YDRgdC40Y8gNC4wKTASBgkrBgEEAYI3\n" +
            "FQEEBQIDAgACMB0GA1UdDgQWBBQGS2MlM2YqJDgYckN6O7fLssr8czAOBgNVHQ8B\n" +
            "Af8EBAMCAYYwEgYDVR0TAQH/BAgwBgEB/wIBADAlBgNVHSAEHjAcMAYGBFUdIAAw\n" +
            "CAYGKoUDZHEBMAgGBiqFA2RxAjBrBggrBgEFBQcBAQRfMF0wWwYIKwYBBQUHMAKG\n" +
            "T2h0dHA6Ly90ZXN0Y2EyMDEyLmNyeXB0b3Byby5ydS9haWEvYjQyOTY5ZmZhZmQz\n" +
            "Mzg4OGZjMTM0MzQ5NjY2MWRkMzMzN2JkNmEwOS5jcnQwHwYJKwYBBAGCNxUHBBIw\n" +
            "EAYIKoUDAgIuAAECAQECAQAwggEaBgUqhQNkcASCAQ8wggELDDTQodCa0JfQmCAi\n" +
            "0JrRgNC40L/RgtC+0J/RgNC+IENTUCIgKNCy0LXRgNGB0LjRjyA0LjApDDHQn9CQ\n" +
            "0JogItCa0YDQuNC/0YLQvtCf0YDQviDQo9CmIiDQstC10YDRgdC40LggMi4wDE/Q\n" +
            "odC10YDRgtC40YTQuNC60LDRgiDRgdC+0L7RgtCy0LXRgtGB0YLQstC40Y8g4oSW\n" +
            "INCh0KQvMTI0LTMzODAg0L7RgiAxMS4wNS4yMDE4DE/QodC10YDRgtC40YTQuNC6\n" +
            "0LDRgiDRgdC+0L7RgtCy0LXRgtGB0YLQstC40Y8g4oSWINCh0KQvMTI4LTM1OTIg\n" +
            "0L7RgiAxNy4xMC4yMDE4MGAGA1UdHwRZMFcwVaBToFGGT2h0dHA6Ly90ZXN0Y2Ey\n" +
            "MDEyLmNyeXB0b3Byby5ydS9jZHAvYjQyOTY5ZmZhZmQzMzg4OGZjMTM0MzQ5NjY2\n" +
            "MWRkMzMzN2JkNmEwOS5jcmwwggGXBgNVHSMEggGOMIIBioAUtClp/6/TOIj8E0NJ\n" +
            "ZmHdMze9agmhggFdpIIBWTCCAVUxIDAeBgkqhkiG9w0BCQEWEWluZm9AY3J5cHRv\n" +
            "cHJvLnJ1MRgwFgYFKoUDZAESDTEwMzc3MDAwODU0NDQxGjAYBggqhQMDgQMBARIM\n" +
            "MDA3NzE3MTA3OTkxMQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQ\n" +
            "stCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxLzAtBgNVBAkMJtGD0LsuINCh0YPR\n" +
            "idGR0LLRgdC60LjQuSDQstCw0Lsg0LQuIDE4MSUwIwYDVQQKDBzQntCe0J4gItCa\n" +
            "0KDQmNCf0KLQni3Qn9Cg0J4iMWUwYwYDVQQDDFzQotC10YHRgtC+0LLRi9C5INCz\n" +
            "0L7Qu9C+0LLQvdC+0Lkg0KPQpiDQntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4i\n" +
            "INCT0J7QodCiIDIwMTIgKNCj0KYgMi4wKYIRAjA/qgCtq0avT2VPjW00FTMwCgYI\n" +
            "KoUDBwEBAwIDQQDDXANhFtqxpV/A6hTAGbIp2q2BCQNK92ecW8U96Zj1uZF43uTv\n" +
            "Ixfq4XZDT1m3G5Ks0ll8xLOZEBhLao7Jg/wE\n" +
            "-----END CERTIFICATE-----\n";

    // Клиентский сертификат тестового УЦ https://testca2012.cryptopro.ru/ui
    private static final String CLIENT_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIHczCCByCgAwIBAgIRAkdyEwFrrBuZTHVhgC+DzIcwCgYIKoUDBwEBAwIwggFb\n" +
            "MSAwHgYJKoZIhvcNAQkBFhFpbmZvQGNyeXB0b3Byby5ydTEYMBYGBSqFA2QBEg0x\n" +
            "MDM3NzAwMDg1NDQ0MRowGAYIKoUDA4EDAQESDDAwNzcxNzEwNzk5MTELMAkGA1UE\n" +
            "BhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEVMBMGA1UEBwwM0JzQvtGB\n" +
            "0LrQstCwMS8wLQYDVQQJDCbRg9C7LiDQodGD0YnRkdCy0YHQutC40Lkg0LLQsNC7\n" +
            "INC0LiAxODElMCMGA1UECgwc0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIjFr\n" +
            "MGkGA1UEAwxi0KLQtdGB0YLQvtCy0YvQuSDQv9C+0LTRh9C40L3QtdC90L3Ri9C5\n" +
            "INCj0KYg0J7QntCeICLQmtCg0JjQn9Ci0J4t0J/QoNCeIiDQk9Ce0KHQoiAyMDEy\n" +
            "ICjQo9CmIDIuMCkwHhcNMjAxMTA2MTYzMjUyWhcNMjEwMjA2MTY0MjUyWjAgMQsw\n" +
            "CQYDVQQGEwJSVTERMA8GA1UEAwwIcmV2Y2hlY2swZjAfBggqhQMHAQEBATATBgcq\n" +
            "hQMCAiQABggqhQMHAQECAgNDAARAiwo7BF8ed2RMmdbhUoJuJVNH9KpgTc53lVib\n" +
            "s1ixkhS0sUSwtA7nt9eTbscRNHFm5/rEPZv9D5pwl14XZFik7aOCBO8wggTrMA4G\n" +
            "A1UdDwEB/wQEAwIE8DAfBgkrBgEEAYI3FQcEEjAQBggqhQMCAi4ACAIBAQIBADAd\n" +
            "BgNVHQ4EFgQUMLv6wqn0MlrSq6CHzEAPrCOylAMwJgYDVR0lBB8wHQYIKwYBBQUH\n" +
            "AwQGCCsGAQUFBwMCBgcqhQMCAiIGMDIGCSsGAQQBgjcVCgQlMCMwCgYIKwYBBQUH\n" +
            "AwQwCgYIKwYBBQUHAwIwCQYHKoUDAgIiBjCBpwYIKwYBBQUHAQEEgZowgZcwOAYI\n" +
            "KwYBBQUHMAGGLGh0dHA6Ly90ZXN0Y2EyMDEyLmNyeXB0b3Byby5ydS9vY3NwL29j\n" +
            "c3Auc3JmMFsGCCsGAQUFBzAChk9odHRwOi8vdGVzdGNhMjAxMi5jcnlwdG9wcm8u\n" +
            "cnUvYWlhLzA2NGI2MzI1MzM2NjJhMjQzODE4NzI0MzdhM2JiN2NiYjJjYWZjNzMu\n" +
            "Y3J0MB0GA1UdIAQWMBQwCAYGKoUDZHECMAgGBiqFA2RxATArBgNVHRAEJDAigA8y\n" +
            "MDIwMTEwNjE2MzI1MlqBDzIwMjEwMjA2MTYzMjUyWjCCARoGBSqFA2RwBIIBDzCC\n" +
            "AQsMNNCh0JrQl9CYICLQmtGA0LjQv9GC0L7Qn9GA0L4gQ1NQIiAo0LLQtdGA0YHQ\n" +
            "uNGPIDQuMCkMMdCf0JDQmiAi0JrRgNC40L/RgtC+0J/RgNC+INCj0KYiINCy0LXR\n" +
            "gNGB0LjQuCAyLjAMT9Ch0LXRgNGC0LjRhNC40LrQsNGCINGB0L7QvtGC0LLQtdGC\n" +
            "0YHRgtCy0LjRjyDihJYg0KHQpC8xMjQtMzM4MCDQvtGCIDExLjA1LjIwMTgMT9Ch\n" +
            "0LXRgNGC0LjRhNC40LrQsNGCINGB0L7QvtGC0LLQtdGC0YHRgtCy0LjRjyDihJYg\n" +
            "0KHQpC8xMjgtMzU5MiDQvtGCIDE3LjEwLjIwMTgwLAYFKoUDZG8EIwwh0KHQmtCX\n" +
            "0JggItCa0YDQuNC/0YLQvtCf0YDQviBDU1AiMGAGA1UdHwRZMFcwVaBToFGGT2h0\n" +
            "dHA6Ly90ZXN0Y2EyMDEyLmNyeXB0b3Byby5ydS9jZHAvMDY0YjYzMjUzMzY2MmEy\n" +
            "NDM4MTg3MjQzN2EzYmI3Y2JiMmNhZmM3My5jcmwwggGXBgNVHSMEggGOMIIBioAU\n" +
            "BktjJTNmKiQ4GHJDeju3y7LK/HOhggFdpIIBWTCCAVUxIDAeBgkqhkiG9w0BCQEW\n" +
            "EWluZm9AY3J5cHRvcHJvLnJ1MRgwFgYFKoUDZAESDTEwMzc3MDAwODU0NDQxGjAY\n" +
            "BggqhQMDgQMBARIMMDA3NzE3MTA3OTkxMQswCQYDVQQGEwJSVTEYMBYGA1UECAwP\n" +
            "Nzcg0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxLzAtBgNVBAkM\n" +
            "JtGD0LsuINCh0YPRidGR0LLRgdC60LjQuSDQstCw0Lsg0LQuIDE4MSUwIwYDVQQK\n" +
            "DBzQntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4iMWUwYwYDVQQDDFzQotC10YHR\n" +
            "gtC+0LLRi9C5INCz0L7Qu9C+0LLQvdC+0Lkg0KPQpiDQntCe0J4gItCa0KDQmNCf\n" +
            "0KLQni3Qn9Cg0J4iINCT0J7QodCiIDIwMTIgKNCj0KYgMi4wKYIRAm3PtACtq0uI\n" +
            "Rg2Y+dz8Ly4wCgYIKoUDBwEBAwIDQQBKK6+zJgW9LGYYqTD7bTlZ722Hnviw5KjY\n" +
            "/zdSjj/hXnGOmckPYIYLp28zBtWzYQPAJnjxCQCP4hspcRX3Q8hh\n" +
            "-----END CERTIFICATE-----\n";

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.

        JCPInit.initProviders(false); // JCP - провайдер по умолчанию.

        final CertificateFactory factory = CertificateFactory
            .getInstance("X509");

        final Certificate client = factory.generateCertificate(
            new ByteArrayInputStream(CLIENT_CERT.getBytes()));

        final Certificate ca = factory.generateCertificate(
            new ByteArrayInputStream(CA_CERT.getBytes()));

        final Certificate root = factory.generateCertificate(
            new ByteArrayInputStream(ROOT_CERT.getBytes()));

        List<Certificate> all = Stream.of(client, ca, root)
            .collect(Collectors.toList());

        // Варианты проверок.

        validate(client, root, all, false); // с помощью OCSP
        validate(client, root, all, true); // с помощью CRL

    }

    /**
     * Проверка цепочки сертификатов.
     *
     * @param client Сертификат клиента.
     * @param root Корневой сертификат.
     * @param all Все сертификаты.
     * @param withCRL True, если для проверки следует
     * использовать CRL.
     * @throws Exception
     */
    private static void validate(Certificate client, Certificate root,
        List<Certificate> all, boolean withCRL) throws Exception {

        System.out.println("%%% Certificate validation using "
            + (withCRL ? "CRL" : "OCSP") + " %%%");

        final Set<TrustAnchor> trust = new HashSet<TrustAnchor>(0);
        trust.add(new TrustAnchor((X509Certificate) root, null));

        // Построение цепочки сертификатов.

        final PKIXBuilderParameters cpp =
            new PKIXBuilderParameters(trust, null);

        cpp.setSigProvider(null);

        final CollectionCertStoreParameters par =
            new CollectionCertStoreParameters(all);

        final CertStore store = CertStore.getInstance("Collection", par);
        cpp.addCertStore(store);

        final X509CertSelector selector = new X509CertSelector();

        selector.setCertificate((X509Certificate) client);
        cpp.setTargetCertConstraints(selector);
        cpp.setRevocationEnabled(false); // проверка статуса сертификата отключена при построении

        CertPathBuilder builder = CertPathBuilder
            .getInstance("CPPKIX", "RevCheck");

        final PKIXCertPathBuilderResult res =
            (PKIXCertPathBuilderResult)builder.build(cpp);

        final CertPath cp = res.getCertPath(); // цепочка сертификатов
        System.out.println(cp);

        final CertPathValidator DEFAULT = CertPathValidator
            .getInstance("PKIX"); // данный алгоритм имеет реализацию OCSP revocation checker

        CertPathChecker cpc = DEFAULT.getRevocationChecker();
        PKIXRevocationChecker prc = (PKIXRevocationChecker)cpc;

        prc.init(false);

        // Адрес OCSP службы согласно адресу УЦ. Его можно
        // опустить, если в сертификате есть AIA с адресом
        // службы.
        //
        // URI uri = new URI(OCSP_ADDRESS);
        // prc.setOcspResponder(uri);

        // Параметры проверки цепочки.
        //
        // Set<PKIXRevocationChecker.Option> options = EnumSet.
        //    of(PKIXRevocationChecker.Option.NO_FALLBACK);
        //
        // prc.setOptions(options);

        if (withCRL) { // с помощью CRL

            Set<PKIXRevocationChecker.Option> options = EnumSet.
                of(PKIXRevocationChecker.Option.PREFER_CRLS);

            prc.setOptions(options);

        } // if

        final CertPathValidator validator = CertPathValidator
            .getInstance("CPPKIX", "RevCheck"); // провайдер проверки, не имеет OCSP revocation checker

        // prc.setOcspResponderCert(cert);
        cpp.addCertPathChecker(prc); // задаем провайдеру проверки OCSP revocation checker

        validator.validate(cp, cpp);
        System.out.println("%%% Certificate validation completed %%%");

    }

}
