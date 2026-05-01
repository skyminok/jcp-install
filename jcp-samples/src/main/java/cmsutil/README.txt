*** Использование утилиты cmsutil ***

Данная утилита осуществляет работу с CMS-enveloped контейнерами.
Контейнер формируется в соответствии с RFC 5652 и RFC 4490.

java -jar cmsutil.jar -encrypt -certstore <Название хранилища доверенных сертификатов> [-pass <Пароль от хранилища] -alias <Имя сертификата> -in <Файл с данными для шифрования> -out <Файл выхода>

java -jar cmsutil.jar -decrypt -keystore <Тип хранилища> -alias <Имя контейнера> [-pass <Пароль контейнера>] -in <Файл с шифрованными данными > -out <Файл выхода>

*** Тестирование утилиты cmsutil с csptest ***

1) С помощью утилиты cryptcp.exe создать контейнеры с ключами ВКО ГОСТ Р 34.10-2001, получить сертификаты открытых ключей.

cryptcp.exe -creatcert -provtype 75 -provname "Crypto-Pro GOST R 34.10-2001 Cryptographic Service Provider"  -silent -rdn "E=jcms1@test.ru, CN=jcms1" -cont "\\.\HDIMAGE\jcms1" -certusage 1.3.6.1.5.5.7.3.2 -ku -du -ex -ca <CA URL> -keysize 512 -hashalg 1.2.643.2.2.9

cryptcp.exe -creatcert -provtype 75 -provname "Crypto-Pro GOST R 34.10-2001 Cryptographic Service Provider"  -silent -rdn "E=jcms2@test.ru, CN=jcms2" -cont "\\.\HDIMAGE\jcms2" -certusage 1.3.6.1.5.5.7.3.2 -ku -du -ex -ca <CA URL> -keysize 512 -hashalg 1.2.643.2.2.9

2) С помощью контрольной панели КриптоПро CSP установить сертификат из контейнеров в хранилище доверенных сертификатов Windows. (Сервис->просмотреть сертификаты в контейнере). Сертификат jcms2 экспортировать в файл (Свойства->Состав->Копировать в файл).

3) Скопировать контейнер jcms2 в хранилище HDImageStore криптопровайдера JCP. По умолчанию оно находится в C:\Users\<user>\AppData\Local\Crypto Pro

4) Открыть контрольную панель JCP:
ControlPane.bat <path_to_jre>
В ней на вкладке Keys and certificate stores установить сертификат jcms2 в хранилище доверенных сертификатов JCP. Если хранилища нет, его необходимо заранее создать (далее, предполагаем что его имя JavaCertStore).

5) Проверка корректности разбора CMS утилитой.
- Создаём с помощью утилиты csptest.exe сообщение:
csptest.exe -lowenc -encrypt -in text.txt -out text.csp.enc -cert jcms2 -cmsmode agree

- Расшифровываем сообщение с помощью утилиты JCP:
<path_to_jre>/bin/java.exe -jar cmsutil.jar -decrypt -keystore HDImageStore -alias jcms2 -in text.csp.enc -out text.csp.jcp.txt

6) Проверка корректности построения CMS утилитой
- Создаём сообщение с помощью утилиты JCP:
<path_to_jre>/bin/java.exe -jar cmsutil.jar -encrypt -certstore JavaCertStore -alias jcms2.cer -in text.txt -out text.jcp.enc

- Расшифровываем сообщение с помощью утилиты csptest.exe
csptest.exe -lowenc -decrypt -in text.jcp.enc -out text.jcp.csp.txt -my jcms2 -cmsmode agree



