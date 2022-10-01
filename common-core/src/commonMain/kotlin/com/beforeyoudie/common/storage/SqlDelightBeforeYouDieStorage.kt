package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.benasher44.uuid.uuidFrom

/**
 * Sqlite implementation of [BeforeYouDieStorageInterface]
 */
class SqlDelightBeforeYouDieStorage(
    private val database: BeforeYouDieDb,
    override val isInMemory: Boolean
) : BeforeYouDieStorageInterface {
    override fun getAllTaskNodeInformation() =
        database.taskNodeQueries.selectAllTaskNodesWithDependentAndChildData().executeAsList()
            .groupBy {
                it.id
            }.map {
                val value = it.value

                TaskNode(
                    uuidFrom(value.first().id),
                    value.first().title,
                    value.first.description,
                    parent,
                    children,
                    blockingTasks,
                    blockedTasks
                )
            }
}
}