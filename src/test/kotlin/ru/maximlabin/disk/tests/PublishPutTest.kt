package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.model.Resource
import ru.maximlabin.disk.util.TestData

@Epic("Yandex Disk REST API")
@Feature("PUT /v1/disk/resources/publish — публикация ресурса")
class PublishPutTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `публикация даёт public_url и public_key`() {
        val file = path("shared.bin")
        api.uploadFile(file, TestData.randomBytes(128))

        val publish = api.publish(file)
        assertThat(publish.code).isEqualTo(200)

        val meta = api.getResource(file).to<Resource>()
        assertThat(meta.publicUrl).isNotBlank()
        assertThat(meta.publicKey).isNotBlank()
    }

    @Test
    fun `unpublish снимает публикацию`() {
        val file = path("unshare.bin")
        api.uploadFile(file, TestData.randomBytes(128))
        api.publish(file)

        val unpublish = api.unpublish(file)
        assertThat(unpublish.code).isEqualTo(200)

        val meta = api.getResource(file).to<Resource>()
        assertThat(meta.publicUrl).isNull()
    }

    @Test
    @Tag("negative")
    fun `публикация несуществующего ресурса возвращает 404`() {
        val resp = api.publish(path("ghost.bin"))

        assertThat(resp.code).isEqualTo(404)
    }
}
