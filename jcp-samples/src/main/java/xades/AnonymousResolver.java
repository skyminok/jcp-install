/**
 * $RCSfileAnonymousResolver.java,v $
 * version $Revision: 36379 $
 * created 01.02.2018 16:22 by elvira
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xades;

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;

import org.w3c.dom.Attr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Класс AnonymousResolver реализует ResourceResolver и используется для
 * элементов, у которых отсутствует URI.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class AnonymousResolver extends ResourceResolverSpi {

    private InputStream inputStream = null;
    private byte[] bytes = null;
    private String fileName = null;

    /**
     * Конструктор на основе байтового массива.
     *
     * @param data Данные.
     */
    public AnonymousResolver(byte[] data) {
        this.bytes = data;
    }

    /**
     * Конструктор на основе потока данных.
     *
     * @param stream Данные.
     */
    public AnonymousResolver(InputStream stream) {
        this.inputStream = stream;
    }

    /**
     * Конструктор на основе пути к файлу.
     * Используется только в однопоточном режиме.
     * В многопоточном режиме небезопасен!
     *
     * @param fileName Путь к файлу с данными.
     */
    public AnonymousResolver(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) throws ResourceResolverException {

        XMLSignatureInput result = null;

        if (bytes != null) {
            result = new XMLSignatureInput(bytes);
        } // if
        else if(fileName != null) {

            try(FileInputStream is = new FileInputStream(fileName)) {
                result = new XMLSignatureInput(is);
            } catch (IOException e) {
                throw new ResourceResolverException(e.getMessage(), context.uriToResolve, context.baseUri);
            }

        } // else
        else if (inputStream != null) {
            result = new XMLSignatureInput(inputStream);
        } // else

        if (result != null) {
            result.setExcludeComments(true);
            result.setMIMEType("text/xml");
        }

        return result;
    }


    @Override
    public boolean engineCanResolveURI(ResourceResolverContext context) {

        if (context.attr == null) {
            return true;
        }

        return false;
    }

}
