/**
 * Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package PDF;

import java.io.File;
import java.io.IOException;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import java.util.Properties;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Класс SignPDF предназначен для подписи PDF-файлов.
 * Используется пропатченный itextpdf версии 5.5.5.
 * 
 * @author afevma
 *
 */
public class SignPDF {

    /**
     * PDF документ для подписи.
     */
	private static final String IN_PDF = "-in";

    /**
     * Подписанный PDF документ для сохранения.
     */
	private static final String OUT_PDF = "-out";

    /**
     * Папка с PDF документами.
     */
	private static final String IN_DIR_PDF = "-indir";

    /**
     * Папка для сохранения подписанных PDF документов.
     */
	private static final String OUT_DIR_PDF = "-outdir";

    /**
     * Алиас ключа подписи.
     */
	private static final String ALIAS = "-alias";

    /**
     * Пароль к ключу.
     */
	private static final String PASSWORD = "-password";

    /**
     * Поле Location для добавления в подпись PDF документа.
     */
	private static final String LOCATION = "-location";

    /**
     * Поле Reason для добавления в подпись PDF документа.
     */
	private static final String REASON = "-reason";

	/**
	 * @param args Аргументы.
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length == 0) {
			help();
			System.exit(1);
		} // if

		JCPInit.initProviders(false);
		final Properties argList = new Properties();
		
		argList.setProperty(IN_PDF,      getValue(IN_PDF,      args, ""));
		argList.setProperty(IN_DIR_PDF,  getValue(IN_DIR_PDF,  args, ""));
		argList.setProperty(OUT_PDF,     getValue(OUT_PDF,     args, ""));
		argList.setProperty(OUT_DIR_PDF, getValue(OUT_DIR_PDF, args, ""));
		argList.setProperty(ALIAS,       getValue(ALIAS,       args, ""));
		argList.setProperty(PASSWORD,    getValue(PASSWORD,    args, ""));
		argList.setProperty(LOCATION,    getValue(LOCATION,    args, "Crypto-Pro LLC"));
		argList.setProperty(REASON,      getValue(REASON,      args, "JCP Documentation"));
		
		String srcDocument  = argList.getProperty(IN_PDF);
		String srcDirectory = argList.getProperty(IN_DIR_PDF);
		String dstDocument  = argList.getProperty(OUT_PDF);
		String dstDirectory = argList.getProperty(OUT_DIR_PDF);
		String alias        = argList.getProperty(ALIAS);
		String password     = argList.getProperty(PASSWORD);
		String location     = argList.getProperty(LOCATION);
		String reason       = argList.getProperty(REASON);
		
		argList.list(System.out);
		
		if (((srcDocument.length() == 0 || dstDocument.length() == 0) && 
			(srcDirectory.length() == 0 || dstDirectory.length() == 0)) || alias.length() == 0) {

            help();
			System.exit(1);

		} // if
		
		char[] real_password = password.length() != 0
            ? password.toCharArray()
            : null;
		
		// Если подписываем отдельный файл...
		if (srcDocument.length() > 0) {
			
			File file = new File(srcDocument);
			File destination = new File(dstDocument);
			
			proceedOneFile(file, destination, alias, real_password,
                location, reason);
			
		} // if
		// Если подписываем файлы в папке.
		else {
			
			File source = new File(srcDirectory);
			File[] files = source.listFiles();
			
			if (files == null || files.length == 0) {
				return;
			}

			File destination = new File(dstDirectory);
			destination.mkdirs();

			for (File file : files) {
				proceedOneFile(file, destination, alias, real_password, location, reason);
			} // for
			
		} // else

	}
	
	/**
	 * Информация о запуске.
	 * 
	 */
	private static void help() {
		System.out.println("-in <file.pdf> -out <file.pdf> [-in_dir <directory> -out_dir <directory>] " +
			"-alias <key> -password <pin> [-location <location> -reason <reason>]");
	}
	
