package com.beforeyoudie.common.storage

import com.squareup.sqldelight.db.SqlDriver

/**
 * Sqlite implementation of [BeforeYouDieStorageInterface]
 */
class SqlDelightBeforeYouDieStorage(private val database: BeforeYouDieDb) : BeforeYouDieStorageInterface {
}