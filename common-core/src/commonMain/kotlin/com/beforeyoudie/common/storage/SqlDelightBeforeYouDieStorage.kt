package com.beforeyoudie.common.storage

/**
 * Sqlite implementation of [BeforeYouDieStorageInterface]
 */
class SqlDelightBeforeYouDieStorage(
    private val database: BeforeYouDieDb,
    override val isInMemory: Boolean
) : BeforeYouDieStorageInterface {
}