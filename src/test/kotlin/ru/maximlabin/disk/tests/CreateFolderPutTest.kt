package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.core.DiskApiClient
import ru.maximlabin.disk.model.Resource

@Epic("Yandex Disk REST API")
@Feature("PUT /v1/disk/resources — создание папки")
class CreateFolderPutTest : BaseDiskTest() {

    @Test
    @Tag("smoke")
    fun `создаёт папку и она появляется на Диске`() {
        val dir = path("new-folder")

        val resp = api.createFolder(dir)

        assertThat(resp.code).isEqualTo(201)
        assertThat(api.getResource(dir).to<Resource>().isDir).isTrue()
    }

    @Test
    @Tag("negative")
    fun `повторное создание возвращает 409`() {
        val dir = path("dup")
        api.createFolder(dir)

        val resp = api.createFolder(dir)

        assertThat(resp.code).isEqualTo(409)
        assertThat(resp.body).contains("DiskPathPointsToExistentDirectoryError")
    }

    @Test
    @Tag("negative")
    fun `создание в несуществующем родителе возвращает 409`() {
        val resp = api.createFolder(path("missing-parent/child"))

        assertThat(resp.code).isEqualTo(409)
    }

    @Test
    @Tag("negative")
    fun `без токена возвращает 401`() {
        val resp = DiskApiClient(token = null).createFolder(path("x"))

        assertThat(resp.code).isEqualTo(401)
    }
}
