package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun loadPlatformSpecificModule(): Module = module {
  single {
    val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name)
    if (dbFileName.isNotBlank()) {
      logger.debug("opening db file with name: $dbFileName")
    } else {
      logger.debug("Using in memory database")
    }

    AndroidSqliteDriver(
      BeforeYouDieDb.Schema,
      get(),
      dbFileName,
      // Use this to use the newest version of sqlite (not the one packaged with android).
      factory = RequerySQLiteOpenHelperFactory()
    )
  } withOptions {
    createdAtStart()
  } bind SqlDriver::class
}
