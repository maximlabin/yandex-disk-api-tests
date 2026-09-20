package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.model.Resource
import ru.maximlabin.disk.util.TestData

@Epic("Yandex Disk REST API")
@Feature("GET /v1/disk/resources — метаданные ресурсов")
class ResourcesGetTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `метаданные папки содержат тип dir и путь`() {
        val resp = api.getResource(workDir)

        assertThat(resp.code).isEqualTo(200)
        val res = resp.to<Resource>()
        assertThat(res.isDir).isTrue()
        assertThat(res.path).isEqualTo(workDir)
    }

    @Test
    fun `метаданные файла содержат md5 и размер`() {
        val content = TestData.randomBytes(2048)
        val file = path("meta.bin")
        api.uploadFile(file, content)

        val res = api.getResource(file).to<Resource>()

        assertThat(res.isFile).isTrue()
        assertThat(res.size).isEqualTo(content.size.toLong())
        assertThat(res.md5).isEqualTo(TestData.md5(content))
    }

    @Test
    fun `limit ограничивает число вложенных элементов`() {
        repeat(3) { api.createFolder(path("sub-$it")) }

        val res = api.getResource(workDir, mapOf("limit" to "2")).to<Resource>()

        assertThat(res.embedded?.items).hasSize(2)
        assertThat(res.embedded?.limit).isEqualTo(2)
        assertThat(res.embedded?.total).isGreaterThanOrEqualTo(3)
    }

    @Test
    @Tag("negative")
    fun `несуществующий путь возвращает 404`() {
        val resp = api.getResource(path("no-such-resource"))

        assertThat(resp.code).isEqualTo(404)
        assertThat(resp.body).contains("DiskNotFoundError")
    }

    @Test
    @Tag("negative")
    fun `пустой путь возвращает 400`() {
        val resp = api.getResource("")

        assertThat(resp.code).isEqualTo(400)
    }
}
