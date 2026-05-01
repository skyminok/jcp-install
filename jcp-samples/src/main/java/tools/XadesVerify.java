/**
 * $RCSfileXadesVerify.java,v $
 * version $Revision$
 * created 24.05.2018 18:30 by la
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

import org.w3c.dom.Document;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESType;
import xades.config.XAdESConfig;
import xades.util.XMLUtility;

import java.util.*;

/**
 * Утилита для проверки Xades и XML подписи.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XadesVerify extends VerifyTool {

    /**
     * Тип подписи XAdES-BES.
     */
    public static final String TYPE_XADES_BES = "XADES_BES";

    /**
     * Тип подписи XAdES-T.
     */
    public static final String TYPE_XADES_T = "XADES_T";

    /**
     * Тип подписи XAdES-X Long Type 1.
     */
    public static final String TYPE_XADES_X_LT_1 = "XADES_X_LT_1";

    /**
     * Конструктор. Заполняем списки допустимых параметров командной строки.
     */
    public XadesVerify() {

        validArguments = new ArrayList<String>() {{
            add("-sig");
            add("-type");
            add("-cert");
            add("-crl");
            add("-log");
            add("-logpath");
        }};

        validArgumentsHelp = new ArrayList<String>() {{
            add("-sig <arg>         full path to file with signature");
            add("-type <arg>        type of signature. Available types: " + TYPE_XADES_BES + " [default], " + TYPE_XADES_T + ", " + TYPE_XADES_X_LT_1);
            add("-cert <arg>        full path to folder with certificates");
            add("-crl <arg>         full path to folder with crls (default equals certstore)");
            add("-log <arg>         set log level. Available values: 1-5. 1 (ERROR), 2 (WARNING), 3 (INFO), 4 (FINE), 5 (ALL). Default - OFF");
            add("-logpath <arg>     set full path to logging file (default - null)");
        }};
    }

    public void setSignatureType() throws Exception {
        if (stringType == null)
            type = XAdESType.XAdES_BES;
        else if (stringType.equals(TYPE_XADES_BES))
            type = XAdESType.XAdES_BES;
        else if (stringType.equals(TYPE_XADES_T))
            type = XAdESType.XAdES_T;
        else if (stringType.equals(TYPE_XADES_X_LT_1))
            type = XAdESType.XAdES_X_Long_Type_1;
        else
            throw new Exception("Unsupported type");

    }

    @Override
    public void verifySignature(String[] args) throws Exception {

        prepareVerify(args);
        byte[] doc = Array.readFile(sigPath);

        Document signedDoc = XMLUtility.parseFile(doc);
        XAdESSignature xadesSignature = new XAdESSignature(signedDoc.getDocumentElement(), type);

        if (!type.equals(XAdESType.XAdES_X_Long_Type_1)) {
            xadesSignature.verify(chain, crlList);
        } // if
        else {
            xadesSignature.verify(null);
        } // else

        XAdESConfig.printSignatureInfo(xadesSignature);
        System.out.println("XAdES verification completed.");

    }

    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        XadesVerify xadesVerify = new XadesVerify();
        xadesVerify.verifySignature(args);
    }

}
