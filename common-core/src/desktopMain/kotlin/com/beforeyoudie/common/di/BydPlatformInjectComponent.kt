package com.beforeyoudie.common.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
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
      DILogger.d("opening db file with name: $databaseFileName")
    } else {
      DILogger.d("Using in memory database")
    }

    val jdbcUri =
      if (databaseFileName.isNotBlank()) {
        Path("./sqlite/db").createDirectories()
        "jdbc:sqlite:./sqlite/db/$databaseFileName"
      } else {
        JdbcSqliteDriver.IN_MEMORY
      }

    return JdbcSqliteDriver(url = jdbcUri)
  }
}