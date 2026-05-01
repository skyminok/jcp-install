/**
 * BioConsoleSimulator.java,v $
 * version $
 * created 07.12.2020 13:22 by afevma
 * last modified $ by $
 * <p>
 * Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * </p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package util.simulator.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import util.simulator.BioSimulatorBase;

/**
 * Служебный класс BioConsoleSimulator предназначен для симуляции нажатий в консольном ДСЧ.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class BioConsoleSimulator extends BioSimulatorBase {

    /**
     * Поток вывода, связанный с потоком ввода.
     */
    private final PipedOutputStream pipedIOutputStream;

    /**
     * Сохраненный дефолтный поток ввода.
     */
    private InputStream savedInputStream = null;

    /**
     * Конструктор.
     *
     * @param pipedInputStream Имитация потока ввода.
     */
    public BioConsoleSimulator(PipedInputStream pipedInputStream) {
        super(new BioConsoleNext());
        this.pipedIOutputStream = new PipedOutputStream(); // поток ввода
        try {
            this.pipedIOutputStream.connect(pipedInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Конструктор.
     *
     */
    public BioConsoleSimulator() {
        super(new BioConsoleNext());
        PipedInputStream pipedInputStream = new PipedInputStream();
        savedInputStream = System.in;
        System.setIn(pipedInputStream);
        this.pipedIOutputStream = new PipedOutputStream(); // поток ввода
        try {
            this.pipedIOutputStream.connect(pipedInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Закрытие потоков.
     *
     */
    private synchronized void close() {
        if (savedInputStream != null) {
            System.setIn(savedInputStream);
            savedInputStream = null;
        } // if
        try {
            pipedIOutputStream.close();
        } catch (IOException e) {}
        try {
            pipedIOutputStream.close();
        } catch (IOException e) {}
    }

    @Override
    public void interrupt() {
        try {
            close();
        } finally {
            super.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Создание и передача события.
                final char send = next.next(); // enter
                pipedIOutputStream.write(send);
                Thread.sleep(getPauseTime()); // пауза
            }
        } catch (InterruptedException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    protected long getPauseTime() {
        return 110;
    }

}
