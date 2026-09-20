package ru.maximlabin.disk.core

import io.qameta.allure.Allure

/** Тонкие обёртки над Allure — программные шаги без aspectj-агента в вызывающем коде. */
object AllureSteps {

    fun <T> step(name: String, action: () -> T): T =
        Allure.step(name, Allure.ThrowableContextRunnable { action() })

    fun attachText(name: String, content: String) {
        Allure.addAttachment(name, "text/plain", content, ".txt")
    }
}