	/**
	 * Извлечение параметров.
	 * 
	 * @param com параметр.
	 * @param arg аргументы командной строки (пары парамтр значение параметра).
	 * @param parDef значение параметра по умолчанию.
	 * @return значение параметра.
	 */
	private static String getValue(String com, String[] arg, String parDef) {
	   
		String par = null;
	    
	    for (int i = 0; i < arg.length; i++) {
	        
	    	if (arg[i].equalsIgnoreCase(com) &&
	            !"-".equals(arg[i + 1].substring(0, 1))) {
	            par = arg[i + 1];
	        } // if
	    } // for
	    
	    if (par == null) { 
	    	par = parDef;
	    } // if

	    return par;
	}
	
	/**
	 * Подпись одного PDF-файла (CryptoPro PDF / PKCS7 DETACHED)
     * и сохранение в файл.
	 * 
	 * @param inFile исходный PDF-файл.
	 * @param destination место сохранения подписанного PDF-файл.
	 * @param alias алиас ключа.
	 * @param password пароль.
	 * @param location адрес.
	 * @param reason причина.
	 * @throws Exception
	 */
	private static void proceedOneFile(File inFile, File destination,
        String alias, char[] password, String location, String reason)
        throws Exception {
		
		String fileName = inFile.getName();
		
		if (inFile.isDirectory()) {
			System.out.println("Skip Directory: " + fileName);
			return;
		} // if

		if (fileName.indexOf(".pdf") != fileName.length() - 4) {
			System.out.println("Skip file: " + fileName);
			return;
		} // if

		System.out.println("Sign file: " + fileName);
		
		String baseOutFileName = destination.getAbsolutePath() + 
			(destination.isDirectory() ? (File.separator + fileName) : "");

		File baseOutFile = new File(baseOutFileName);
		String outFileName = baseOutFileName + ".signed";
		
		File outFile = new File(outFileName);
		System.out.println("Destination file: " + outFile.getCanonicalPath());
		
		// Подписываем.

		signPDF(inFile.getCanonicalPath(), outFile.getCanonicalPath(),
            alias, password, location, reason);
		
		// Удаляем исходный файл (если он совпадает по имени
		// с подписанным) и переименовываем подписанный файл.

		if (inFile.getCanonicalPath().equalsIgnoreCase(
            baseOutFile.getCanonicalPath())) {
			
			if (!inFile.delete() || !outFile.renameTo(baseOutFile)) {
				throw new IOException("Couldn't delete and rename file '"
                    + outFile.getName() + "' to '" + fileName + "'");
			} // if
			
		} // if
		else {
			
			if (!outFile.renameTo(baseOutFile)) {
				throw new IOException("Couldn't rename file '"
                    + outFile.getName() + "' to '" + fileName + "'");
			} // if
			
		} // else
			
	}
	
	/**
	 * Подпись конкретного PDF-файла (CryptoPro PDF / PKCS7 DETACHED)
     * и сохранение в файл.
	 * 
	 * @param fileToSign исходный PDF-файл.
	 * @param signedFile подписанный PDF-файл.
	 * @param alias алиас ключа.
	 * @param password пароль.
	 * @param location адрес.
	 * @param reason причина.
	 * @throws Exception
	 */
	public static void signPDF(String fileToSign, String signedFile, String alias, 
		char[] password, String location, String reason) throws Exception {
		
		KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME, JCP.PROVIDER_NAME);
		keyStore.load(null, null);
		
		PrivateKey key = (PrivateKey)keyStore.getKey(alias, password);
		Certificate[] chain = keyStore.getCertificateChain(alias);

        String keyAlgorithm  = key.getAlgorithm();
        String hashAlgorithm = JCP.GOST_DIGEST_NAME;

        if (keyAlgorithm.equals(JCP.GOST_EL_2012_256_NAME) ||
            keyAlgorithm.equals(JCP.GOST_DH_2012_256_NAME)) {
            hashAlgorithm = JCP.GOST_DIGEST_2012_256_NAME;
        } // if
        else if (
            keyAlgorithm.equals(JCP.GOST_EL_2012_512_NAME) ||
            keyAlgorithm.equals(JCP.GOST_DH_2012_512_NAME)) {
            hashAlgorithm = JCP.GOST_DIGEST_2012_512_NAME;
        } // else

        SignVerifyPDFExample.sign(key, hashAlgorithm, JCP.PROVIDER_NAME,
            chain, fileToSign, signedFile, location, reason, false, false);

	}

}
