# Автотесты REST API Яндекс.Диска

[![API tests](https://github.com/maximlabin/yandex-disk-api-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/maximlabin/yandex-disk-api-tests/actions/workflows/tests.yml)

Проект автотестов для [REST API Яндекс.Диска](https://yandex.ru/dev/disk/rest/).
Покрыты методы **GET**, **POST**, **PUT** и **DELETE** — позитивные и негативные сценарии.

**Стек:** Kotlin 2.0 · JUnit 5 · OkHttp 4 · kotlinx.serialization · AssertJ · Allure · Gradle · GitHub Actions

---

## Быстрый старт

```bash
git clone https://github.com/maximlabin/yandex-disk-api-tests.git
cd yandex-disk-api-tests

# однократно, если в репозитории нет gradle-wrapper
gradle wrapper --gradle-version 8.10.2

export YANDEX_DISK_TOKEN=<токен тестового аккаунта>
./gradlew test
```

Требования: **JDK 17+**, Gradle 8.x (или сгенерированный wrapper), доступ в интернет.

## Получение OAuth-токена

> ⚠️ Используйте **отдельный тестовый аккаунт** Яндекса. Тесты создают, перемещают
> и безвозвратно удаляют файлы в папке `/autotests` — личный Диск для этого не подходит.

1. Зарегистрируйте новый аккаунт на Яндексе.
2. Откройте [полигон Яндекс.Диска](https://yandex.ru/dev/disk/poligon/) и нажмите **Получить OAuth-токен**.
3. Подтвердите выдачу прав приложению — токен появится в поле на странице полигона.

Токен можно передать тремя способами (приоритет сверху вниз):

| Способ | Пример |
|---|---|
| Свойство Gradle | `./gradlew test -PyandexDiskToken=y0__xxx` |
| Переменная окружения | `export YANDEX_DISK_TOKEN=y0__xxx` |
| `local.properties` | `yandexDiskToken=y0__xxx` |

`local.properties` добавлен в `.gitignore`; шаблон — `local.properties.example`.
В CI токен хранится в GitHub Secret `YANDEX_DISK_TOKEN`.

## Запуск

```bash
./gradlew test                                   # весь набор
./gradlew test -PincludeTags=smoke               # только smoke
./gradlew test -PincludeTags=negative            # только негативные
./gradlew test --tests "*CreateFolderPutTest"    # один класс
./gradlew test --tests "*DeleteResourceTest.fileIsDeletedToTrash"
```

HTML-отчёт JUnit: `build/reports/tests/test/index.html`.

### Allure-отчёт

```bash
./gradlew test
allure serve build/allure-results     # требуется Allure CLI: brew install allure
```

В отчёт попадают шаги с HTTP-вызовами и вложения с телами запросов/ответов.
Значение заголовка `Authorization` маскируется.

## Покрытие

| HTTP-метод | Эндпоинт | Проверяется |
|---|---|---|
| GET | `/v1/disk` | квота, фильтр `fields`, 401 без токена, 401 с невалидным токеном |
| GET | `/v1/disk/resources` | метаданные папки и файла, `limit`/`offset`/`sort`, 404, 400 |
| GET | `/v1/disk/resources/download` | ссылка на скачивание, совпадение содержимого |
| GET | `/v1/disk/operations/{id}` | ожидание асинхронных операций (202), используется в copy/move/delete |
| PUT | `/v1/disk/resources` | создание папки, 409 на дубликат и отсутствующий родитель, 401 |
| PUT | `href` от `/resources/upload` | загрузка файла, сверка размера и md5, `overwrite`, 409, 401 |
| PUT | `/v1/disk/resources/publish` `/unpublish` | `public_url`/`public_key`, доступ по ключу, 404 |
| POST | `/v1/disk/resources/copy` | копирование файла и папки, `overwrite`, 409, 404 |
| POST | `/v1/disk/resources/move` | перемещение, переименование, 401 |
| DELETE | `/v1/disk/resources` | удаление в корзину и `permanently=true`, непустая папка, 404, 401 |

## Структура

```
src/test/kotlin/ru/maximlabin/disk/
├── core/
│   ├── Config.kt              # источники токена и базового URL
│   ├── DiskApiClient.kt       # обёртка над API: GET/POST/PUT/DELETE + ожидание операций
│   ├── ApiResponse.kt         # вычитанный ответ + десериализация
│   ├── HttpClientFactory.kt   # общий OkHttpClient
│   ├── LoggingInterceptor.kt  # лог + Allure-вложения, маскирование токена
│   └── AllureSteps.kt         # программные шаги без aspectj-агента
├── model/Models.kt            # DTO ответов API
├── util/TestData.kt           # генерация путей, временных файлов, md5
└── tests/
    ├── BaseDiskTest.kt        # изолированная папка на тест + автоочистка
    ├── DiskInfoGetTest.kt
    ├── ResourcesGetTest.kt
    ├── CreateFolderPutTest.kt
    ├── UploadFilePutTest.kt
    ├── PublishPutTest.kt
    ├── CopyMovePostTest.kt
    └── DeleteResourceTest.kt
```

## Принятые решения

- **Клиент не бросает исключения на 4xx/5xx.** `DiskApiClient` всегда возвращает `ApiResponse`,
  поэтому негативные тесты проверяют коды и тела ошибок так же, как позитивные — успешные ответы.
- **Изоляция.** Каждый тест работает в собственной папке `/autotests/run_<дата>_<id>`,
  которая безвозвратно удаляется в `@AfterEach`. Падение теста не ломает соседние.
- **Асинхронные операции.** Копирование, перемещение и удаление больших ресурсов
  возвращают `202` со ссылкой на операцию — `awaitOperation` опрашивает
  `/v1/disk/operations/{id}` до статуса `success`.
- **Один поток.** Параллельный запуск отключён: внешний сервис ограничивает частоту запросов.
- **Безопасность.** Токен не хранится в репозитории и маскируется в логах и Allure-вложениях.

## CI

`.github/workflows/tests.yml` запускает прогон на push в `main`, на pull request,
вручную и по расписанию. JUnit- и Allure-артефакты публикуются к каждому запуску.
Перед первым запуском добавьте секрет `YANDEX_DISK_TOKEN` в
*Settings → Secrets and variables → Actions*.
