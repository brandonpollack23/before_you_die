package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.impl.SqlDelightBydStorage
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Component
abstract class JvmDesktopPlatformComponent(
  @get:Provides override val applicationCoroutineContext: ApplicationCoroutineContext,
  @get:Provides override val ioCoroutineContext: IOCoroutineContext
) : BydPlatformComponent

@Component
abstract class JvmDesktopPlatformSqlDelightStorageComponent(
  @get:Provides override val databaseFileName: DatabaseFileName = ""
) : ApplicationStoragePlatformComponent {

  @ApplicationStoragePlatformScope
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

  override fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory =
    databaseFileName.trim('"').isEmpty() ||
      databaseFileName == JdbcSqliteDriver.IN_MEMORY
}