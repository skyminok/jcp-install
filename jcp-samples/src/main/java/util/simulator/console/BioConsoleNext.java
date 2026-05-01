/**
 * BioConsoleNext.java,v $
 * version $
 * created 07.12.2020 14:53 by afevma
 * last modified $ by $
 * <p/>
 * Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package util.simulator.console;

import java.awt.event.KeyEvent;

import util.simulator.Next;

/**
 * Интерфейс BioConsoleNext предназначен для получения символа ENTER.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class BioConsoleNext implements Next {
    @Override
    public char next() {
        return KeyEvent.VK_ENTER;
    }
}
