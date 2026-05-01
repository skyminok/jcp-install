Пакет wss4j1_6 представляет собой группу примеров по работе по созданию, проверки ЭЦП, а также проверки ее сервисом СМЭВ.
Подпись реализуется методами wss4j и xmlsec.
Для работы должны быть использованы версии: wss4j - 1.6.19, xmlsec - 1.5.0.

1) Пакет wss4j.wss4j1_6 содержит:
 - класс SOAPXMLSignatureManager_1_6, реализующий функции создания и проверки подписи средставми wss4j
 - пример WSS4J_SignVerifySOAP, использующий функции этого класса
 - несколько тестов для оценки производительности.

 В данном пакете реализована возможность использования класса wss4j.wss4j1_6.ws.security.components.crypto.MerlinEx
 вместо стандартного класса org.apache.ws.security.components.crypto.Merlin. Это позволяет повысить скорость работы
 функций подписи и проверки подписи.

Для данного пакета в папке с проектом должен быть создан каталог /data/WebContent, в него помещен файл crypto.properties
с необходимыми настройками. Например:

org.apache.ws.security.crypto.provider=wss4j.wss4j1_6.ws.security.components.crypto.MerlinEx
org.apache.ws.security.crypto.merlin.keystore.type=HDImageStore
org.apache.ws.security.crypto.merlin.keystore.password=my_password
org.apache.ws.security.crypto.merlin.keystore.alias=my_key_store
cert.file=path_to_cert
ca.file=path_to_ca
crl.file=path_to_crl

,где my_password - пароль для доступа к контейнеру,
my_key_store - название контейнера (alias),
path_to_cert - путь к сертификату, соответствующему my_key_store,
path_to_ca - путь к корневому сертификату для path_to_cert,
path_to_crl - путь к CRL файлу.

Если первый параметр не указывать, будет использован стандартный класс Merlin.


2) Пакет wss4j.wss4j1_6_xmlsec содержит:
 - класс SOAPXMLSignatureManager_1_6, реализующий функции создания и проверки подписи средставми xmlsec
 - пример WSS4J_SignVerifySOAP, использующий функции этого класса
 - несколько тестов для оценки производительности.

3) В пакете wss4j.examples содержатся следующие примеры
SMEVExample - создание и проверка подписи средствами wss4j и опциональная провка сервисом СМЭВ
SMEVExampleXmlsSec - создание и проверка подписи средствами xmlsec и опциональная провка сервисом СМЭВ
SMEVSignBodyThenSecurity - подпись документа средствами xmlsec и создание Security Header с помощью WSS4J.
SmevDocumentCheckExample - проверка сохраненного в файл документа с подписью (можно испольовать после примера SMEVSignBodyThenSecurity)