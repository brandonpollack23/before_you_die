package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.beforeyoudie.common.util.ResultExt
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
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
                id =  uuidFrom(it.id),
                title = it.title,
                isComplete = it.complete,
                description = it.description,
                // TODO NOW remove this if and the other after fixing broken correlated subqueries
                parent = if (it.parent.isNotBlank()) uuidFrom(it.parent) else null,
                children = expandUuidList(it.children),
                blockingTasks = if (it.blocking_tasks.isNotEmpty()) expandUuidList(it.blocking_tasks) else emptySet(),
                blockedTasks = expandUuidList(it.blocked_tasks),
            )
        }

    // TODO STORAGE NOW detect if node with uuid already exits ORRRR change to insert OR update
    // TODO STORAGE NOW check if parent/dependent exists
    // TODO STORAGE NOW detect loops

    override fun insertTaskNode(id: Uuid, title: String, description: String?, complete: Boolean) =
        ResultExt.asResult(BYDFailure::InsertionFailure) {
            database.taskNodeQueries.insertTaskNode(
                id.toString(),
                title,
                description,
                complete
            )
        }

    override fun addChildToTaskNode(parent: Uuid, child: Uuid) =
        // SQLite will throw an exception because child must be unique
        // TODO NOW check for cycles and test
        ResultExt.asResult(BYDFailure::DuplicateParent) {
            database.taskNodeQueries.addChildToTaskNode(
                parent.toString(),
                child.toString()
            )
        }

    override fun addDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit> {
        // TODO NOW check for cycles and test
        var failureReason: Result<Unit> = Result.success(Unit)
        database.taskNodeQueries.transaction {
            val blockedTaskDbEntry =
                database.taskNodeQueries.getTaskNode(blockedTask.toString()).executeAsOneOrNull()
            val blockingTaskDbEntry =
                database.taskNodeQueries.getTaskNode(blockingTask.toString()).executeAsOneOrNull()
            if (blockedTaskDbEntry == null || blockingTaskDbEntry == null ||
                isDependencyAncestorOf(blockingTask, blockedTask)
            ) {
                failureReason = Result.failure(BYDFailure.OperationWouldIntroduceCycle(blockedTask, blockingTask))
                rollback()
            }

            database.taskNodeQueries.addDependencyToTaskNode(blockedTask.toString(), blockingTask.toString())
        }

        return failureReason
    }

    private fun isDependencyAncestorOf(blockingTask: Uuid, blockedTask: Uuid) =
        database.taskNodeQueries.isDependencyAncestorOf(
            blockedTask.toString(),
            blockingTask.toString()
        ).executeAsOne() != 0L
}

fun <T> expandDelimitedList(str: String?, delim: String = ",", mapper: (String) -> T) =
    str?.splitToSequence(delim)?.map { child -> mapper(child) }?.toSet() ?: emptySet()

fun expandUuidList(s: String?) = expandDelimitedList(s, mapper = ::uuidFrom)

fun createDatabase(driver: SqlDriver, isInMemory: Boolean): SqlDelightBeforeYouDieStorage {
    return SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver), isInMemory)
}