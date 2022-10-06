package com.beforeyoudie.common.di

import android.content.Context
import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationPlatformScope

@ApplicationPlatformScope
@Component
actual abstract class BydPlatformInjectComponent actual constructor(
  databaseFileName: DatabaseFileName
) {
  @ApplicationPlatformScope
  @Provides
  fun provideSqlDriver(databaseFileName: DatabaseFileName, context: Context): SqlDriver {
    if (databaseFileName.isNotBlank()) {
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
}

class AndroidComponent(
  @get:Provides val context: Context,
  databaseFileName: DatabaseFileName = ""
) : BydPlatformInjectComponent(databaseFileName)
