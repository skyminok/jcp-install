/**
 * $RCSfile$
 * version $Revision$
 * created 13.10.2004 16:58:55 by iva
 * last modified $Date$ by $Author$
 *
 * Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package HtmlSign;

import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.tools.ExpandException;
import ru.CryptoPro.JCP.tools.PropertyExpander;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Enumeration;

/**
 * Простой Applet подписи информации.
 * <br>
 * Если у Вас нет ключа, то для генерации ключа подписи по DSA можно
 * воспользоваться командной строкой: <code> keytool -genkey  -alias dsa -dname
 * CN=test,O=CryptPro,C=RU -keypass 123456 -storepass 123456 -storetype JKS
 * -provider sun.security.provider.Sun </code> Для проверки наличия ключа в
 * store при подписи по DSA, можно воспользоваться командной строкой: <code>
 * keytool.exe -list -v -alias dsa -storepass 123456 -storetype JKS -provider
 * sun.security.provider.Sun </code>
 *
 * @author Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public final class HtmlSign extends Applet implements
        ActionListener, ItemListener {
/**
 * Текст для подписи.
 */
private TextArea textToBeSigned;
/**
 * Alias ключа.
 */
private TextField keyAlias;
/**
 * Кнопка выбора alias.
 */
private Button buttonAlias;
/**
 * Пароль на ключ.
 */
private TextField keyPassword;
/**
 * Кнопка подписи.
 */
private Button buttonToSign;
/**
 * Подпись, часть 1.
 */
private Label signatureOne;
/**
 * Подпись, часть 2.
 */
private Label signatureTwo;
/**
 * Имя store.
 */
private Choice storeType;
/**
 * Имя провайдера.
 */
private Choice providerType;
/**
 * Алгоритм подписи.
 */
private Choice signatureType;
/**
 * Имя файла store.
 */
private TextField storeFile;
/**
 * Пароль на store.
 */
private TextField storePassword;
/**
 * Кнопка выбора имени файла со store.
 */
private Button buttonFile;

/**
 * Пустой кончтруктор.
 */
public HtmlSign() {
    mainArgs = new String[0];
}

/**
 * Кончтруктор с доп. параметрами.
 *
 * @param args Параметры командной строки. Задаются в виде &lt;имя параметра&gt;
 * &lt;Значение параметра&gt;
 */
public HtmlSign(String[] args) {
    mainArgs = args;
}

/**
 * Дополнительные параметры, могут быть заданы при старте не через applet.
 */
private final String[] mainArgs;

public String getParameter(String name) {
    String ret = null;
    for (int i = 0; i < mainArgs.length; i += 2) {
        if (name == mainArgs[i] && i + 1 < mainArgs.length)
            ret = mainArgs[i];
    }
    if (ret == null)
        ret = super.getParameter(name);
    return ret;
}

/**
 * Запуск без Applet.
 *
 * @param args параметры командной строки
 */
public static void main(String[] args) {
    final Frame frame = new Frame("HtmlSign");
    final Applet htmlSign = new HtmlSign(args);
    htmlSign.init();
    frame.add("Center", htmlSign);
    frame.pack();
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            htmlSign.stop();
            htmlSign.destroy();
            frame.setVisible(false);
            frame.dispose();
        }
    });
    htmlSign.start();
}

/**
 * Инициализация Applet.
 */
