package com.beforeyoudie.common

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.startKoin
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

open class CommonTest : FunSpec(), KoinTest {
    init {
        beforeTest {
            Logger.setLogWriters(CommonWriter())
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