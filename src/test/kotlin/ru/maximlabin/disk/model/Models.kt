package ru.maximlabin.disk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Информация о Диске: /v1/disk */
@Serializable
data class DiskInfo(
    @SerialName("total_space") val totalSpace: Long = 0,
    @SerialName("used_space") val usedSpace: Long = 0,
    @SerialName("trash_size") val trashSize: Long = 0,
    @SerialName("max_file_size") val maxFileSize: Long = 0,
    @SerialName("user") val user: User? = null,
)

@Serializable
data class User(
    @SerialName("login") val login: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("uid") val uid: String? = null,
)

/** Метаинформация о ресурсе: /v1/disk/resources */
@Serializable
data class Resource(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("type") val type: String,               // "dir" | "file"
    @SerialName("md5") val md5: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("public_url") val publicUrl: String? = null,
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("created") val created: String? = null,
    @SerialName("modified") val modified: String? = null,
    @SerialName("_embedded") val embedded: Embedded? = null,
) {
    val isDir: Boolean get() = type == "dir"
    val isFile: Boolean get() = type == "file"
}

@Serializable
data class Embedded(
    @SerialName("items") val items: List<Resource> = emptyList(),
    @SerialName("total") val total: Int = 0,
    @SerialName("limit") val limit: Int = 0,
    @SerialName("offset") val offset: Int = 0,
    @SerialName("path") val path: String? = null,
)

/** Ссылка на операцию (upload/download) или на асинхронную операцию. */
@Serializable
data class Link(
    @SerialName("href") val href: String,
    @SerialName("method") val method: String = "GET",
    @SerialName("templated") val templated: Boolean = false,
)

/** Ответ /v1/disk/operations/{id} */
@Serializable
data class OperationStatus(
    @SerialName("status") val status: String,           // in-progress | success | failed
)

/** Тело ошибки API. */
@Serializable
data class ApiError(
    @SerialName("message") val message: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("error") val error: String? = null,
)
