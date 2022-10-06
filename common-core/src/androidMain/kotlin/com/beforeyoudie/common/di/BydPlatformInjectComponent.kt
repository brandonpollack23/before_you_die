package com.beforeyoudie.common.di

import com.squareup.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationPlatformScope
@Component

@ApplicationPlatformScope
actual abstract class BydPlatformInjectComponent actual constructor(databaseFileName: DatabaseFileName) {
  @ApplicationPlatformScope
  @Provides
  fun provideSqlDriver(databaseFileName: DatabaseFileName): SqlDriver {
    if (databaseFileName.isNotBlank()) {
      DILogger.debug("opening db file with name: $databaseFileName")
    } else {
      DILogger.debug("Using in memory database")
    }

    AndroidSqliteDriver(
      BeforeYouDieDb.Schema,
      get(),
      databaseFileName,
      // Use this to use the newest version of sqlite (not the one packaged with android).
      factory = RequerySQLiteOpenHelperFactory()
    )
  }
}