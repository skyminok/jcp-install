//
// Программный код, содержащийся в этом файле, предназначен
// исключительно для целей обучения и не может быть использован
// для защиты информации.
//
// Компания Крипто-Про не несет никакой
// ответственности за функционирование этого кода.
//
//------------------------------------------------------------------------------
// В данном script-примере осуществляется подпись и проверка подписи
// содержимого файла при помощи инструментария CAPICOM.
//------------------------------------------------------------------------------

//Особенности работы с подписью.
//При использовании данного скрипта (или аналогичного на VBS из примеров к CSP)
//для создания отделенной подписи следует помнить,
//что скрипт при чтении данных кодирует их в UTF-16LE кодировку.
//Поэтому для проверки этой подписи из java (например, CMS.CMSVerify...)
//следует данные (content) закодировать в UTF-16LE.
//Соответственно для проверки отделенной подписи сгенерированной в java
//с помощью скрипта необходимо, чтобы подпись была на закодированные в
//UTF-16LE кодировку данные.


var ForReading = 1, ForWriting = 2;

// Команды.
var Unknown = 0;
var Sign = 1;
var CoSign = 2;
var Verify = 3;

// CAPICOM константы.
var CAPICOM_CURRENT_USER_STORE = 2;

var CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY = 6;
var CAPICOM_CERTIFICATE_FIND_TIME_VALID = 9;

var CAPICOM_VERIFY_SIGNATURE_ONLY = 0;

var CAPICOM_CERTIFICATE_INCLUDE_CHAIN_EXCEPT_ROOT = 0;

var CAPICOM_PROPID_KEY_PROV_INFO = 2;

// Аргументы командной строки.
var Command = Unknown;
var StoreLocation = null;
var VerifyFlag = CAPICOM_VERIFY_SIGNATURE_ONLY;
var IncludeOption = CAPICOM_CERTIFICATE_INCLUDE_CHAIN_EXCEPT_ROOT;
var bDetached = true;

//отделенная подпись

// Фильтр.
var FileNames;

// В первую очередь осуществляется проверка того, что script выполняется при помощи CScript.exe.
if (WScript.FullName.toLowerCase().indexOf("cscript.exe") == -1) {
    var ss = "This script can only be executed by CScript.exe.\n" +
             "You can either:\n" +
             "1. Set CScript.exe as the default (Run CScript //h:cscript), or\n" +
             "2. Run CScript.exe directly as in, CScript " +
             WScript.ScriptName + ".";
    WScript.Echo(ss);
    WScript.Quit(-1);
}

// Разбор командной сроки.
ParseCommandLine();

// Поиск сертификата, на котором будет осуществляться операция подписи.
var Signer = new ActiveXObject("CAPICOM.Signer");

// Определение свойств операции подписи.
if (Command == Sign || Command == CoSign) {
    var iIndex;
    var Store;
    var Certificates;
    var StoreName = "MY";

    // Открытие соответствующего хранилища.
    Store = new ActiveXObject("CAPICOM.Store");

    StoreLocation = CAPICOM_CURRENT_USER_STORE;

    Store.Open(StoreLocation, StoreName);

    // В качестве кандитатов на сертификат, на котором будет осуществлена подпись,
    // изначально определяются все сертификаты хранилища.
    Certificates = Store.Certificates;

    // Из них не рассматриваются сертификаты, в которых отсутствует закрытый ключ.
    if (Certificates.Count > 0) {
        Certificates =
        Certificates.Find(CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY, CAPICOM_PROPID_KEY_PROV_INFO);
    }

    // Из них выбираются только сертификаты, действительные в настоящее время.
    if (Certificates.Count > 0) {
        Certificates = Certificates.Find(CAPICOM_CERTIFICATE_FIND_TIME_VALID);
    }

    // Выбор сертификата для подписи. Если после проведенной фильтрации осталось несколько кандидатов,
    // то пользователю предоставляется выбор одного из них (при помощи диалогового окна).
    if (Certificates.Count == 0) {
        WScript.Echo("Error: No signing certificate can be found.");
        WScript.Quit(1);
    } else if (Certificates.Count == 1) {
        Signer.Certificate = Certificates(1);
    } else {
        Certificates =
        Certificates.Select("CSignData.js", "Please select a certificate to sign " +
                                            FileNames[0] + ".");
        if (Certificates.Count == 0) {
            WScript.Echo("Error: Certificate selection dialog was canceled.");
            WScript.Quit(2);
        }
        Signer.Certificate = Certificates(1);
    }

    Certificates = null;
    Store = null;

    Signer.Options = IncludeOption;

}

