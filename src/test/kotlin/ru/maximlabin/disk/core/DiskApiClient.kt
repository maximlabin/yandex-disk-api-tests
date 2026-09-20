package ru.maximlabin.disk.core

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.maximlabin.disk.model.Link
import ru.maximlabin.disk.model.OperationStatus

/**
 * Обёртка над REST API Яндекс.Диска.
 *
 * Клиент НЕ бросает исключения на 4xx/5xx — всегда возвращает [ApiResponse].
 * Благодаря этому негативные тесты проверяют коды и тела ошибок так же,
 * как позитивные проверяют успешные ответы.
 */
class DiskApiClient(
    private val baseUrl: String = Config.baseUrl,
    private val token: String? = Config.token,
) {
    private val client = HttpClientFactory.client
    private val apiV1 = "$baseUrl/v1/disk"

    // ---- Низкоуровневые методы ------------------------------------------------

    fun get(path: String, query: Map<String, String?> = emptyMap()): ApiResponse =
        execute(builder(url(path, query)).get())

    fun post(path: String, query: Map<String, String?> = emptyMap()): ApiResponse =
        execute(builder(url(path, query)).post(EMPTY_BODY))

    fun put(path: String, query: Map<String, String?> = emptyMap(), body: RequestBody = EMPTY_BODY): ApiResponse =
        execute(builder(url(path, query)).put(body))

    fun delete(path: String, query: Map<String, String?> = emptyMap()): ApiResponse =
        execute(builder(url(path, query)).delete())

    // ---- Доменные операции ----------------------------------------------------

    fun diskInfo(fields: String? = null): ApiResponse =
        get("$apiV1", mapOf("fields" to fields))

    fun getResource(path: String, query: Map<String, String?> = emptyMap()): ApiResponse =
        get("$apiV1/resources", mapOf("path" to path) + query)

    fun createFolder(path: String): ApiResponse =
        put("$apiV1/resources", mapOf("path" to path))

    fun deleteResource(path: String, permanently: Boolean = false): ApiResponse =
        delete("$apiV1/resources", mapOf("path" to path, "permanently" to permanently.toString()))

    fun copy(from: String, to: String, overwrite: Boolean = false): ApiResponse =
        post("$apiV1/resources/copy", mapOf("from" to from, "path" to to, "overwrite" to overwrite.toString()))

    fun move(from: String, to: String, overwrite: Boolean = false): ApiResponse =
        post("$apiV1/resources/move", mapOf("from" to from, "path" to to, "overwrite" to overwrite.toString()))

    fun publish(path: String): ApiResponse =
        put("$apiV1/resources/publish", mapOf("path" to path))

    fun unpublish(path: String): ApiResponse =
        put("$apiV1/resources/unpublish", mapOf("path" to path))

    fun getUploadLink(path: String, overwrite: Boolean = false): ApiResponse =
        get("$apiV1/resources/upload", mapOf("path" to path, "overwrite" to overwrite.toString()))

    fun getDownloadLink(path: String): ApiResponse =
        get("$apiV1/resources/download", mapOf("path" to path))

    fun trashResources(path: String? = null): ApiResponse =
        get("$apiV1/trash/resources", mapOf("path" to (path ?: "trash:/")))

    fun deleteFromTrash(path: String? = null): ApiResponse =
        delete("$apiV1/trash/resources", if (path != null) mapOf("path" to path) else emptyMap())

    /** Загрузка байтов на href, полученный из [getUploadLink]. Возвращает ответ (обычно 201). */
    fun uploadTo(href: String, content: ByteArray, contentType: String = "application/octet-stream"): ApiResponse =
        execute(builder(href).put(content.toRequestBody(contentType.toMediaType())))

    /** Полный сценарий загрузки: получить ссылку и залить содержимое. */
    fun uploadFile(path: String, content: ByteArray, overwrite: Boolean = false): ApiResponse {
        val link = getUploadLink(path, overwrite)
        check(link.isSuccessful) { "Не удалось получить ссылку на загрузку: ${link.code} ${link.body}" }
        return uploadTo(link.to<Link>().href, content)
    }

    /** Скачать содержимое по href из [getDownloadLink]. */
    fun download(href: String): ByteArray {
        client.newCall(builder(href).get().build()).execute().use { resp ->
            return resp.body?.bytes() ?: ByteArray(0)
        }
    }

    /**
     * Опросить операцию до завершения. На вход — ответ, вернувший 202 (со ссылкой на операцию).
     * Возвращает финальный статус или null, если ответ не был асинхронным.
     */
    fun awaitOperation(accepted: ApiResponse, attempts: Int = 20, delayMs: Long = 500): OperationStatus? {
        if (accepted.code != 202) return null
        val href = accepted.to<Link>().href
        repeat(attempts) {
            val status = get(href).to<OperationStatus>()
            if (status.status != "in-progress") return status
            Thread.sleep(delayMs)
        }
        return OperationStatus("in-progress")
    }

    // ---- Служебное ------------------------------------------------------------

    private fun url(pathOrUrl: String, query: Map<String, String?>): String {
        val httpUrl = pathOrUrl.toHttpUrl().newBuilder()
        query.forEach { (k, v) -> if (v != null) httpUrl.addQueryParameter(k, v) }
        return httpUrl.build().toString()
    }

    private fun builder(url: String): Request.Builder =
        Request.Builder().url(url).apply {
            if (token != null) header("Authorization", "OAuth $token")
        }

    private fun execute(builder: Request.Builder): ApiResponse {
        client.newCall(builder.build()).execute().use { resp ->
            return ApiResponse(
                code = resp.code,
                body = resp.body?.string() ?: "",
                contentType = resp.body?.contentType()?.toString(),
            )
        }
    }

    companion object {
        private val EMPTY_BODY: RequestBody = ByteArray(0).toRequestBody(null)
    }
}
