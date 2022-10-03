package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.BeforeYouDieStorageInterface
import com.beforeyoudie.common.storage.createDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions

actual fun loadPlatformSpecificModule(): Module = module {
    single {
        val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name)
        if (dbFileName.isNotBlank()) logger.debug("opening db file with name: $dbFileName")
        else logger.debug("Using in memory database")

        val driver = AndroidSqliteDriver(
            BeforeYouDieDb.Schema,
            get(),
            dbFileName,
            // Use this to use the newest version of sqlite (not the one packaged with android).
            factory = RequerySQLiteOpenHelperFactory()
        )

        createDatabase(driver, dbFileName.isNotBlank())
    } bind BeforeYouDieStorageInterface::class
}