package com.beforeyoudie.common.di

import android.content.Context
import co.touchlab.kermit.Logger
import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ApplicationPlatformScope
@Component
actual abstract class BydPlatformComponent(
  @get:ApplicationPlatformScope
  @get:Provides
  val context: Context,

  @get:ApplicationPlatformScope
  @get:Provides
  val databaseFileName: DatabaseFileName,

  @get:ApplicationPlatformScope @get:Provides
  val applicationCoroutineContext: ApplicationCoroutineContext
) {
  @Provides
  inline fun <reified T> provideClassLogger(): Logger = Logger.withTag(T::class.toString())

  @ApplicationPlatformScope
  @Provides
  open fun provideSqlDriver(
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

  @ApplicationPlatformScope
  @Provides
  fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory =
    databaseFileName.trim('"').isEmpty() ||
      databaseFileName == SQLiteDatabaseConfiguration.MEMORY_DB_PATH
}
