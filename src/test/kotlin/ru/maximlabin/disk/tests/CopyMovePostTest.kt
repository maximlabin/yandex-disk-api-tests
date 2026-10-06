package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.core.DiskApiClient
import ru.maximlabin.disk.model.Resource
import ru.maximlabin.disk.util.TestData

@Epic("Yandex Disk REST API")
@Feature("POST /v1/disk/resources/copy|move — копирование и перемещение")
class CopyMovePostTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `копирование файла создаёт копию, оригинал остаётся`() {
        val content = TestData.randomBytes(1024)
        val src = path("src.bin")
        val dst = path("copy.bin")
        api.uploadFile(src, content)

        val resp = api.copy(src, dst)
        api.awaitOperation(resp)

        assertThat(resp.code).isIn(201, 202)
        assertThat(api.getResource(src).code).isEqualTo(200)
        assertThat(awaitResourceCode(dst, 200)).isEqualTo(200)
        assertThat(api.getResource(dst).to<Resource>().md5).isEqualTo(TestData.md5(content))
    }

    @Test
    fun `копирование папки переносит вложенный файл`() {
        val srcDir = path("dir-src")
        val dstDir = path("dir-copy")
        api.createFolder(srcDir)
        api.uploadFile("$srcDir/inner.bin", TestData.randomBytes(64))

        val resp = api.copy(srcDir, dstDir)
        api.awaitOperation(resp)

        assertThat(awaitResourceCode("$dstDir/inner.bin", 200)).isEqualTo(200)
    }

    @Test
    fun `перемещение убирает файл из старого места`() {
        val src = path("move-src.bin")
        val dst = path("move-dst.bin")
        api.uploadFile(src, TestData.randomBytes(256))

        val resp = api.move(src, dst)
        api.awaitOperation(resp)

        assertThat(resp.code).isIn(201, 202)
        assertThat(awaitResourceCode(src, 404)).isEqualTo(404)
        assertThat(awaitResourceCode(dst, 200)).isEqualTo(200)
    }

    @Test
    @Tag("negative")
    fun `копирование поверх существующего без overwrite возвращает 409`() {
        val src = path("a.bin")
        val dst = path("b.bin")
        api.uploadFile(src, TestData.randomBytes(64))
        api.uploadFile(dst, TestData.randomBytes(64))

        val resp = api.copy(src, dst, overwrite = false)

        assertThat(resp.code).isEqualTo(409)
    }

    @Test
    @Tag("negative")
    fun `копирование несуществующего источника возвращает 404`() {
        val resp = api.copy(path("ghost.bin"), path("dest.bin"))

        assertThat(resp.code).isEqualTo(404)
    }

    @Test
    @Tag("negative")
    fun `перемещение без токена возвращает 401`() {
        val resp = DiskApiClient(token = null).move(path("a"), path("b"))

        assertThat(resp.code).isEqualTo(401)
    }
}
