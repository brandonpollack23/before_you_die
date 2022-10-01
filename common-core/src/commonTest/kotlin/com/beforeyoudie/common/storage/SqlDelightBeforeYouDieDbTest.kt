package com.beforeyoudie.common.storage

import com.beforeyoudie.common.di.startKoin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.MockProvider

class SqlDelightBeforeYouDieDbTest : FunSpec(), KoinTest {
    private val mockProvider = MockProvider
    private val db by inject<BeforeYouDieStorageInterface>()
    init {

        test("database is in memory for test") {
            startKoin()
            db.isInMemory shouldBe true
        }
    }
}