/**
 * $RCSfileRutokenSample.java,v $
 * version $Revision$
 * created 21.07.2020 18:51 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2020.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.Rutoken.RutokenStoreParameter;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

/**
 * Данный пример демонстрирует получение списка имен
 * хранилищ KeyStore для работы с рутокенами.
 * Для каждого такого хранилища через объект
 * {@link ru.CryptoPro.JCP.KeyStore.Rutoken.RutokenStoreParameter}
 * можно получить серийный номер токена.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class RutokenSample {

    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(true);

        Provider prov = Security.getProvider(JCP.PROVIDER_NAME);
        Set<Provider.Service> services = prov.getServices();

        Vector<String> storeTypes = new Vector();
        for (Provider.Service service : services) {

            String serviceName = service.getType();
            String algorithm = service.getAlgorithm();

            if (serviceName.equals("KeyStore") && algorithm.contains("Rutoken")) {
                storeTypes.add(algorithm);
            } // if

        }

        for (String nextRutokenStore : storeTypes) {

            KeyStore keyStore = KeyStore.getInstance(nextRutokenStore);
            RutokenStoreParameter parameter = new RutokenStoreParameter();

            keyStore.load(parameter);
            String serialNumber = parameter.getSerialNumber();

            System.out.println("Store name: " + nextRutokenStore
                + " serial number: " + serialNumber);

            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                System.out.println(certificate.getSubjectX500Principal());
            }
        }

    }

}
