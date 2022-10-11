package com.beforeyoudie.common.di

import android.content.Context
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.impl.BeforeYouDieDb
import com.beforeyoudie.common.storage.impl.SqlDelightBydStorage
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class AndroidBydPlatformComponent(
  @get:ApplicationPlatformScope
  @get:Provides
  val context: Context,
  private val applicationCoroutineContext: ApplicationCoroutineContext,
  private val ioCoroutineContext: IOCoroutineContext
) : BydPlatformComponent {
  override fun provideApplicationCoroutineContext(): ApplicationCoroutineContext =
    applicationCoroutineContext

  override fun provideIoCoroutineContext(): IOCoroutineContext = ioCoroutineContext
}

@Component
abstract class AndroidPlatformSqlDelightStorageComponent(
  @Component val androidPlatformComponent: AndroidBydPlatformComponent,
  private val databaseFileName: DatabaseFileName = "",
) : ApplicationStoragePlatformComponent {
  val SqlDelightBydStorage.bind: IBydStorage
    @ApplicationStoragePlatformScope
    @Provides
    get() = this

  override fun provideDatabaseFileName(): DatabaseFileName = databaseFileName

  @ApplicationStoragePlatformScope
  @Provides
  fun provideSqlDriver(
    databaseFileName: DatabaseFileName,
    isDbInMemory: IsDbInMemory,
    context: Context
  ): SqlDriver {
    if (isDbInMemory) {
      DILogger.d("opening db file with name: $databaseFileName")
    } else {
      DILogger.d("Using in memory database")
    }

    return AndroidSqliteDriver(
      BeforeYouDieDb.Schema,
      context,
      databaseFileName,
      // Use this to use the newest version of sqlite (not the one packaged with android).
      factory = RequerySQLiteOpenHelperFactory()
    )
  }

  override fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory =
    databaseFileName.trim('"').isEmpty() ||
      databaseFileName == SQLiteDatabaseConfiguration.MEMORY_DB_PATH

  /** Must set up the database schema, creating tables etc. before returning the database. */
  @ApplicationStoragePlatformScope
  @Provides
  fun provideBeforeYouDieDb(driver: SqlDriver): BeforeYouDieDb {
    BeforeYouDieDb.Schema.create(driver)
    return BeforeYouDieDb(driver)
  }
}
