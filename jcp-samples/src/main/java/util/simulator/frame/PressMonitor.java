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

import util.simulator.BioSimulator;
import util.simulator.Next;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

/**
 * Класс передачи сообщений клавиш в заданной именем нити окно.
 *
 * @author Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class PressMonitor implements AWTEventListener, BioSimulator {

    /**
     * Получение следующего символа для автоввода.
     */
    private final Next getter;

    /**
     * Такт ввода.
     */
    private final int pushTick;

    /**
     * Имя фрейм для которого происходит toFront.
     */
    private final String frameName;

    /**
     * Конструктор монитора.
     *
     * @param pushTime время ожидания между нажатиями
     * @param getNext интерфейс получения следующего символа
     * @param frame Имя окна для которого происходит обработка
     */
    public PressMonitor(int pushTime, Next getNext, String frame) {
        pushTick = pushTime;
        getter = getNext;
        frameName = frame;
    }

    /**
     * Запуск монитора ввода.
     */
    @Override
    public void init() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Останов монитора.
     */
    @Override
    public void release() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    /**
     * Invoked when an event is dispatched in the AWT.
     *
     * @param event current event
     */
    @Override
    public void eventDispatched(AWTEvent event) {
        final Object source = event.getSource();
        final boolean open = event.getID() == WindowEvent.WINDOW_OPENED;
        if (source instanceof JFrame && open) {
            final JFrame frame = (JFrame) source;
            if (frameName.equals(frame.getName())) {
                final KeyListener[] keyListeners = frame.getKeyListeners();
                final PushTimerListener th = new PushTimerListener(getter, keyListeners);
                final Timer timer = new Timer(pushTick, th);
                frame.addWindowListener(new StopListener(timer));
                timer.start();
            }
        }
    }

}
