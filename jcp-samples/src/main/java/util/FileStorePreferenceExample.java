/**
 * FileStorePrefrenceExample.java,v $
 * version $
 * created 17.10.2020 17:35 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package util;

import java.io.File;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Пример использования способа хранения настроек (preferences)
 * в виде файлов в ОС Windows.
 *
 * Как использовать:
 * -Djava.util.prefs.PreferencesFactory=ru.CryptoPro.JCP.pref.file.FileSystemPreferencesFactory \
 * -Djava.util.prefs.systemRoot=path_to_system_root_directory \
 * -Djava.util.prefs.userRoot=path_to_system_user_directory
 *
 * Например:
 * -Djava.util.prefs.PreferencesFactory=ru.CryptoPro.JCP.pref.file.FileSystemPreferencesFactory \
 * -Djava.util.prefs.systemRoot=c:/Projects/prefs/systemRoot \
 * -Djava.util.prefs.userRoot=c:/Projects/prefs/userRoot
 *
 * Задать свойства можно также программно, с помощью System.setProperty().
 *
 * При этом должна существовать папка c:/Projects/prefs/systemRoot/.systemPrefs,
 * иначе такая же (.systemPrefs) будет создана в запущенной JRE/JDK (из-за того,
 * что нет .systemPrefs в заданной папке).
 *
 * Папка c:/Projects/prefs/userRoot может быть пустой, настройки пользователя
 * при этом сохранятся в c:/Projects/prefs/userRoot/.java/.userPrefs
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class FileStorePreferenceExample {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // В примере используется временная папка.

        String tmpDir = System.getProperty("java.io.tmpdir");
        System.out.println("Temporary directory: " + tmpDir);

        // Задаем папки для хранения настроек. Они должны
        // существовать.

        File systemRootDir = new File(tmpDir, "system-root");
        if (!systemRootDir.exists()) {

            if (!systemRootDir.mkdir()) {
                throw new Exception("Cannot create " +
                    systemRootDir.getAbsolutePath());
            } // if

        } // if

        // Папка для system свойств должна содержать
        // папку .systemPrefs.

        File systemPrefsDir = new File(systemRootDir, ".systemPrefs");
        if (!systemPrefsDir.exists()) {

            if (!systemPrefsDir.mkdir()) {
                throw new Exception("Cannot create " +
                    systemPrefsDir.getAbsolutePath());
            } // if

        } // if

        File userRootDir = new File(tmpDir, "user-root");
        if (!userRootDir.exists()) {

            if (!userRootDir.mkdir()) {
                throw new Exception("Cannot create " +
                    userRootDir.getAbsolutePath());
            } // if

        } // if

        // Задаем свойства для переопределения preference
        // factory, чтобы указать место хранения настроек.

        System.setProperty("java.util.prefs.PreferencesFactory",
            "ru.CryptoPro.JCP.pref.file.FileSystemPreferencesFactory");

        System.setProperty("java.util.prefs.systemRoot", systemRootDir.getAbsolutePath());
        System.setProperty("java.util.prefs.userRoot",   userRootDir.getAbsolutePath());

        // Теперь все настройки будут храниться в указанных
        // папках.

        Preferences system = Preferences.systemRoot();
        test(system, "system");

        Preferences user = Preferences.userRoot();
        test(user, "user");

    }

    /**
     * Проверка чтения и сохранения настроек.
     *
     * @param prefs Настройки.
     * @param from Место хранения.
     * @throws BackingStoreException
     */
    private static void test(Preferences prefs, String from)
        throws BackingStoreException {

        final String KEY   = "test_key_from_" + from;
        final String VALUE = "rnd-" + (new Random()).nextInt();

        // Выведем то, что есть.

        String value = prefs.get(KEY, null);
        System.out.println("Current state: " + KEY + " == " + value);

        // Запишем новое значение.

        prefs.put(KEY, VALUE);
        System.out.println("New state: " + KEY + " == " + VALUE);

        prefs.sync();

    }

}
