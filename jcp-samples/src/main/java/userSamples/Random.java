/**
 * $RCSfile$
 * version $Revision$
 * created 14.04.2005 16:59:29 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2009.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.SecureRandom;

/**
 * Пример использования генератора случайных чисел.
 */
public class Random {
/**
 * длина вектора
 */
private static final int RND_LENGTH = 8;

/**
 * @param args null
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    main_(args);
}

public static void main_(String[] args) throws Exception {
    final byte[] randomBytes = new byte[RND_LENGTH];
    final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG);
    random.nextBytes(randomBytes);
    System.out.println("Random bytes are:");
    System.out.println(Constants.toHexString(randomBytes));
    System.out.println("OK");
}

}
