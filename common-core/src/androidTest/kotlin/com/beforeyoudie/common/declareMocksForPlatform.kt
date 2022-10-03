package com.beforeyoudie.common

import org.koin.test.KoinTest
import org.koin.test.mock.declareMock

actual fun KoinTest.declareMocksForPlatform() {
    declareMock<android.content.Context>()
}