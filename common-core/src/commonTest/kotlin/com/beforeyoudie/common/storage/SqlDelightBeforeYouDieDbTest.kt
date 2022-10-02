package com.beforeyoudie.common.storage

import com.beforeyoudie.common.di.startKoin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.MockProvider

class SqlDelightBeforeYouDieDbTest : FunSpec(), KoinTest {
    private val mockProvider = MockProvider
    private val db by inject<BeforeYouDieStorageInterface>()
    init {
        beforeTest {
            startKoin()
        }
        afterTest {
            stopKoin()
        }

        test("database is in memory for test") {
            db.isInMemory shouldBe true
        }

        // TODO NOW table behaves how I want tests
    }
}