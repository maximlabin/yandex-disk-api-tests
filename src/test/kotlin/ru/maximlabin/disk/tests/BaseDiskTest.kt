package ru.maximlabin.disk.tests

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import ru.maximlabin.disk.core.DiskApiClient
import ru.maximlabin.disk.util.TestData

/**
 * База для всех тестов: у каждого теста своя папка `/autotests/run_...`,
 * которая безвозвратно удаляется после теста. Падение одного теста
 * не влияет на остальные.
 */
abstract class BaseDiskTest {

    protected val api = DiskApiClient()

    /** Изолированная рабочая папка текущего теста. */
    protected lateinit var workDir: String

    @BeforeEach
    fun createWorkDir() {
        // Родительский /autotests может ещё не существовать — создаём молча (409 = уже есть).
        api.createFolder(TestData.ROOT)
        workDir = TestData.uniqueRunDir()
        val created = api.createFolder(workDir)
        check(created.isSuccessful) { "Не удалось создать рабочую папку: ${created.code} ${created.body}" }
    }

    @AfterEach
    fun removeWorkDir() {
        runCatching {
            val resp = api.deleteResource(workDir, permanently = true)
            api.awaitOperation(resp)
        }
    }

    /** Путь внутри рабочей папки. */
    protected fun path(name: String): String = "$workDir/$name"
}
