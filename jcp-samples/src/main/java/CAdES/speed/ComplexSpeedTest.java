/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CAdES.speed;

import CAdES.configuration.container.Container2012_256;
import CAdES.configuration.container.ISignatureContainer;
import CAdES.speed.OperationManager.OperationType;

import ru.CryptoPro.AdES.AdESConfig;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import util.Tools;

import java.util.Calendar;
import java.util.Vector;

/**
 * Пример для проверки производительности различных операций с подписью CAdES:
 * создание, проверка, усовершенствование. Можно создать n потоков с выполнением
 * x идентичных операций.
 *
 * 26/04/2012
 * 
 */
public class ComplexSpeedTest {

    /**
     * Время ожидания выполнения всех потоков, msec.
     */
    private static final int THREADS_TIMEOUT = 10 * 60 * 1000;

	/**
	 * Класс потока для выполнения определенной операции.
	 * 
	 */
	class TestThread extends Thread {

		/**
		 * Количество итераций.
		 */
		private int iterationCount = 100;
        /**
         * Имя провайдера для хеширования, подписи и проверки
         * подписи.
         */
        private String providerName = null;
        /**
         * Описание ключевого контейнера.
         */
        private ISignatureContainer signatureContainer = null;
		/**
		 * Тип выполняемой операции.
		 */
		private OperationType operationType = OperationType.otSignCadesBes;
		/**
		 * Время выполнения всех операций в потоке, мс.
		 */
		private long executionTime = 0;

		/**
		 * Конструктор.
		 *
         * @param provider Имя провайдера для хеширования, подписи и проверки
         * подписи.
         * @param container Описание ключевого контейнера.
		 * @param count Количество итераций в потоке.
		 */
		public TestThread(int count, String provider, ISignatureContainer
            container, OperationType otype) {

            providerName = provider;
			iterationCount = count;
			operationType = otype;
            signatureContainer = container;

		}

		/**
		 * Поточная функция, выполняющая нужную операцию заданое количество раз.
		 * 
		 */
		@Override
		public void run() {

			byte[] data = null;

			// Если собираемся проверять или усовершенствовать, то создадим одну
			// подпись подходящего типа, которую потом будем использовать.
			if (operationType == OperationType.otVerifyCadesBes
				|| operationType == OperationType.otVerifyCadesXLongType1
				|| operationType == OperationType.otEnhanceCadesBes) {

				OperationManager dataManager = null;

				switch (operationType) {

				    case otVerifyCadesBes:
				    case otEnhanceCadesBes: {
					    dataManager = new OperationManager(providerName,
						    signatureContainer, OperationType.otSignCadesBes);
					    break;
				    }

				    case otVerifyCadesXLongType1: {
					    dataManager = new OperationManager(providerName,
				            signatureContainer, OperationType.otSignCadesXLongType1);
					    break;
				    }

				} // switch

                if (dataManager != null) {
				    data = dataManager.execute(null);
                } // if
                else {
                    return;
                } // else

			} // if

			OperationManager operationManager = new OperationManager(
                providerName, signatureContainer, operationType);

			// Замеряем время.
			long startTime = Calendar.getInstance().getTime().getTime();

			for (int i = 0; i < iterationCount; ++i) {
				operationManager.execute(data);
			}

			executionTime = Calendar.getInstance().getTime().getTime()
				- startTime;
		}

		/**
		 * Получение времени выполнения задания.
		 * 
		 * @return время в миллисекундах.
		 */
		public long getExecutionTime() {
			return executionTime;
		}
	}

	/**
	 * Запуск теста для проверки производительности.
	 *
     * @param provider Имя провайдера для хеширования, подписи и проверки
     * подписи.
     * @param container Описание ключевого контейнера.
	 * @param otype Тип операции.
	 * @param tCount Количество потоков.
	 * @param iCount Количество итераций в потоке.
	 */
	private void runTest(String provider, ISignatureContainer container,
        OperationType otype, int tCount, int iCount) {

		Vector<TestThread> threads = new Vector<TestThread>();

		if (iCount <= 0) {
			iCount = 1;
		}

		if (tCount <= 0) {
			tCount = 1;
		}

		try {

			// Создаем потоки.
			for (int i = 0; i < tCount; ++i) {
				threads.add(new TestThread(iCount, provider, container, otype));
			} // for

			// Запускаем потоки.
			for (int i = 0; i < tCount; ++i) {
				threads.get(i).start();
			} // for

			// Ждем потоки не более 10 минут.
			for (int i = 0; i < tCount; ++i) {
				threads.get(i).join(THREADS_TIMEOUT);
			} // for

			long totalTime = 0;

			// Убиваем потоки, если еще живые.
			for (int i = 0; i < tCount; ++i) {

				if (threads.get(i).isAlive()) {
					threads.get(i).stop();
				}

				long threadTime = threads.get(i).getExecutionTime();

				// Среднее время и скорость по одному потоку.
				System.out.println("---------- Thread # " + (i + 1) + " ----------");
				Tools.printInfo("Average speed of execution: ", (double) (iCount * 1000) / threadTime, "op/s");
                Tools.printInfo("Average time of an operation: ", (double) threadTime / (iCount * 1000), "s");

				totalTime += threadTime;
			} // for

			// Среднее время и скорость по всем потокам.
			System.out.println("-------------------------------------");
			printTestInfo(otype, tCount, iCount);
            Tools.printInfo("Average speed of execution: ", (double) (tCount * iCount * 1000) / totalTime, "op/s");
            Tools.printInfo("Average time of an operation: ", (double) totalTime / (tCount * iCount), "ms");
            Tools.printInfo("Total speed of execution: ", (double) (tCount * iCount * 1000) / (totalTime / tCount), "op/s");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Вывод сводной информации о производительности.
	 * 
	 * @param otype Тип операции.
	 * @param tCount Количество потоков.
	 * @param iCount Количество итераций.
	 */
	private void printTestInfo(OperationType otype, int tCount, int iCount) {
		
		System.out.print("Test: ");
		
		switch (otype) {
			
			case otSignCadesBes:
				System.out.println("Sign CADES_BES");
				break;
		
			case otSignCadesXLongType1:
				System.out.println("Sign CADES_X_LONG_TYPE_1");
				break;
		
			case otVerifyCadesBes:
				System.out.println("Verify CADES_BES");
				break;
		
			case otVerifyCadesXLongType1:
				System.out.println("Verify CADES_X_LONG_TYPE_1");
				break;
				
			case otEnhanceCadesBes:
				System.out.println("Enhance CADES_BES to CADES_X_LONG_TYPE_1");
				break;
		}
		
		System.out.println("Number of threads: " + tCount);
		System.out.println("Number of iterations: " + iCount);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        final String defaultProvider = JCP.PROVIDER_NAME;
		JCPInit.initProviders(false);

        // На случай, если нужно будет задать не JCP.
        System.setProperty(AdESConfig.DEFAULT_PROVIDER, defaultProvider);

        // Пример запуска 5 потоков для 1000 операций создания подписей
        // CAdES-BES. Можно проверить, используя провайдер JCP либо Java CSP.

		ComplexSpeedTest speedTest = new ComplexSpeedTest();

		speedTest.runTest(defaultProvider, new Container2012_256(),
            OperationType.otSignCadesBes, 5, 1000);

	}

    static {

        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

    }

}
