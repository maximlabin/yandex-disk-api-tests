package ru.maximlabin.disk.tests

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.maximlabin.disk.core.Config
import ru.maximlabin.disk.core.DiskApiClient
import ru.maximlabin.disk.model.DiskInfo

@Epic("Yandex Disk REST API")
@Feature("GET /v1/disk — информация о Диске")
class DiskInfoGetTest {

    private val api = DiskApiClient()

    @Test
    @Tag("smoke")
    fun `возвращает квоту Диска`() {
        val resp = api.diskInfo()

        assertThat(resp.code).isEqualTo(200)
        val info = resp.to<DiskInfo>()
        assertThat(info.totalSpace).isPositive()
        assertThat(info.usedSpace).isGreaterThanOrEqualTo(0)
        assertThat(info.usedSpace).isLessThanOrEqualTo(info.totalSpace)
    }

    @Test
    fun `фильтр fields возвращает только запрошенные поля`() {
        val resp = api.diskInfo(fields = "total_space")

        assertThat(resp.code).isEqualTo(200)
        assertThat(resp.body).contains("total_space")
        assertThat(resp.body).doesNotContain("trash_size")
    }

    @Test
    @Tag("negative")
    fun `без токена возвращает 401`() {
        val anonymous = DiskApiClient(token = null)

        val resp = anonymous.diskInfo()

        assertThat(resp.code).isEqualTo(401)
    }

    @Test
    @Tag("negative")
    fun `с невалидным токеном возвращает 401`() {
        val invalid = DiskApiClient(token = "definitely-not-a-valid-token")

        val resp = invalid.diskInfo()

        assertThat(resp.code).isEqualTo(401)
        assertThat(Config.baseUrl).startsWith("https://")
    }
}
