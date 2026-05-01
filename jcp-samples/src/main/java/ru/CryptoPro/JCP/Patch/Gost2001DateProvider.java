/**
 * $RCSfileGost2001DateProvider.java,v $
 * version $Revision: 36379 $
 * created 24.04.2018 12:00 by afevma
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
package ru.CryptoPro.JCP.Patch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.security.Provider;

import java.util.GregorianCalendar;
import java.util.prefs.Preferences;

/**
 * Класс Gost2001DateProvider реализует провайдер,
 * выполняющий только изменение даты истечения срока
 * действия ГОСТ 2001 в поле
 * {@link ru.CryptoPro.JCP.JCP#gost2001Expires}.
 *
 * Используется факт, что JCP или вызов вида XXX.getInstance()
 * вызывает перечисление провайдеров, следовательно, данного
 * провайдера {@link #Gost2001DateProvider} тоже, если он
 * зарегстрирован в java.security. Провайдер должен находиться
 * на позиции после JCP.
 *
 * При загрузке провайдера Gost2001DateProvider производится
 * поиск загруженного класса {@link #PROVIDER_NAME}. Если
 * такой присутствует, то далее следует поиск в заданном
 * классе поля {@link #PROVIDER_FIELD} - оно содержит дату,
 * которую необходимо подменить.
 *
 * Дата заменяется на {@link #DEFAULT_DATE} либо прочитанную
 * из Preferences из узла {@link #DATE_NODE} и ключа
 * {@link #DATE_KEY}. Дата задается в миллисекундах (from
 * the epoch).
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Gost2001DateProvider extends Provider {

    /**
     * Идентификатор.
     */
    private static final long serialVersionUID = 100100L;

    /**
     * Имя класса.
     */
    private static final String CLASS_NAME =
        Gost2001DateProvider.class.getName();

    /**
     * Искомый класс провайдера, содержащий поле gost2001Expires.
     */
    public static final String PROVIDER_NAME = "ru.CryptoPro.JCP.JCP";

    /**
     * Поле gost2001Expires для модификации.
     */
    public static final String PROVIDER_FIELD = "gost2001Expires";

    /**
     * Узел параметра с датой для установки. Читается из preferences.
     */
    public static final String DATE_NODE = "ru/CryptoPro/JCP/patch";

    /**
     * Параметр с датой для установки. Читается из preferences.
     */
    public static final String DATE_KEY = "date";

    /**
     * Дата по умолчанию: 2019, 6, 31, 11, 59, 59
     * (31 июля 2019г, 11:59:59).
     */
    public static final long DEFAULT_DATE = 1564563599000L;

    /**
     * Конструктор.
     *
     */
    public Gost2001DateProvider() {

        super("Gost2001DateProvider", 1.0,
            "JCP patch for " + PROVIDER_FIELD);

        try {
            patch();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Функция выполняет поиска класса {@link #PROVIDER_NAME}
     * и модификацию поля {@link #PROVIDER_FIELD}.
     *
     * @throws Exception
     */
    public static void patch() throws Exception {

        Class jcpClass;

        try {

            System.out.println(TAG() + "finding class...");
            jcpClass = Class.forName(PROVIDER_NAME);

        } catch (ClassNotFoundException e) {

            ClassLoader threadLoader = Thread.currentThread()
                    .getContextClassLoader();

            System.out.println(TAG() + "finding class (thread)...");
            jcpClass = Class.forName(PROVIDER_NAME, true, threadLoader);

        }

        System.out.println(TAG() + "finding field...");

        Field field = jcpClass.getDeclaredField(PROVIDER_FIELD);
        field.setAccessible(true);

        // 'modifiers' - it is a field of a class called 'Field'.
        // Make it accessible and remove 'final' modifier for
        // our 'CONSTANT' field

        System.out.println(TAG() + "changing modifier...");

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        // it updates a field, but it was already inlined
        // during compilation...

        System.out.println(TAG() + "reading references...");

        Preferences root = Preferences.systemRoot();
        Preferences node = root.node(DATE_NODE);

        long date = DEFAULT_DATE;
        if (node != null) {

            String dateValue = node.get(DATE_KEY, "");
            if (!dateValue.isEmpty()) {

                try {
                    date = Long.parseLong(dateValue);
                } catch (NumberFormatException e) {

                    System.err.println(TAG() + "invalid value: " + dateValue);
                    e.printStackTrace();

                }

            } // if

        } // if

        System.out.println(TAG() + "changing date to " + date + "L...");

        GregorianCalendar dateShift = new GregorianCalendar();
        dateShift.setTimeInMillis(date);

        field.set(null, dateShift);
        System.out.println(TAG() + "completed.");

    }

    /**
     * Добавление тега лога.
     *
     * @return тег лога.
     */
    private static String TAG() {

        return "[" + Thread.currentThread().getName() +
            "] :: " + CLASS_NAME + " :: ";

    }

    /**
     * Командная строка.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        patch();
    }

}
