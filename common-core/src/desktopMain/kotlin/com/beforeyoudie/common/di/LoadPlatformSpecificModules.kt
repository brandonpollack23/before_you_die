package com.beforeyoudie.common.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

actual fun loadPlatformSpecificModule(): Module = module {
  single {
    val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name).trim('"')
    if (dbFileName.isNotBlank()) {
      logger.debug("opening db file with name: $dbFileName")
    } else {
      logger.debug("Using in memory database")
    }

    val jdbcUri =
      if (dbFileName.isNotBlank()) {
        Path("./sqlite/db").createDirectories()
        "jdbc:sqlite:./sqlite/db/$dbFileName"
      } else {
        JdbcSqliteDriver.IN_MEMORY
      }

    JdbcSqliteDriver(url = jdbcUri)
  } withOptions {
    createdAtStart()
  } bind SqlDriver::class
}
