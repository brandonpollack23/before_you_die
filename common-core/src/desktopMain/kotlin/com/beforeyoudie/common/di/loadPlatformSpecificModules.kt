package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.BeforeYouDieStorageInterface
import com.beforeyoudie.common.storage.SqlDelightBeforeYouDieStorage
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun loadPlatformSpecificModule(): Module = module {
    single {
        val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name)
        if (dbFileName.isNotBlank()) logger.debug("opening db file with name: $dbFileName")
        else logger.debug("Using in memory database")

        val driver = JdbcSqliteDriver("jdbc:sqlite:./sqlite/db/$dbFileName")
        BeforeYouDieDb.Schema.create(driver)
        SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver), dbFileName == "")
    } withOptions {
        createdAtStart()
    } bind BeforeYouDieStorageInterface::class
}