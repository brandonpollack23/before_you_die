package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.impl.BeforeYouDieDb
import com.beforeyoudie.common.storage.impl.SqlDelightBydStorage
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Component
abstract class JvmDesktopPlatformComponent(
  private val applicationCoroutineContext: ApplicationCoroutineContext,
  private val ioCoroutineContext: IOCoroutineContext
) : BydPlatformComponent {
  override fun provideApplicationCoroutineContext(): ApplicationCoroutineContext =
    applicationCoroutineContext

  override fun provideIoCoroutineContext(): IOCoroutineContext = ioCoroutineContext
}

@Component
abstract class JvmDesktopPlatformSqlDelightStorageComponent(
  private val databaseFileName: DatabaseFileName = ""
) : ApplicationStoragePlatformComponent {
  val SqlDelightBydStorage.bind: IBydStorage
    @ApplicationStoragePlatformScope
    @Provides
    get() = this

  override fun provideDatabaseFileName(): DatabaseFileName = databaseFileName

  /** Must set up the database schema, creating tables etc. before returning the database. */
  @ApplicationStoragePlatformScope
  @Provides
  fun provideBeforeYouDieDb(driver: SqlDriver): BeforeYouDieDb {
    BeforeYouDieDb.Schema.create(driver)
    return BeforeYouDieDb(driver)
  }

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
