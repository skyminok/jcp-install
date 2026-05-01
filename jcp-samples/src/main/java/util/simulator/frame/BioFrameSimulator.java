/**
 * $RCSfileBioSimulator.java,v $
 * version $Revision: 36379 $
 * created 16.10.2017 11:24 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <p/>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
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

import util.simulator.BioSimulatorBase;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Служебный класс BioFrameSimulator предназначен для симуляции нажатий в оконном ДСЧ.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class BioFrameSimulator extends BioSimulatorBase {

    /**
     * Окно для ввода.
     */
    private final Window window;

    /**
     * Конструктор.
     *
     * @param window Окно для ввода.
     */
    public BioFrameSimulator(Window window) {
        super(new BioFrameNext());
        this.window = window;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Создание и передача события.
                final char send = next.next();
                final int value = Character.getNumericValue(send);
                final KeyEvent pressed = new KeyEvent(window, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, value, send);
                final KeyEvent typed = new KeyEvent(window, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, 0, send);
                final KeyEvent released = new KeyEvent(window, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, value, send);
                window.dispatchEvent(pressed);
                window.dispatchEvent(typed);
                window.dispatchEvent(released);
                Thread.sleep(getPauseTime()); // пауза
            }
        } catch (InterruptedException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected long getPauseTime() {
        return 110;
    }

}
