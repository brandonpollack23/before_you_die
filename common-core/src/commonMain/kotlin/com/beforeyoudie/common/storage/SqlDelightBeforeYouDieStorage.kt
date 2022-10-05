package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.beforeyoudie.common.util.ResultExt
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.db.SqlDriver

// TODO STORAGE NOW detect if node with uuid already exits ORRRR change to insert OR update

/**
 * Sqlite implementation of [BeforeYouDieStorageInterface]
 */
class SqlDelightBeforeYouDieStorage(
  private val database: BeforeYouDieDb,
  override val isInMemory: Boolean
) : BeforeYouDieStorageInterface {
  override fun selectAllTaskNodeInformation() =
    database.taskNodeQueries.selectAllTaskNodesWithDependentAndChildData().executeAsList().map {
      TaskNode(
        id = uuidFrom(it.id),
        title = it.title,
        isComplete = it.complete,
        description = it.description,
        // TODO(#1) SQLDELIGHT_BLOCKED remove this if and the other after fixing broken correlated subqueries
        parent = if (it.parent.isNotBlank()) uuidFrom(it.parent) else null,
        children = expandUuidList(it.children),
        blockingTasks = if (it.blocking_tasks.isNotEmpty()) {
          expandUuidList(it.blocking_tasks)
        } else {
          emptySet()
        },
        blockedTasks = expandUuidList(it.blocked_tasks)
      )
    }

  override fun selectAllActionableTaskNodeInformation() =
    database.taskNodeQueries.selectAllActionableTaskNodes().executeAsList().map {
      TaskNode(
        id = uuidFrom(it.id),
        title = it.title,
        isComplete = it.complete,
        description = it.description,
        // TODO(#1) SQLDELIGHT_BLOCKED remove this if and the other after fixing broken correlated subqueries
        parent = if (it.parent.isNotBlank()) uuidFrom(it.parent) else null,
        children = expandUuidList(it.children),
        blockingTasks = if (it.blocking_tasks.isNotEmpty()) {
          expandUuidList(it.blocking_tasks)
        } else {
          emptySet()
        },
        blockedTasks = expandUuidList(it.blocked_tasks)
      )
    }

  override fun insertTaskNode(id: Uuid, title: String, description: String?, complete: Boolean) =
    ResultExt.asResult(BYDFailure::InsertionFailure) {
      database.taskNodeQueries.insertTaskNode(
        id.toString(),
        title,
        description,
        complete
      )
    }

  override fun markComplete(uuid: Uuid): Result<Unit> {
    var result: Result<Unit> = Result.success(Unit)
    database.transaction {
      val taskNode =
        database.taskNodeQueries.selectTaskNode(uuid.toString()).executeAsOneOrNull()
      if (taskNode == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(uuid))
        rollback()
      }

      database.taskNodeQueries.markTaskComplete(true, uuid.toString())
    }

    return result
  }

  override fun addChildToTaskNode(parent: Uuid, child: Uuid): Result<Unit> {
    // SQLite will throw an exception because child must be unique, I also check for cycles.
    var result: Result<Unit> = Result.success(Unit)

    database.taskNodeQueries.transaction {
      val parentTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(parent.toString()).executeAsOneOrNull()
      val childTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(child.toString()).executeAsOneOrNull()

      if (parentTaskDbEntry == null || childTaskDbEntry == null || isParentAncestorOf(
          parent,
          child
        )
      ) {
        result =
          Result.failure(BYDFailure.OperationWouldIntroduceCycle(parent, child))
        rollback()
      }

      result = ResultExt.asResult({ BYDFailure.DuplicateParent(parent) }) {
        database.taskNodeQueries.addChildToTaskNode(
          parent.toString(),
          child.toString()
        )
      }
    }

    return result
  }

  override fun reparentChildToTaskNode(newParent: Uuid, child: Uuid): Result<Unit> {
    // SQLite will throw an exception because child must be unique, I also check for cycles and
    // that there is an existing parent.
    var result: Result<Unit> = Result.success(Unit)

    database.taskNodeQueries.transaction {
      val childTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(child.toString()).executeAsOneOrNull()
      if (childTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(child))
        rollback()
      }
      if (childTaskDbEntry.parent.isBlank()) {
        result = Result.failure(BYDFailure.ChildHasNoParent(child))
        rollback()
      }

      val newParentTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(newParent.toString()).executeAsOneOrNull()
      if (isParentAncestorOf(newParent, child)) {
        result =
          Result.failure(BYDFailure.OperationWouldIntroduceCycle(newParent, child))
        rollback()
      }

      result = ResultExt.asResult({ BYDFailure.DuplicateParent(newParent) }) {
        database.taskNodeQueries.reparentChild(newParent.toString(), child.toString())
      }
    }

    return result
  }

  override fun addDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit> {
    var result: Result<Unit> = Result.success(Unit)
    database.taskNodeQueries.transaction {
      val blockedTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockedTask.toString()).executeAsOneOrNull()
      val blockingTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockingTask.toString())
          .executeAsOneOrNull()
      if (blockedTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(blockedTask))
        rollback()
      } else if (blockingTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(blockingTask))
        rollback()
      } else if (isDependencyAncestorOf(blockingTask, blockedTask)) {
        result = Result.failure(
          BYDFailure.OperationWouldIntroduceCycle(
            blockingTask,
            blockedTask
          )
        )
        rollback()
      }

      database.taskNodeQueries.addDependencyToTaskNode(
        blockedTask.toString(),
        blockingTask.toString()
      )
    }

    return result
  }

  override fun removeTaskNodeAndChildren(uuid: Uuid): Result<Unit> {
    var result: Result<Unit> = Result.success(Unit)
    database.transaction {
      val taskDbEntry =
        database.taskNodeQueries.selectTaskNode(uuid.toString()).executeAsOneOrNull()
      if (taskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(uuid))
        rollback()
      }

      database.taskNodeQueries.removeTaskNodeAndChildren(uuid.toString())
    }

    return result
  }

  override fun removeDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit> {
    var result: Result<Unit> = Result.success(Unit)
    database.taskNodeQueries.transaction {
      val blockedTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockedTask.toString()).executeAsOneOrNull()
      val blockingTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockingTask.toString())
          .executeAsOneOrNull()
      if (blockedTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(blockedTask))
        rollback()
      } else if (blockingTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(blockingTask))
        rollback()
      }

      val blockedTaskList = expandUuidList(blockingTaskDbEntry.blockedTasks)
      val blockingTaskList = expandUuidList(blockedTaskDbEntry.blockingTasks)
      if (!blockedTaskList.contains(blockedTask) || !blockingTaskList.contains(blockingTask)) {
        result = Result.failure(BYDFailure.NoSuchDependencyRelationship(blockingTask, blockedTask))
        rollback()
      }

      database.taskNodeQueries.removeDependencyRelationship(
        blockingTask.toString(),
        blockedTask.toString()
      )
    }

    return result
  }

  private fun isDependencyAncestorOf(blockingTask: Uuid, blockedTask: Uuid) =
    database.taskNodeQueries.isDependencyAncestorOf(
      blockedTask.toString(),
      blockingTask.toString()
    ).executeAsOne() != 0L

  private fun isParentAncestorOf(parentTask: Uuid, childTask: Uuid) =
    database.taskNodeQueries.isParentAncestorOf(
      childTask.toString(),
      parentTask.toString()
    ).executeAsOne() != 0L
}

fun createDatabase(driver: SqlDriver, isInMemory: Boolean): SqlDelightBeforeYouDieStorage {
  return SqlDelightBeforeYouDieStorage(BeforeYouDieDb(driver), isInMemory)
}

fun expandUuidList(s: String?) = expandDelimitedList(s, mapper = ::uuidFrom)

fun <T> expandDelimitedList(str: String?, delim: String = ",", mapper: (String) -> T) =
  str?.splitToSequence(delim)?.map { child -> mapper(child) }?.toSet() ?: emptySet()
