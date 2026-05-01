/**
 * $RCSfile$
 * version $Revision$
 * created 08.09.2005 17:02:26 by cav
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

import ru.CryptoPro.JCP.Random.BioRandomFrame;

/**
 * Класс автоввода символов в BIO ДСЧ.
 *
 * @author Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class BioMonitor extends PressMonitor {

    /**
     * Конструктор монитора.
     *
     */
    public BioMonitor() {
    super(110, new BioFrameNext(), BioRandomFrame.STR_FRAME_NAME);
}

}
