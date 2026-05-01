/**
 * BioNext.java,v $
 * version $
 * created 07.12.2020 13:24 by afevma
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
package util.simulator.frame;

import util.simulator.Next;

/**
 * Класс BioNext предназначен для выдачи очередного символа.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class BioFrameNext implements Next {

    /**
     * Символ '7'.
     */
    private static final char symbol_1 = '7';

    /**
     * Символ '8'.
     */
    private static final char symbol_2 = '8';

    /**
     * Флаг очередности.
     */
    private boolean firstChar = true;

    @Override
    public char next() {
        firstChar = !firstChar;
        return firstChar ? symbol_1 : symbol_2;
    }

}
