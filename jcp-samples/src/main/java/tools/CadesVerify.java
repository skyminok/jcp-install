/**
 * $RCSfileCadesVerify.java,v $
 * version $Revision$
 * created 24.05.2018 14:05 by elvira
 * last modified $Date$ by $Author$
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package tools;

import CAdES.configuration.Configuration;
import org.bouncycastle.tsp.TimeStampToken;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESSignerXLT1;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.FileInputStream;
import java.util.*;

/**
 * Утилита для проверки Cades и CMS подписи.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CadesVerify extends VerifyTool {

    /**
     * Тип подписи CAdES-BES.
     */
    public static final String TYPE_CADES_BES = "CADES_BES";

    /**
     * Тип подписи CAdES-T.
     */
    public static final String TYPE_CADES_T = "CADES_T";

    /**
     * Тип подписи CAdES-X Long Type 1.
     */
    public static final String TYPE_CADES_X_LT_1 = "CADES_X_LT_1";

    /**
     * Конструктор. Заполняем списки допустимых параметров командной строки.
     */
    public CadesVerify() {

        validArguments = new ArrayList<String>() {{
            add("-sig");
            add("-type");
            add("-cert");
            add("-crl");
            add("-data");
            add("-d");
            add("-log");
            add("-logpath");
        }};

        validArgumentsHelp = new ArrayList<String>() {{
            add("-sig <arg>         full path to file with signature");
            add("-type <arg>        type of signature. Available types: " + TYPE_CADES_BES + ", " + TYPE_CADES_T + " , " + TYPE_CADES_X_LT_1 + ". If null, type will be defined automatically");
            add("-cert <arg>        full path to folder with certificates");
            add("-crl <arg>         full path to folder with crls (default equals certstore)");
            add("-data <arg>        full path to signed data (in case of detached signature)");
            add("-d                 is signature detached (default - attached)");
            add("-log <arg>         set log level. Available values: 1-5. 1 (ERROR), 2 (WARNING), 3 (INFO), 4 (FINE), 5 (ALL). Default - OFF");
            add("-logpath <arg>     set full path to logging file (default - null)");
        }};

    }

    @Override
    public void setSignatureType() throws Exception {
        if (stringType == null)
            type = null;
        else if (stringType.equals(TYPE_CADES_BES))
            type = CAdESType.CAdES_BES;
        else if (stringType.equals(TYPE_CADES_T))
            type = CAdESType.CAdES_T;
        else if (stringType.equals(TYPE_CADES_X_LT_1))
            type = CAdESType.CAdES_X_Long_Type_1;
        else
            throw new Exception("Unsupported type");
    }

    @Override
    public void verifySignature(String[] args) throws Exception {

        prepareVerify(args);
        FileInputStream dataStream = null;

        if (isDetached)
            dataStream = new FileInputStream(dataPath);

        FileInputStream sigStream = new FileInputStream(sigPath);
        CAdESSignature cadesSignature = new CAdESSignature(sigStream, dataStream, type);

        cadesSignature.verify(chain, crlList);

        if (dataStream != null)
            dataStream.close();

        sigStream.close();
        // Configuration.printSignatureInfo(cadesSignature);

        System.out.println("CAdES verification completed.");

    }

    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(false);
        CadesVerify cadesVerify = new CadesVerify();
        cadesVerify.verifySignature(args);

    }


    /**
     * Вывод информации о подписантах.
     *
     * @param signers Список подписантов.
     * @throws Exception
     */
    public static void printCAdESSignersInfo(CAdESSigner[] signers)
        throws Exception {

        for (int i = 0; i < signers.length; i++) {

            CAdESSigner signer = signers[i];
            if (signer instanceof CAdESSignerXLT1) {

                CAdESSignerXLT1 cAdESSignerXLT1 = (CAdESSignerXLT1) signer;
                System.out.println("Check timestamps #" + i + ":");

                TimeStampToken signTimestamp = cAdESSignerXLT1.getEarliestValidSignatureTimeStampToken();
                if (signTimestamp == null) {
                    throw new Exception("Signature timestamp is null");
                } // if

                TimeStampToken cdsCTimestamp = cAdESSignerXLT1.getEarliestValidCAdESCTimeStampToken();
                if (cdsCTimestamp == null) {
                    throw new Exception("CAdES-C timestamp is null");
                } // if

                List<TimeStampToken> signatureTimeStampTokens = cAdESSignerXLT1.getSignatureTimestampTokens();
                if (signatureTimeStampTokens == null) {
                    throw new Exception("Signature timestamp list is null");
                } // if

                int sz = signatureTimeStampTokens.size();
                if (sz != 1) {
                    throw new Exception("It is weird... Size of signature timestamp " +
                        "list is more than 1 (" + sz + ")");
                } // if

                List<TimeStampToken> cadesCTimeStampTokens = cAdESSignerXLT1.getCAdESCTimestampTokens();
                if (cadesCTimeStampTokens == null) {
                    throw new Exception("CAdES-C timestamp list is null");
                } // if

                sz = cadesCTimeStampTokens.size();
                if (sz != 1) {
                    throw new Exception("It is weird... Size of CAdES-C timestamp " +
                        "list is more than 1 (" + sz + ")");
                } // if

            } // if

        } // for

    }
}
