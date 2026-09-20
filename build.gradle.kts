plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("io.qameta.allure") version "2.12.0"
}

group = "ru.maximlabin"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    testImplementation("org.assertj:assertj-core:3.26.3")

    testImplementation("io.qameta.allure:allure-junit5:2.29.0")
}

allure {
    version.set("2.29.0")
    adapter.autoconfigure.set(true)
    adapter.aspectjWeaver.set(true)
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform {
        // Выбор групп тестов: ./gradlew test -PincludeTags=smoke
        (project.findProperty("includeTags") as String?)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.forEach { includeTags(it) }
    }
    // Внешний сервис ограничивает частоту запросов — гоняем в один поток.
    maxParallelForks = 1
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
