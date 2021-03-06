MapDemo "Трекинг в режиме реального времени"
============================================
<img src="http://igorpi25.ru/dist/images/mapdemo_screenshot.png" height="200" />

**Модуль был разработан в рамках хакатона [HACKYKT2016][11]**

Это система трекинга в режиме реального времени, построенный с использованием open-source библиотек. Движок польностью модульный. Каждый модуль представляет отдельный open-source проект на Github, с описанинием и запускаемым демонстрационным приложением.

Демонстрационное приложение можете загрузить [отсюда][11]

[<img src="http://igorpi25.ru/screenshot/chat/google_play_icon.png" height="60" />][11]

WebSocket
---------
Работа с вебсокетами происходит благодаря библиотеке автора Communicator. Здесь и далее, применяется единая архитектура описанная в проекте Chat автора. Прежде чем перейти к изучению кода, рекомендуем ознакомится с  [Communicator][82]

Серверная часть
---------------
**ВНИМАНИЕ!** Данная библиотека - это реализация только клиентской части. Серверную часть вы можете видеть в репозитории автора на [GitHub][4]. Демо имеет поправки для работы с картой, и была сделана в рамках HACKYKT 2016

Клонирование из GitHub
----------------------

Проект содержит много git-подмодулей, которые при неумелом использовании могут принести неприятности. Поэтому делайте как я, тогда с git-подмодулями проблем не будет

1. Для клонирования репозитория, автор рекомендует использовать командную строку, вместо EGit Eclipse или AndroidStudio. Т.к. проект содержит настройки workspace в папке репозитория, и репозитории подмодулей находятся НЕ в ветке **master**, и подмодули тоже имеют свои подмодули. В командной строке выполните:
	```
$ git clone git://github.com/Igorpi25/Map.git Map
$ cd Chat
$ git submodule update --remote --recursive --init
	```
Параметры третьей строки (`git submodule update --remote --recursive --init`) означают:
	* `--remote` - подмодуль нужно скачать из ветки удаленного репозитория. Название ветки записано из файле ".submodule" в параметре `branch`. Это ветка `library`, в ветке `master` находится запускаемый демо-проект
	* `--recursive` - повторяй команду `git submodule update --remote --recursive --init` для всех модулей, и их вложенных подмодулей
	* `--init` - если подмодуль не инициализирован, то инициализируй. (во вложенных подмодулях это очень кстати, без этого нам бы пришлось вызывать `git submodule init` для каждого вложенного подмодуля)

Используемые библиотеки
-----------------------
* [Volley][2]
* [Glid][9] - используется в демо-проекте

Библиотеки автора
-----------------
* [Connection][84]- Диалоги ошибки соединения к интернету. Включен в составе Session
* [Session][8] - Авторизация пользователя на сервере
* [Communicator][82] - это архитектурно важная библиотека. Все дела с Websocket, парсинг json и т.п.
* [Profile][85] - профили пользователся и группы. Модуль обеспечивает элементы "социальной сети"

License
-------

See the [LICENSE](LICENSE) file for license rights and limitations (Apache).

[1]: http://actionbarsherlock.com/
[2]: https://github.com/mcxiaoke/android-volley
[4]: https://github.com/Igorpi25/server_v2
[5]: https://git-scm.com/book/en/v2/Git-Tools-Submodules/
[6]: https://github.com/Igorpi25/Profile
[7]: http://www.androidhive.info/2014/01/how-to-create-rest-api-for-android-app-using-php-slim-and-mysql-day-12-2/

[8]: https://github.com/Igorpi25/Session
[81]: https://github.com/Igorpi25/MultipleTypesAdapter
[82]: https://github.com/Igorpi25/Communicator
[83]: https://github.com/Igorpi25/Uploader
[84]: https://github.com/Igorpi25/Connection
[85]: https://github.com/Igorpi25/Profile
[9]: https://github.com/bumptech/glide
[10]:https://code.google.com/archive/p/httpclientandroidlib/

[11]:http://igorpi25.ru/dist/download/mapdemo.apk
[12]:http://www.hackykt.ru/

