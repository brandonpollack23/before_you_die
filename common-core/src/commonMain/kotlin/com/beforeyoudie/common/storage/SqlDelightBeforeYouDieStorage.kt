package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.squareup.sqldelight.db.SqlDriver

/**
 * Sqlite implementation of [BeforeYouDieStorageInterface]
 */
class SqlDelightBeforeYouDieStorage(
    private val database: BeforeYouDieDb,
    override val isInMemory: Boolean
) : BeforeYouDieStorageInterface {
    override fun getAllTaskNodeInformation() =
        database.taskNodeQueries.selectAllTaskNodesWithDependentAndChildData().executeAsList().map {
            TaskNode(
                id =  it.id,
                title = it.title,

            )
        }
}

fun createDatabase(driver: SqlDriver, isInMemory: Boolean): SqlDelightBeforeYouDieStorage {
    return SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver), isInMemory)
}