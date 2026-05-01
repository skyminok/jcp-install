/**
 * $RCSfile$
 * version $Revision$
 * created 08.09.2005 13:30:23 by cav
 * last modified $Date$ by $Author$
 * <p/>
 * Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package util.simulator.frame;

import util.simulator.Next;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Объект проталкивания клавиатурных событий.
 *
 */
public class PushTimerListener implements ActionListener {

    /**
     * Массив слушателей.
     */
    private final KeyListener[] keyListeners;

    /**
     * Конструктор объекта нажатий.
     *
     * @param next Интерфейс получения следующего символа.
     * @param listeners Массив слушателей.
     */
    public PushTimerListener(final Next next, KeyListener[] listeners) {
        getter = next;
        keyListeners = listeners;
    }

    /**
     * Получение следующего символа для автоввода.
     */
    private final Next getter;

    /**
     * Панель от имени которой происходят клавиатурные события.
     */
    private static final Component sourcePanel = new JPanel();

    /**
     * Отсылка очередной клавиши.
     *
     * @throws InterruptedException при рассинхронизации ввода
     */
    private void process() throws InterruptedException {
        final char send = getter.next();
        final int val = Character.getNumericValue(send);
        final KeyEvent press = new KeyEvent(sourcePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, val, send);
        final KeyEvent typed = new KeyEvent(sourcePanel, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, 0, send);
        final KeyEvent release = new KeyEvent(sourcePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, val, send);
        for (KeyListener keyListener : keyListeners) {
            keyListener.keyPressed(press);
            keyListener.keyTyped(typed);
            keyListener.keyReleased(release);
        }
    }

    /**
     * Обработчик таймерного события.
     *
     * @param e Таймерное событие.
     */
    public void actionPerformed(ActionEvent e) {
        try {
            process();
        } catch (InterruptedException ee) {
            // ignore error.
        }
    }

}
