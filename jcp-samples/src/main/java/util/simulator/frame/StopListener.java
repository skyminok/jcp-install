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

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class StopListener implements WindowListener {
    /**
     * Нить нажатий.
     */
    private final Timer currentTimer;
    /**
     * Создание слушателя остановки нити.
     *
     * @param timer Останавливаемая нить.
     */
    public StopListener(Timer timer) {
        currentTimer = timer;
    }
    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosing(WindowEvent e) {
        currentTimer.stop();
    }
    @Override
    public void windowClosed(WindowEvent e) {
        currentTimer.stop();
    }
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}
}
