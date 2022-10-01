package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.BeforeYouDieStorageInterface
import com.beforeyoudie.common.storage.SqlDelightBeforeYouDieStorage
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun loadPlatformSpecificModule(): Module = module {
    single {
        // TODO KOIN use configuration to get DB name
        // TODO STORAGE override this is tests with IN_MEMORY
        val driver = JdbcSqliteDriver("jdbc:sqlite:beforeyoudie.db")
        BeforeYouDieDb.Schema.create(driver)
        SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver))
    } bind BeforeYouDieStorageInterface::class
}