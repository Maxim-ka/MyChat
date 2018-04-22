## Модуль клиента

написан с использованием javafx

реализовано:
+ 3 окна:
    * окно входа
    * окно чата
    * окно регистрации
+ оповещения:
    * отсутвие связи с сервером
    * отключение со стороны сервера
    * неправильный логин/пароль
    * проверка учетной записи на двойника
    * проверка при регистрации на дубликаты логина и никнейма(по отдельности)
    * информирование о входе, выходе, смене никнема в чате
    * информирование об отсутствии адресата    
+ смена никнейма прямо в окне чата с проверкой на дубликат
+ отсылка привратных сообщений (только одному пользователю)
+ синхронизация списка активных пользователей только при отправке отсутствующему адресату или смене никнейма
      