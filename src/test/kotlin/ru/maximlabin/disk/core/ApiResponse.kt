package ru.maximlabin.disk.core

import kotlinx.serialization.json.Json

/**
 * Прочитанный целиком HTTP-ответ. Тело уже вычитано в строку,
 * поэтому объект безопасно использовать после закрытия соединения.
 */
class ApiResponse(
    val code: Int,
    val body: String,
    val contentType: String?,
) {
    val isSuccessful: Boolean get() = code in 200..299

    /** Десериализовать тело в объект типа [T]. */
    inline fun <reified T> to(): T = json.decodeFromString(body)

    companion object {
        val json: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
