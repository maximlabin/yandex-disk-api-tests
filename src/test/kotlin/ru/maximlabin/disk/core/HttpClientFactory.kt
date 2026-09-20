package ru.maximlabin.disk.core

import okhttp3.OkHttpClient
import java.time.Duration

/** Единый OkHttpClient для всех тестов. */
object HttpClientFactory {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .addInterceptor(LoggingInterceptor())
            .build()
    }
}
