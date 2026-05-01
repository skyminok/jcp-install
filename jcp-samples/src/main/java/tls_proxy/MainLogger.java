/**
 * $RCSfileMainLogger.java,v $
 * version $Revision: 36379 $
 * created 17.08.2016 11:39 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package tls_proxy;

import ru.CryptoPro.JCP.tools.Platform;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс MainLogger предназначен для вывода сообщений в лог.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class MainLogger {

    /**
     * Имя логгера.
     */
    private static final String TAG = "tls_proxy";

    /**
     * Логгер.
     */
    private static final Logger LOGGER = Logger.getLogger(TAG);

    /**
     * Флаг включения логирования.
     */
    private static boolean enableLogging = true;

    /**
     * Задание уровня логирования.
     *
     * @param logLevel Уровень логирования.
     */
    public static synchronized void init(String logLevel) {
        if (Platform.isAndroid) {
            enableLogging = !logLevel.equalsIgnoreCase("OFF");
        }
        else {
            LOGGER.setLevel(Level.parse(logLevel));
        }
    }

    /**
     * Вывод на уровне INFO.
     *
     * @param message Сообщение.
     */
    public static void info(String message) {
        if (Platform.isAndroid) {
            if (enableLogging) {
                System.out.println(TAG + " :: " + message);
            }
        } else {
            LOGGER.log(Level.INFO, message);
        }
    }

    /**
     * Вывод на уровне FINE.
     *
     * @param message Сообщение.
     */
    public static void fine(String message) {
        if (Platform.isAndroid) {
            if (enableLogging) {
                System.out.println(TAG + " :: " + message);
            }
        } else {
            LOGGER.log(Level.FINE, message);
        }
    }

    /**
     * Вывод на уровне WARNING.
     *
     * @param message Сообщение.
     */
    public static void warning(String message) {
        if (Platform.isAndroid) {
            if (enableLogging) {
            System.out.println(TAG + " :: " + message);
            }
        }
        else {
            LOGGER.log(Level.WARNING, message);
        }
    }

    /**
     * Вывод на уровне SEVERE.
     *
     * @param message Сообщение.
     */
    public static void error(String message) {
        if (Platform.isAndroid) {
            if (enableLogging) {
                System.err.println(TAG + " :: " + message);
            }
        }
        else {
            LOGGER.log(Level.SEVERE, message);
        }
    }

    /**
     * Вывод на уровне SEVERE.
     *
     * @param message Сообщение.
     * @param throwable Исключение.
     */
    public static void error(String message, Throwable throwable) {
        if (Platform.isAndroid) {
            if (enableLogging) {
                System.err.println(TAG + " :: " + message);
                throwable.printStackTrace();
            }
        }
        else {
            LOGGER.log(Level.SEVERE, message, throwable);
        }
    }

}
