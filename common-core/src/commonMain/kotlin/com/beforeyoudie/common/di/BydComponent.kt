package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.IBydStorage
import com.squareup.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Component

@Component
abstract class BydComponent(val databaseFileName: String = "") {
  abstract val storage: IBydStorage
}

expect fun provideSqlDriver(databaseFileName: String): SqlDriver