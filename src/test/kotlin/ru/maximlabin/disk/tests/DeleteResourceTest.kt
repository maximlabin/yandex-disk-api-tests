package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.core.DiskApiClient
import ru.maximlabin.disk.util.TestData

@Epic("Yandex Disk REST API")
@Feature("DELETE /v1/disk/resources — удаление ресурсов")
class DeleteResourceTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `файл удаляется в корзину`() {
        val file = path("to-delete.bin")
        api.uploadFile(file, TestData.randomBytes(128))

        val resp = api.deleteResource(file, permanently = false)
        api.awaitOperation(resp)

        assertThat(resp.code).isIn(202, 204)
        assertThat(awaitResourceCode(file, 404)).isEqualTo(404)
    }

    @Test
    fun `permanently true удаляет мимо корзины`() {
        val file = path("perma.bin")
        api.uploadFile(file, TestData.randomBytes(128))

        val resp = api.deleteResource(file, permanently = true)
        api.awaitOperation(resp)

        assertThat(resp.code).isIn(202, 204)
        assertThat(awaitResourceCode(file, 404)).isEqualTo(404)
    }

    @Test
    fun `удаление непустой папки удаляет вложенные файлы`() {
        val dir = path("full-dir")
        api.createFolder(dir)
        api.uploadFile("$dir/inner.bin", TestData.randomBytes(64))

        val resp = api.deleteResource(dir, permanently = true)
        api.awaitOperation(resp)

        assertThat(awaitResourceCode(dir, 404)).isEqualTo(404)
    }

    @Test
    @Tag("negative")
    fun `удаление несуществующего ресурса возвращает 404`() {
        val resp = api.deleteResource(path("nothing.bin"))

        assertThat(resp.code).isEqualTo(404)
    }

    @Test
    @Tag("negative")
    fun `удаление без токена возвращает 401`() {
        val resp = DiskApiClient(token = null).deleteResource(path("x"))

        assertThat(resp.code).isEqualTo(401)
    }
}