// Выполнение требуемой операции.
if (Command == Sign) {
    DoSignCommand(FileNames, bDetached, Signer);
} else if (Command == CoSign) {
    DoCoSignCommand(FileNames, bDetached, Signer);
} else {
    DoVerifyCommand(FileNames, bDetached, VerifyFlag);
}
// Освобождение ресурсов.
Signer = null;

WScript.Quit(0);

// Конец main


//------------------------------------------------------------------------------
// Функиция DoSignCommand  подписывает содержимое файла FileNames[0]
// и записывает подписанные данные в FileNames[1].
//------------------------------------------------------------------------------

function DoSignCommand(FileNames, bDetached, Signer) {
    var Content;
    var Message;
    var SignedData;

    // Создание объекта SignedData.
    SignedData = new ActiveXObject("CAPICOM.SignedData");

    // Распечатка информативного сообщения.
    WScript.Echo("Signing text file " + FileNames[0] + ".");
    WScript.Echo();

    // Загрузка содержимого файла для подписи.
    Content = LoadFile(FileNames[0]);

    // Подпись.
    SignedData.Content = Content;
    Message = SignedData.Sign(Signer, bDetached);

    // Сохранение подписанных данных в FileNames[1].
    SaveFile(FileNames[1], Message);
    WScript.Echo("Successful - Signed message saved to " + FileNames[1] + ".");

    // Освобождение ресурсов.
    SignedData = null;

}
//Конец DoSignCommand


//------------------------------------------------------------------------------
// Функция DoCoSignCommand подписывает текстовый файл FileNames[0]и сохраняет
// заново подписанное содержимое в FileNames[1].
//------------------------------------------------------------------------------

function DoCoSignCommand(FileNames, bDetached, Signer) {
    var Content;
    var Message;
    var SignedData;

    // Создание объекта SignedData.
    SignedData = new ActiveXObject("CAPICOM.SignedData");

    // Распечатка информативного сообщения.
    WScript.Echo("CoSigning text file " + FileNames[0] + ".");
    WScript.Echo();


    // Загрузка подписанных данных.
    Message = LoadFile(FileNames[0]);

    // Обработка отделенных данных
    if (bDetached) {
        Content = LoadFile(FileNames[1]);
        SignedData.Content = Content;
        WScript.Echo("content file " + FileNames[1] + ".");
    }

    // Проверка подписанных данных.
    SignedData.Verify(Message, bDetached, VerifyFlag);

    // Повторная подпись.
    Message = SignedData.CoSign(Signer, bDetached);

    // Сохранение вновь подписанных данных в FileNames[0].
    SaveFile(FileNames[0], Message);
    WScript.Echo("Successful - CoSigned message saved to " + FileNames[0] +
                 ".");

    // Освобождение ресурсов.
    SignedData = null;

}
// Конец DoCoSignCommand


//------------------------------------------------------------------------------
// Функция DoVerifyCommand проверяется подписанный текстовый файл.
//------------------------------------------------------------------------------

