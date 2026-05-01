package cmsutil;

/**
 * Главный класс
 */
public class CMSMain
{
    /**
     * Класс для обработки параметров командной строки.
     */
    public static class Parameters {
        /** Режим - шифрование или расшифроание*/
        int mode = 0;
        /** Имя хранилища доверенных сертификатов. */
        String certStore = null;
        /** тип хранилища. */
        String certStoreType = null;
        /** Провайдер хранилища. */
        String certStoreProvider = null;
        /** Пароль на хранилище сертификатов/ключевой контейнер */
        String pass = null;
        /** Имя сертификата/ключевого контейнера*/
        String alias = null;
        /** Название входного файла*/
        String inFile = null;
        /** Название выходного файла*/
        String outFile = null;
        /** Тип ключевого контейнера*/
        String keyStore = null;
        /** Провайдер*/
        String provider = null;
        /** Признак ошибки при разборе */
        private boolean failed = true;

        /**
         * Метод, осуществляющий разбор параметров командной строки.
         * @param args
         */
        private void parseParams(String args[]) {
            int i = 0;
            while (i < args.length) {
                if (args[i].equalsIgnoreCase("-encrypt")) {
                    if (mode != 0)
                        return;
                    mode = ENCRYPT;
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-decrypt")) {
                    if (mode != 0)
                        return;
                    mode = DECRYPT;
                    i++;
                }
                else if (args[i].equalsIgnoreCase("-certstore")) {
                    if (certStore != null)
                        return;
                    certStore = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                }else if (args[i].equalsIgnoreCase("-certstoretype")) {
                    if (certStoreType != null)
                        return;
                    certStoreType = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                }else if (args[i].equalsIgnoreCase("-certstoreprovider")) {
                    if (certStoreProvider != null)
                        return;
                    certStoreProvider = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-pass")) {
                    if (pass != null)
                        return;
                    pass = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-alias")) {
                    if (alias != null)
                        return;
                    alias = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-in")) {
                    if (inFile != null)
                        return;
                    inFile = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-out")) {
                    if (outFile != null)
                        return;
                    outFile = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-keystore")) {
                    if (keyStore != null)
                        return;
                    keyStore = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else if (args[i].equalsIgnoreCase("-provider")) {
                    if (provider != null)
                        return;
                    provider = (i + 1) == args.length ? null : args[i + 1];
                    i += 2;
                } else {
                    i++;
                }
            }
            failed = false;
        }

        /**
         * Конструктор
         * @param args
         */
        Parameters(String[] args) {
            parseParams(args);
        }

        /**
         * Проверка правильности параметров.
         * @return true, если и только если параметры корректны.
         */
        boolean check() {
            if (failed)
                return false;
            if (mode == 0)
                return false;
            if (mode == ENCRYPT) {
                return (certStore != null) && (alias != null) && (inFile != null) && (outFile != null);
            } else {
                return (keyStore != null) && (alias != null)  && (inFile != null) && (outFile != null);
            }
        }
    }

    public final static int ENCRYPT = 1;
    public final static int DECRYPT = 2;

    /**
     * Основной метод входа
     * @param args Параметры командной строки
     */
    public static void main(String[] args) throws Exception
    {
        Parameters parameters = new Parameters(args);
        if (!parameters.check()) {
            printHelp();
            return;
        }

        CMSCipher.process(parameters, false);

    }

    /**
     * Основной метод входа
     * @param args Параметры командной строки
     */
    public static void main_(String[] args) throws Exception
    {
        Parameters parameters = new Parameters(args);
        if (!parameters.check()) {
            printHelp();
            return;
        }

        CMSCipher.process(parameters, true);

    }

    /**
     * Печать справки об использовании программы.
     */
    private static void printHelp() {
        System.out.println(
                "[Usage]: \njava -jar cmsutil.jar -encrypt -certstore <certificate storage name> " +
                "[-certstoreprovider <cert store provider>] [-certstoreprovider <cert store provider>]" +
                " [-pass <storage password>] -alias <certificate alias> -in <input file> -out <output file>" +
                " -provider <provider name>\n" +
                "\n" +
                "java -jar cmsutil.jar -decrypt -keystore <key storage type> -alias <container name> " +
                "[-pass <container password>] -in <input file> -out <output file> -provider <provider name>"
        );
    }


}
