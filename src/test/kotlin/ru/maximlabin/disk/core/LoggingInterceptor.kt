package ru.maximlabin.disk.core

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * Логирует запрос/ответ и кладёт их вложениями в Allure.
 * Значение заголовка Authorization маскируется.
 */
class LoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestDump = buildString {
            appendLine("${request.method} ${request.url}")
            request.headers.forEach { (name, value) ->
                appendLine("$name: ${if (name.equals("Authorization", true)) MASK else value}")
            }
            request.body?.let { body ->
                if (isText(body.contentType()?.toString())) {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    appendLine()
                    appendLine(buffer.readUtf8())
                } else {
                    appendLine()
                    appendLine("<binary body, ${body.contentLength()} bytes>")
                }
            }
        }
        AllureSteps.attachText("HTTP request — ${request.method} ${request.url.encodedPath}", requestDump)

        val response = chain.proceed(request)

        // peekBody не потребляет исходный поток ответа.
        val peeked = response.peekBody(64 * 1024)
        val responseDump = buildString {
            appendLine("HTTP ${response.code} ${response.message}")
            appendLine()
            appendLine(peeked.string())
        }
        AllureSteps.attachText("HTTP response — ${response.code}", responseDump)

        return response
    }

    private fun isText(contentType: String?): Boolean =
        contentType != null && (contentType.contains("json") ||
            contentType.contains("text") ||
            contentType.contains("urlencoded"))

    private companion object {
        const val MASK = "OAuth ****"
    }
}