function DoVerifyCommand(FileNames, bDetached, VerifyFlag) {
    var Content;
    var Message;
    var SignedData;

    // Создание объекта SignedData.
    SignedData = new ActiveXObject("CAPICOM.SignedData");

    // Распечатка информативного сообщения.
    WScript.Echo("Verifying signed text file " + FileNames[0] +
                 ", please wait...");
    WScript.Echo();

    // Загрузка подписанных данных для проверки.
    Message = LoadFile(FileNames[0]);

    // Обработка отделенных данных
    if (bDetached) {
        Content = LoadFile(FileNames[1]);
        SignedData.Content = Content;
    }

    // Проверка подписи.
    SignedData.Verify(Message, bDetached, VerifyFlag);


    // Сохранение проверенного содержимого в FileNames[1].
    if (!bDetached) {
        SaveFile(FileNames[1], SignedData.Content);
        WScript.Echo("Successful - Verified content saved to " + FileNames[1] +
                     ".");
    } else {
        WScript.Echo("Successful.");
    }

    // Освобождение ресурсов.
    SignedData = null;

}
// Конец DoVerifyCommand

//------------------------------------------------------------------------------
//Функция LoadFile читает содержимое текстового файла.
//------------------------------------------------------------------------------

function LoadFile(FileName) {
    var fso;
    fso = new ActiveXObject("Scripting.FileSystemObject");

    if (!fso.FileExists(FileName)) {
        WScript.Echo("Error: File " + FileName + " not found.");
        WScript.Quit(-5);
    }

    var ts;
    ts = fso.OpenTextFile(FileName, ForReading);
    Buffer = ts.ReadAll();
    return Buffer;

}
// Конец LoadFile


//------------------------------------------------------------------------------
// Функция SaveFile сохраняет строку в файл.
//------------------------------------------------------------------------------

function SaveFile(FileName, Buffer) {
    var fso;
    fso = new ActiveXObject("Scripting.FileSystemObject");

    var ts;
    ts = fso.OpenTextFile(FileName, ForWriting, true);
    ts.Write(Buffer);

}
// Конец SaveFile


//------------------------------------------------------------------------------
// Функция ParseCommandLine разбирает командную строку и устанавливает
// опции согласно ей.
//------------------------------------------------------------------------------

function ParseCommandLine() {

    // Константы для разбора состояний, задаваемых командной строкой.
    var ARG_STATE_COMMAND = 0;
    var ARG_STATE_OPTIONS = 1;
    var ARG_STATE_FILENAME = 13;

    // Разбор командной строки.
    var Arg;
    var ArgState = ARG_STATE_COMMAND;
    var tmp = new Array(2);

    for (var i = 0; i < WScript.Arguments.length; i++) {
        Arg = WScript.Arguments(i);
        if (ArgState == ARG_STATE_COMMAND) {
            if (Arg.toLowerCase() == "sign") {
                Command = Sign;
            } else if (Arg.toLowerCase() == "cosign") {
                Command = CoSign;
            } else if (Arg.toLowerCase() == "verify") {
                Command = Verify;
            } else {
                DisplayUsage();
            }
            ArgState = ARG_STATE_OPTIONS;
        } else if (ArgState == ARG_STATE_OPTIONS) {
            if (Arg.toLowerCase() == "-?" || Arg.toLowerCase() == "/?") {
                DisplayUsage();
            } else {
                if (Arg.substring(0, 1) == "-" || Arg.substring(0, 1) == "/") {
                    DisplayUsage();
                } else {
                    FileNames = new Array(1);
                    FileNames[0] = Arg;
                    tmp[0] = Arg;
                }
                ArgState = ARG_STATE_FILENAME;
            }
        } else if (ArgState == ARG_STATE_FILENAME) {
            if (Arg.substring(0, 1) == "-" || Arg.substring(0, 1) == "/") {
                DisplayUsage();
            } else {
                FileNames = new Array(2);
                FileNames[0] = tmp[0];
            }
            FileNames[1] = Arg;
        } else {
            WScript.Echo("Internal script error: Unknown argument state (" +
                         CStr(ArgState) + ") encountered.");
            WScript.Quit(-3);
        }
    }

    // Проверка правильности состояния.
    if (ArgState != ARG_STATE_FILENAME) {
        DisplayUsage();
    }

    // Проверка опций.
    if (Command == Sign || Command == Verify) {
        // Функция подписи и проверки подписи должна иметь входной и выходной (или файл с данными) файлы.
        if (FileNames.length != 2) {
            DisplayUsage();
        }
    }
}
// Конец ParseCommandLine

