package com.beforeyoudie.common.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@ApplicationPlatformScope
@Component
actual abstract class BydPlatformComponent(
  @get:ApplicationPlatformScope @get:Provides
  val databaseFileName: DatabaseFileName = "",
  @get:ApplicationPlatformScope @get:Provides
  val applicationCoroutineContext: ApplicationCoroutineContext
) {
  @ApplicationPlatformScope
  @Provides
  fun provideSqlDriver(databaseFileName: DatabaseFileName, isDbInMemory: IsDbInMemory): SqlDriver {
    if (isDbInMemory) {
      DILogger.d("opening db file with name: $databaseFileName")
    } else {
      DILogger.w("Using in memory database")
    }

    val jdbcUri = if (!isDbInMemory) {
      Path("./sqlite/db").createDirectories()
      "jdbc:sqlite:./sqlite/db/$databaseFileName"
    } else {
      JdbcSqliteDriver.IN_MEMORY
    }

    return JdbcSqliteDriver(url = jdbcUri)
  }

  @ApplicationPlatformScope
  @Provides
  fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory =
    databaseFileName.trim('"').isEmpty() ||
      databaseFileName == JdbcSqliteDriver.IN_MEMORY
}
