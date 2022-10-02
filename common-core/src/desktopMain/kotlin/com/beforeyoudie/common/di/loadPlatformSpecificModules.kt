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
        val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name).trim('"')
        if (dbFileName.isNotBlank()) logger.debug("opening db file with name: $dbFileName")
        else logger.debug("Using in memory database")

        val jdbcUri = if (dbFileName.isNotBlank()) "jdbc:sqlite:./sqlite/db/$dbFileName" else JdbcSqliteDriver.IN_MEMORY
        val driver = JdbcSqliteDriver(url = jdbcUri)
        BeforeYouDieDb.Schema.create(driver)

        SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver), jdbcUri == JdbcSqliteDriver.IN_MEMORY)
    } withOptions {
        createdAtStart()
    } bind BeforeYouDieStorageInterface::class
}