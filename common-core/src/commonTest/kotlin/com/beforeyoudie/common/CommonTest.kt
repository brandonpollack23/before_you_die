package com.beforeyoudie.common

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.startKoin
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.junit5.mock.MockProviderExtension

open class CommonTest : FunSpec(), KoinTest {
    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { mockkClass(it) }

    init {
        beforeTest {
            Logger.setLogWriters(CommonWriter())
            declareMocksForPlatform()
            startKoin()
        }
        afterTest {
            stopKoin()
        }
    }

    final override fun afterTest(f: suspend (Tuple2<TestCase, TestResult>) -> Unit) =
        super.afterTest(f)
    final override fun beforeTest(f: suspend (TestCase) -> Unit) = super.beforeTest(f)
}

expect fun KoinTest.declareMocksForPlatform()