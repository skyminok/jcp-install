/**
 * $RCSfile$
 * version $Revision$
 * created 13.02.2024 13:30:23 by afevma
 * last modified $Date$ by $Author$
 * <p/>
 * Copyright 2004-2024 Crypto-Pro. All rights reserved.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package util.simulator;

import util.simulator.console.BioConsoleSimulator;
import util.simulator.frame.BioMonitor;

import java.awt.*;

public class BioDetector {
    public static BioSimulator createBioSimulator() {
        if (GraphicsEnvironment.isHeadless()) { // без GUI
            return new BioConsoleSimulator();
        } // if
        else {
            return new BioMonitor();
        } // else
    }
}
