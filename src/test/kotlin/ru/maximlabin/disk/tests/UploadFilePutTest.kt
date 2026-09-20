package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.model.Link
import ru.maximlabin.disk.model.Resource
import ru.maximlabin.disk.util.TestData

@Epic("Yandex Disk REST API")
@Feature("PUT href — загрузка файла")
class UploadFilePutTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `загруженный файл совпадает по размеру и md5`() {
        val content = TestData.randomBytes(4096)
        val file = path("upload.bin")

        val upload = api.uploadFile(file, content)
        assertThat(upload.code).isEqualTo(201)

        val meta = api.getResource(file).to<Resource>()
        assertThat(meta.size).isEqualTo(content.size.toLong())
        assertThat(meta.md5).isEqualTo(TestData.md5(content))
    }

    @Test
    fun `скачанный файл идентичен загруженному`() {
        val content = TestData.randomBytes(3000)
        val file = path("roundtrip.bin")
        api.uploadFile(file, content)

        val href = api.getDownloadLink(file).to<Link>().href
        val downloaded = api.download(href)

        assertThat(downloaded).isEqualTo(content)
    }

    @Test
    fun `overwrite true заменяет содержимое`() {
        val file = path("over.bin")
        api.uploadFile(file, TestData.randomBytes(512))
        val updated = TestData.randomBytes(1024)

        val resp = api.uploadFile(file, updated, overwrite = true)

        assertThat(resp.isSuccessful).isTrue()
        assertThat(api.getResource(file).to<Resource>().md5).isEqualTo(TestData.md5(updated))
    }

    @Test
    @Tag("negative")
    fun `загрузка без overwrite поверх существующего файла возвращает 409`() {
        val file = path("conflict.bin")
        api.uploadFile(file, TestData.randomBytes(256))

        val link = api.getUploadLink(file, overwrite = false)

        assertThat(link.code).isEqualTo(409)
    }

    @Test
    @Tag("negative")
    fun `ссылка на загрузку без токена возвращает 401`() {
        val resp = ru.maximlabin.disk.core.DiskApiClient(token = null)
            .getUploadLink(path("nope.bin"))

        assertThat(resp.code).isEqualTo(401)
    }
}