public void init() {
    super.init();
    setLayout(new BorderLayout());
    final Panel north = new Panel(new GridLayout(4, 1));

    final Panel northProvider = new Panel(new GridLayout(1, 4));
    northProvider.add(new Label("Provider:"));
    providerType = new Choice();
    final Provider[] providers = Security.getProviders();
    for (int i = 0; i < providers.length; i++) {
        providerType.add(providers[i].getName());
    }
    final String defProviderType = getParameter("defProviderType");
    if (defProviderType != null)
        providerType.select(defProviderType);
    providerType.addItemListener(this);
    northProvider.add(providerType);
    northProvider.add(new Label("Signature:"));
    signatureType = new Choice();
    fillByPrefix(providerType.getSelectedItem(), STR_SIGNATURE, signatureType);
    final String defSignatureType = getParameter("defSignatureType");
    if (defSignatureType != null)
        signatureType.select(defSignatureType);
    northProvider.add(signatureType);
    north.add(northProvider);

    final Panel northStore = new Panel(new GridLayout(1, 4));
    northStore.add(new Label("Store:"));
    storeType = new Choice();
    for (int i = 0; i < providers.length; i++) {
        fillByPrefix(providers[i].getName(), STR_KEYSTORE, storeType);
    }
    final String defStoreType = getParameter("defStoreType");
    if (defStoreType != null)
        storeType.select(defStoreType);
    northStore.add(storeType);
    northStore.add(new Label("Store keyPassword"));
    storePassword = new TextField();
    storePassword.setEchoChar('*');
    final String defStorePassword = getParameter("defStorePassword");
    if (defStorePassword != null)
        storePassword.setText(defStorePassword);
    northStore.add(storePassword);
    north.add(northStore);

    final Panel northStoreFile = new Panel(new GridLayout(1, 3));
    northStoreFile.add(new Label("Store file:"));
    storeFile = new TextField();
    String defStoreFile = getParameter("defStoreFile");
    if (defStoreFile != null) {
        try {
            defStoreFile = PropertyExpander.expand(defStoreFile);
        } catch (ExpandException e) {
            // ignore exception;
        }
    }
    if (defStoreFile != null)
        storeFile.setText(defStoreFile);
    northStoreFile.add(storeFile);
    buttonFile = new Button("Select");
    buttonFile.addActionListener(this);
    northStoreFile.add(buttonFile, BorderLayout.SOUTH);
    north.add(northStoreFile);

    final Panel northKey = new Panel(new GridLayout(1, 5));
    northKey.add(new Label("Key keyAlias:"));
    keyAlias = new TextField();
    final String defAlias = getParameter("defAlias");
    if (defAlias != null)
        keyAlias.setText(defAlias);
    northKey.add(keyAlias);
    buttonAlias = new Button("Select");
    buttonAlias.addActionListener(this);
    northKey.add(buttonAlias);
    northKey.add(new Label("Password:"));
    keyPassword = new TextField();
    keyPassword.setEchoChar('*');
    final String defKeyPassword = getParameter("defKeyPassword");
    if (defKeyPassword != null)
        keyPassword.setText(defKeyPassword);
    northKey.add(keyPassword);
    north.add(northKey);

    add(north, BorderLayout.NORTH);

    final Panel center = new Panel(new BorderLayout());
    textToBeSigned = new TextArea("Text to be signed");
    final String defTextToBeSigned = getParameter("defTextToBeSigned");
    if (defTextToBeSigned != null)
        textToBeSigned.setText(defTextToBeSigned);
    center.add(textToBeSigned, BorderLayout.CENTER);
    buttonToSign = new Button("Sign textToBeSigned");
    center.add(buttonToSign, BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);

    final Panel sourth = new Panel(new GridLayout(3, 1));
    sourth.add(new Label("Signature:"));
    signatureOne = new Label();
    sourth.add(signatureOne);
    signatureTwo = new Label();
    sourth.add(signatureTwo);
    add(sourth, BorderLayout.SOUTH);
    buttonToSign.addActionListener(this);
}

/**
 * Уничтожение Applet.
 */
public void destroy() {
    remove(textToBeSigned);
    remove(keyAlias);
    remove(keyPassword);
    remove(buttonToSign);
    remove(signatureOne);
    remove(signatureTwo);
    remove(storeType);
    remove(storeFile);
    remove(storePassword);
    remove(buttonFile);
    super.destroy();
}

/**
 * Информация об Applet.
 *
 * @return Название Applet
 */
public String getAppletInfo() {
    return "Simple html sign.";
}

private KeyStore load() throws GeneralSecurityException, IOException {
    final String file = storeFile.getText();
    FileInputStream storeStream = null;
    try {
        if (file != null && file.length() != 0)
            storeStream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
//        showStatus( e.toString() );
    }

    final String type = storeType.getSelectedItem();
    final String provName = providerType.getSelectedItem();
    JCPInit.initProviders(provName.equalsIgnoreCase(
        DefaultProvider.JCSP_PROVIDER_NAME));
    final KeyStore store = KeyStore.getInstance(type);

    final String sPwd = storePassword.getText();
    final char[] sPassword = sPwd.toCharArray();
    store.load(storeStream, sPassword);
    return store;
}

/**
 * Собственно функция подписи.
 *
 * @return подпись
 * @throws GeneralSecurityException Ошибки подписи
 * @throws IOException Ошибки открытия store
 */
private byte[] sign()
        throws GeneralSecurityException, IOException {
    final KeyStore store = load();

    final String pwd = keyPassword.getText();
    final char[] password = pwd.toCharArray();
    final String alias = keyAlias.getText();
    final Key key = store.getKey(alias, password);

    final String provName = providerType.getSelectedItem();
    final Signature signature = Signature.getInstance(
            signatureType.getSelectedItem(),
            provName);
    signature.initSign((PrivateKey) key);
    final String txt = textToBeSigned.getText();
    final byte[] info = txt.getBytes();
    signature.update(info);
    return signature.sign();
}