//------------------------------------------------------------------------------
// Функция DisplayUsage распечатывает информацию об использовании данного
// примера, и затем осуществляет выход с ошибкой.
//------------------------------------------------------------------------------

function DisplayUsage() {
    var ss;
    if (Command == Unknown) {
        ss = "Usage: CSignData Command File1 [File2]\n\n" +
             "Command:\n" +
             "  Sign   \t -- Sign a text file\n" +
             "  CoSign \t -- CoSign a signed text file\n" +
             "  Verify \t -- Verify a signed text file\n\n" +
             "For help on a specific command, enter \"CSignData Command -?\"";
        WScript.echo(ss);
    } else if (Command == Sign) {
        ss = "Usage: CSignData Sign ContentFile SignedFile\n" +
             "The Sign command is used to sign a text file. Signing protects a file from\n" +
             "tampering, and allows user to verify the signer based on signing certificate.\n" +
             "For non-detached signing, both the content and signature will be saved to\n" +
             "SignedFile. For detached signing, only the signature is saved to SignedFile.\n\n" +
             "Parameters:\n" +
             "  -?          \t -- This help screen\n" +
             "  ContentFile \t -- Text file to be signed\n" +
             "  SignedFile  \t -- Signed file (contains signature only if (detached)\n\n" +
             "Note: All non-fatal invalid options for this specific command will be ignored,\n" +
             "      and the ** symbol indicates option can be listed multiple times.\n" +
             "      if (there is only one certificate found in the MY store or PFX that\n" +
             "      matches the requirement, that particular certificate will be used.\n" +
             "      However, if (there is more than one certificate matching the requirement,\n" +
             "      a dialog will be displayed to allow selection of the signing certificate.";
        WScript.echo(ss);
    } else if (Command == CoSign) {
        ss = "Usage: CSignData CoSign SignedFile [ContentFile]\n" +
             "The CoSign command is used to cosign a signed text file. CoSigning provides the\n" +
             "same type of benefits as signing, with an additional signature.\n" +
             "For non-detached cosigning, both the content and signatures will be saved to\n" +
             "SignedFile. For detached cosigning, only the signatures are saved to\n" +
             "SignedFile.\n\n" +
             "Parameters:\n" +
             "  -?          \t -- This help screen\n" +
             "  SignedFile  \t -- Signed file (contains signature only if (detached)\n" +
             "  ContentFile \t -- Text file (required if (detached)\n\n" +
             "Note: All non-fatal invalid options for this specific command will be ignored,\n" +
             "      and the ** symbol indicates option can be listed multiple times.\n" +
             "      if (there is only one certificate found in the MY store or PFX that\n" +
             "      matches the requirement, that particular certificate will be used.\n" +
             "      However, if (there is more than one certificate matching the requirement,\n" +
             "      a dialog will be displayed to allow selection of the signing certificate.";
        WScript.echo(ss);
    } else if (Command == Verify) {
        ss = "Usage: CSignData Verify SignedFile ContentFile\n" +
             "The Verify command is used to verify signed text file. Verification checks\n" +
             "integrity of the signed file and determines if (the signing certificate is\n" +
             "valid and issued by a trusted party.\n" +
             "For non-detached signed file, the content will be extracted and saved to\n" +
             "ContentFile. For detached signed file, the ContentFile is not modified.\n\n" +
             "Parameters:\n" +
             "  -?                     -- This help screen\n" +
             "  SignedFile             -- Signed file (contains signature only if (detached)\n" +
             "  ContentFile            -- Text file (will not be over written if (detached)\n\n" +
             "Note: All non-fatal invalid options for this specific command will be ignored.";
        WScript.echo(ss);
    } else {
        WScript.echo("Internal script error: Unknown help state (Command = " +
                     CStr(Command) + ").");
        WScript.Quit(-2);
    }
    WScript.Quit(-1);

}
// Конец DisplayUsage
// Конец примера
