package com.beforeyoudie.common.storage

import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.IsDbInMemory
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.beforeyoudie.common.util.ResultExt
import com.beforeyoudie.common.util.getClassLogger
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import me.tatarka.inject.annotations.Inject

/**
 * Sqlite implementation of [IBydStorage]
 */
@Inject
class SqlDelightBydStorage(
  private val database: BeforeYouDieDb,
  override val isInMemory: IsDbInMemory
) : IBydStorage {
  private val logger: Logger = getClassLogger()

  override fun selectAllTaskNodeInformation(): List<TaskNode> {
    logger.v("selecting all task nodes")
    return database.taskNodeQueries.selectAllTaskNodesWithDependentAndChildData()
      .executeAsList()
      .map {
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
  }

  override fun selectAllActionableTaskNodeInformation(): List<TaskNode> {
    logger.v("selecting all task nodes that are not blocked")
    return database.taskNodeQueries.selectAllActionableTaskNodes().executeAsList().map {
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
  }

  override fun insertTaskNode(
    id: Uuid,
    title: String,
    description: String?,
    complete: Boolean
  ): Result<Unit> {
    logger.v {
      "Inserting task: id: $id\n\ttitle: \"$title\"\n\t" +
        "description: \"$description\"\n\tcomplete: $complete"
    }
    return ResultExt.asResult(BYDFailure::InsertionFailure) {
      database.taskNodeQueries.insertTaskNode(
        id.toString(),
        title,
        description,
        complete
      )
    }
  }

  override fun markComplete(uuid: Uuid): Result<Unit> {
    logger.v("Marking task: $uuid as complete")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.markTaskComplete(true, uuid.toString())
    }
  }

  override fun markIncomplete(uuid: Uuid): Result<Unit> {
    logger.v("Marking task: $uuid as incomplete")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.markTaskComplete(false, uuid.toString())
    }
  }

  override fun addChildToTaskNode(parent: Uuid, child: Uuid): Result<Unit> {
    // SQLite will throw an exception because child must be unique, I also check for cycles.
    logger.v("Adding child parent relationship parent: $parent child: $child")
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
        logger.e("parent: $parent child: $child relationship would introduce a cycle!")
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
    logger.v("Reparenting child parent relationship newParent: $newParent child: $child")
    var result: Result<Unit> = Result.success(Unit)

    database.taskNodeQueries.transaction {
      val childTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(child.toString()).executeAsOneOrNull()
      if (childTaskDbEntry == null) {
        result = Result.failure(BYDFailure.NonExistentNodeId(child))
        rollback()
      }
      if (childTaskDbEntry.parent.isBlank()) {
        logger.e("Task $child has no parent! Aborting reparent")
        result = Result.failure(BYDFailure.ChildHasNoParent(child))
        rollback()
      }
      val newParentTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(newParent.toString()).executeAsOneOrNull()
      if (newParentTaskDbEntry == null) {
        logger.e("No such node $newParent to assign as parent to $child")
        result = Result.failure(BYDFailure.NonExistentNodeId(newParent))
      }

      if (isParentAncestorOf(newParent, child)) {
        logger.e("newParent: $newParent child: $child relationship would introduce a cycle!")
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
    logger.v("Adding dependency blockingTask: $blockingTask -> blockedTask: $blockedTask")
    var result: Result<Unit> = Result.success(Unit)
    database.taskNodeQueries.transaction {
      val blockedTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockedTask.toString()).executeAsOneOrNull()
      val blockingTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockingTask.toString())
          .executeAsOneOrNull()
      if (blockedTaskDbEntry == null) {
        logger.e("No such node $blockedTask")
        result = Result.failure(BYDFailure.NonExistentNodeId(blockedTask))
        rollback()
      } else if (blockingTaskDbEntry == null) {
        logger.e("No such node $blockingTask")
        result = Result.failure(BYDFailure.NonExistentNodeId(blockingTask))
        rollback()
      } else if (isDependencyAncestorOf(blockingTask, blockedTask)) {
        logger.e {
          "blockingTask: $blockingTask blockedTask: $blockedTask " +
            "relationship would introduce a cycle!"
        }
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

  override fun updateTaskTitle(uuid: Uuid, title: String): Result<Unit> {
    logger.v("Updating task $uuid with title \"$title\"")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.updateTitle(nodeId = uuid.toString(), title = title)
    }
  }

  override fun updateTaskDescription(uuid: Uuid, description: String?): Result<Unit> {
    logger.v("Updating task $uuid with title \"$description\"")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.updateDescription(
        nodeId = uuid.toString(),
        description = description
      )
    }
  }

  override fun removeTaskNodeAndChildren(uuid: Uuid): Result<Unit> {
    logger.v("Removing task and all descendants of $uuid")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.removeTaskNodeAndChildren(uuid.toString())
    }
  }

  override fun removeDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit> {
    logger.v {
      "Removing dependency relationship blockingTask:" +
        "$blockingTask -> blockedTask: $blockedTask"
    }
    var result: Result<Unit> = Result.success(Unit)
    database.taskNodeQueries.transaction {
      val blockedTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockedTask.toString()).executeAsOneOrNull()
      val blockingTaskDbEntry =
        database.taskNodeQueries.selectTaskNode(blockingTask.toString())
          .executeAsOneOrNull()
      if (blockedTaskDbEntry == null) {
        logger.e("No such node $blockedTask")
        result = Result.failure(BYDFailure.NonExistentNodeId(blockedTask))
        rollback()
      } else if (blockingTaskDbEntry == null) {
        logger.e("No such node $blockingTask")
        result = Result.failure(BYDFailure.NonExistentNodeId(blockingTask))
        rollback()
      }

      val blockedTaskList = expandUuidList(blockingTaskDbEntry.blockedTasks)
      val blockingTaskList = expandUuidList(blockedTaskDbEntry.blockingTasks)
      if (!blockedTaskList.contains(blockedTask) || !blockingTaskList.contains(blockingTask)) {
        logger.e("No dependency blockingTask: $blockingTask -> blockedTask: $blockedTask")
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

  private inline fun simpleUpdate(
    uuid: Uuid,
    crossinline updateOperation: (Uuid) -> Unit
  ): Result<Unit> {
    var result: Result<Unit> = Result.success(Unit)
    database.transaction {
      val taskNode =
        database.taskNodeQueries.selectTaskNode(uuid.toString()).executeAsOneOrNull()
      if (taskNode == null) {
        logger.e("no task node $uuid exists")
        result = Result.failure(BYDFailure.NonExistentNodeId(uuid))
        rollback()
      }

      updateOperation(uuid)
    }

    return result
  }
}

fun expandUuidList(s: String?) = expandDelimitedList(s, mapper = ::uuidFrom)

fun <T> expandDelimitedList(str: String?, delim: String = ",", mapper: (String) -> T) =
  str?.splitToSequence(delim)?.map { child -> mapper(child) }?.toSet() ?: emptySet()