/**
 * Показ диалога о возникновении exception.
 *
 * @param e exception
 */
private void showExceptionDialog(Exception e) {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(os);
    e.printStackTrace(ps);
    final String msg = os.toString();
    JOptionPane.showMessageDialog(this, msg,
            GeneralSecurityException.class.toString(),
            JOptionPane.ERROR_MESSAGE);
}

/**
 * Таблица перекодировки в 16-ричное представление. Не используем классы из
 * JCP.
 */
private static final char[] hex_digits = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

/**
 * Функция перевода массива байт в строковое 16-ричное представление.
 *
 * @param array массив
 * @return строковое 16-ричное представление
 */
private static String toHexSequence(byte[] array) {
    int i;
    String s = "";
    for (i = 0; i < array.length; i++) {
        s += hex_digits[(array[i] >>> 4) & 0xf];
        s += hex_digits[array[i] & 0xf];
    }
    return s;
}

/**
 * Реакция на нажатие кнопки. Выдача диалогового окна для выбора файла store и
 * подпись текста
 *
 * @param event сообщение
 */
public void actionPerformed(ActionEvent event) {
    final Object source = event.getSource();
    if (source == buttonToSign) {
        try {
            final byte[] res = sign();
            final String sign = toHexSequence(res);
            final int l = sign.length() / 2;
            signatureOne.setText(sign.substring(0, l));
            signatureTwo.setText(sign.substring(l));
            Font dfont = signatureOne.getFont();
            Font f = new Font("Monospaced", Font.PLAIN, dfont.getSize());
            signatureOne.setFont(f);
            signatureTwo.setFont(f);
        } catch (GeneralSecurityException e) {
            showExceptionDialog(e);
        } catch (IOException e) {
            showExceptionDialog(e);
        } catch (SecurityException e) {
            showExceptionDialog(e);
        }
    }
    if (source == buttonFile) {
        try {
            final JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(storeFile.getText()));
            final int retval = chooser.showDialog(this, null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                final File theFile = chooser.getSelectedFile();
                storeFile.setText(theFile.getAbsolutePath());
            }
        } catch (SecurityException e) {
            showExceptionDialog(e);
        }
    }
    if (source == buttonAlias) {
        Enumeration en;
        int size;
        try {
            final KeyStore store = load();
            en = store.aliases();
            size = store.size();
        } catch (SecurityException e) {
            showExceptionDialog(e);
            return;
        } catch (GeneralSecurityException e) {
            showExceptionDialog(e);
            return;
        } catch (IOException e) {
            showExceptionDialog(e);
            return;
        }
        Object[] possibilities = new String[size];
        int i = 0;
        while (en.hasMoreElements())
            possibilities[i++] = en.nextElement();
        String s = (String) JOptionPane.showInputDialog(this,
                "Select avaiable aliase:",
                "Alias Chooser",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                keyAlias.getText());
        if (s != null && s.length() > 0)
            keyAlias.setText(s);
    }
}

/**
 * Префикс для KeyStore в Property Provider.
 */
private static final String STR_KEYSTORE = "KeyStore.";

/**
 * Префикс для алгоритма подписи в Property Provider.
 */
private static final String STR_SIGNATURE = "Signature.";

/**
 * Заполнение Choice из Provider Properties по prefix.
 *
 * @param provider Имя провайдера
 * @param prefix префикс для заполнения
 * @param choice заполняемый choice
 */
private static void fillByPrefix(String provider, String prefix, Choice choice) {
    final Provider p = Security.getProvider(provider);
    final Enumeration en = p.keys();
    final int sl = prefix.length();
    while (en.hasMoreElements()) {
        final String prop = (String) en.nextElement();
        final String start = prop.substring(0, sl);
        if (start.equalsIgnoreCase(prefix) &&
                prop.indexOf(" ") < 0)
            choice.add(prop.substring(sl));
    }
}

/**
 * Реакция на изменение элемента choice. Перечитывание списка алгоритмов.
 *
 * @param event событие
 */
public void itemStateChanged(ItemEvent event) {
    final Object source = event.getSource();
    if (source == providerType) {
        signatureType.removeAll();
        fillByPrefix(providerType.getSelectedItem(), STR_SIGNATURE,
                signatureType);
    }
}
}
