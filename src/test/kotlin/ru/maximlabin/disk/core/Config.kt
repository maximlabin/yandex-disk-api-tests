package ru.maximlabin.disk.core

import java.io.File
import java.util.Properties

/**
 * Источники конфигурации проекта.
 *
 * OAuth-токен ищется по приоритету (сверху вниз):
 *  1. системное свойство / свойство Gradle `yandexDiskToken` (`-PyandexDiskToken=...`);
 *  2. переменная окружения `YANDEX_DISK_TOKEN`;
 *  3. файл `local.properties`, ключ `yandexDiskToken`.
 *
 * Токен не хранится в репозитории — см. `local.properties.example`.
 */
object Config {

    /** Базовый URL API. По умолчанию боевой хост, можно переопределить для тестового стенда. */
    val baseUrl: String
        get() = firstNonBlank(
            System.getProperty("yandexDiskBaseUrl"),
            System.getenv("YANDEX_DISK_BASE_URL"),
            localProperty("yandexDiskBaseUrl"),
        ) ?: "https://cloud-api.yandex.net"

    /** OAuth-токен тестового аккаунта. Бросает понятную ошибку, если не задан. */
    val token: String by lazy {
        firstNonBlank(
            System.getProperty("yandexDiskToken"),
            System.getenv("YANDEX_DISK_TOKEN"),
            localProperty("yandexDiskToken"),
        ) ?: error(
            """
            OAuth-токен не найден. Задайте его одним из способов:
              • ./gradlew test -PyandexDiskToken=<token>
              • export YANDEX_DISK_TOKEN=<token>
              • local.properties: yandexDiskToken=<token>
            Используйте ОТДЕЛЬНЫЙ тестовый аккаунт, не личный Диск.
            """.trimIndent(),
        )
    }

    private val localProperties: Properties by lazy {
        Properties().apply {
            File("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
    }

    private fun localProperty(key: String): String? = localProperties.getProperty(key)

    private fun firstNonBlank(vararg values: String?): String? =
        values.firstOrNull { !it.isNullOrBlank() }?.trim()
}
