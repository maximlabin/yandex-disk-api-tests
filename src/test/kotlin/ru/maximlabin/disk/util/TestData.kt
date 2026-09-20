package ru.maximlabin.disk.util

import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

/** Генерация уникальных путей и тестовых данных. */
object TestData {

    /** Корень, в котором работают все тесты. */
    const val ROOT = "disk:/autotests"

    private val counter = AtomicInteger(0)
    private val stamp: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    /** Уникальная папка на один тест: /autotests/run_<дата>_<n>_<random>. */
    fun uniqueRunDir(): String {
        val n = counter.incrementAndGet()
        val rnd = (100000..999999).random()
        return "$ROOT/run_${stamp}_${n}_$rnd"
    }

    fun randomName(prefix: String = "item", ext: String = ""): String {
        val rnd = (100000..999999).random()
        return "$prefix-$rnd$ext"
    }

    /** Псевдослучайное содержимое файла заданного размера. */
    fun randomBytes(size: Int = 1024): ByteArray = ByteArray(size).also { java.util.Random().nextBytes(it) }

    fun md5(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }
}
