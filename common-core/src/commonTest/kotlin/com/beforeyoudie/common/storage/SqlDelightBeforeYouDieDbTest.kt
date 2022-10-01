package com.beforeyoudie.common.storage

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.di.loadKoinModules
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FunSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class SqlDelightBeforeYouDieDbTest : FunSpec(), KoinTest {
    override fun extensions(): List<Extension> = listOf(KoinExtension(loadKoinModules()))

    private val db by inject<BeforeYouDieStorageInterface>()

    init {
        test("database is in memory for test") {
            db.isInMemory shouldBe true
        }
    }
}