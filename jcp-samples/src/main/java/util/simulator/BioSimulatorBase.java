/**
 * BioSimulatorBase.java,v $
 * version $
 * created 07.12.2020 14:11 by afevma
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
package util.simulator;

/**
 * Общий класс симулятора ДСЧ.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public abstract class BioSimulatorBase extends Thread implements BioSimulator {

    /**
     * Выдача символа.
     */
    protected final Next next;

    /**
     * Конструктор.
     *
     * @param next Генератор символов.
     */
    protected BioSimulatorBase(Next next) {
        this.next = next;
    }

    /**
     * Получение таймаута для паузы между передачами символов.
     *
     * @return пауза в мс.
     */
    protected abstract long getPauseTime();

    @Override
    public void init() {
        start();
    }

    @Override
    public void release() {
        // JCP-2496, JCP-2497: в новых версиях Thread.stop() перестал поддерживаться.
        interrupt();
    }

}
