package com.beforeyoudie.common.storage.impl

import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.IsDbInMemory
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.BYDFailure
import com.beforeyoudie.common.util.ResultExt
import com.beforeyoudie.common.util.getClassLogger
import com.beforeyoudie.storage.TaskNodeQueries
import com.benasher44.uuid.uuidFrom
import me.tatarka.inject.annotations.Inject

// TODO(#11) STORAGE use the result type transaction to cleanup some of the mutation style code here.

/**
 * Sqlite implementation of [IBydStorage]
 */
@Inject
class SqlDelightBydStorage(
  private val database: BeforeYouDieDb,
  override val isInMemory: IsDbInMemory
) : IBydStorage {
  private val logger: Logger = getClassLogger()

  override fun selectAllTaskNodeInformation(): Map<TaskId, TaskNode> {
    logger.v("selecting all task nodes")
    return database.taskNodeQueries.selectAllTaskNodesWithDependentAndChildData()
      .executeAsList()
      .associate {
        val taskId = TaskId(uuidFrom(it.id))
        taskId to
          TaskNode(
            id = taskId,
            title = it.title,
            description = it.description,
            isComplete = it.complete,
            // TODO(#1) SQLDELIGHT_BLOCKED remove this if and the other after fixing broken correlated subqueries
            parent = if (it.parent.isNotBlank()) TaskId(uuidFrom(it.parent)) else null,
            children = expandTaskIdList(it.children),
            blockingTasks = if (it.blocking_tasks.isNotEmpty()) {
              expandTaskIdList(it.blocking_tasks)
            } else {
              emptySet()
            },
            blockedTasks = expandTaskIdList(it.blocked_tasks)
          )
      }
  }

  override fun selectAllActionableTaskNodeInformation(): Map<TaskId, TaskNode> {
    logger.v("selecting all task nodes that are not blocked")
    return database.taskNodeQueries.selectAllActionableTaskNodes()
      .executeAsList()
      .associate {
        val taskId = TaskId(uuidFrom(it.id))
        taskId to
          TaskNode(
            id = taskId,
            title = it.title,
            description = it.description,
            isComplete = it.complete,
            // TODO(#1) SQLDELIGHT_BLOCKED remove this if and the other after fixing broken correlated subqueries
            parent = if (it.parent.isNotBlank()) TaskId(uuidFrom(it.parent)) else null,
            children = expandTaskIdList(it.children),
            blockingTasks = if (it.blocking_tasks.isNotEmpty()) {
              expandTaskIdList(it.blocking_tasks)
            } else {
              emptySet()
            },
            blockedTasks = expandTaskIdList(it.blocked_tasks)
          )
      }
  }

  override fun insertTaskNode(
    id: TaskId,
    title: String,
    description: String?,
    parent: TaskId?,
    complete: Boolean
  ): Result<TaskNode> {
    logger.v {
      "Inserting task: id: $id\n\ttitle: \"$title\"\n\t" +
        "description: \"$description\"\n\tcomplete: $complete" +
        "parent: $parent"
    }

    var result = Result.success(TaskNode(id, title, description, complete, parent))
    database.transaction {
      if (parent != null) {
        if (database.taskNodeQueries.selectTaskNode(parent.toString())
          .executeAsOneOrNull() == null
        ) {
          result = Result.failure(BYDFailure.NonExistentNodeId(parent))
          rollback()
        }

        // This shouldnt even be possible. It isnt possible with a brand new node, no one can point to it. (assuming it isnt a weird undeleted state, but I check for that elsewhere).
        if (isParentAncestorOf(parent, id)) {
          logger.a("parent: $parent child: $id relationship would introduce a cycle!")
          result = Result.failure(BYDFailure.OperationWouldIntroduceCycle(parent, id))
        }

        database.taskNodeQueries.addChildToTaskNode(parent.toString(), id.toString())
      }

      database.taskNodeQueries.insertTaskNode(
        id.toString(),
        title,
        description,
        complete
      )
    }

    return result
  }

  override fun markComplete(uuid: TaskId): Result<Unit> {
    logger.v("Marking task: $uuid as complete")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.markTaskComplete(true, uuid.toString())
    }
  }

  override fun markIncomplete(uuid: TaskId): Result<Unit> {
    logger.v("Marking task: $uuid as incomplete")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.markTaskComplete(false, uuid.toString())
    }
  }

  override fun addChildToTaskNode(parent: TaskId, child: TaskId): Result<Unit> {
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

  override fun reparentChildToTaskNode(newParent: TaskId, child: TaskId): Result<Unit> {
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

  override fun addDependencyRelationship(blockingTask: TaskId, blockedTask: TaskId): Result<Unit> {
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

  override fun updateTaskTitle(uuid: TaskId, title: String): Result<Unit> {
    logger.v("Updating task $uuid with title \"$title\"")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.updateTitle(nodeId = uuid.toString(), title = title)
    }
  }

  override fun updateTaskDescription(uuid: TaskId, description: String?): Result<Unit> {
    logger.v("Updating task $uuid with title \"$description\"")
    return simpleUpdate(uuid) {
      database.taskNodeQueries.updateDescription(
        nodeId = uuid.toString(),
        description = description
      )
    }
  }

  // TODO(#1) Dirty hack because of sqldelight bug in generating causing conflicting declarations for the separate delete tasks.
  private fun TaskNodeQueries.removeTaskNodeAndChildren(tasks: Collection<String>) =
    removeTaskNodeAndChildren(tasks, tasks, tasks)

  override fun removeTaskNodeAndChildren(uuid: TaskId): Result<Collection<TaskId>> {
    logger.v("Removing task and all descendants of $uuid")
    return database.transactionWithResult {
      val root = database.taskNodeQueries.selectTaskNode(uuid.toString()).executeAsOneOrNull()
      if (root == null) {
        logger.e("No such task $uuid")
        rollback(Result.failure(BYDFailure.NonExistentNodeId(uuid)))
      }

      val taskIdsToRemove =
        database.taskNodeQueries.selectTaskselectTaskNodeAndDescendentIds(uuid.toString())
          .executeAsList()

      database.taskNodeQueries.removeTaskNodeAndChildren(taskIdsToRemove.map { it })
      Result.success(taskIdsToRemove.asSequence().map { TaskId(uuidFrom(it)) }.toSet())
    }
  }

  override fun removeDependencyRelationship(
    blockingTask: TaskId,
    blockedTask: TaskId
  ): Result<Unit> {
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

      val blockedTaskList = expandTaskIdList(blockingTaskDbEntry.blockedTasks)
      val blockingTaskList = expandTaskIdList(blockedTaskDbEntry.blockingTasks)
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

  private fun isDependencyAncestorOf(blockingTask: TaskId, blockedTask: TaskId) =
    database.taskNodeQueries.isDependencyAncestorOf(
      blockedTask.toString(),
      blockingTask.toString()
    ).executeAsOne() != 0L

  private fun isParentAncestorOf(parentTask: TaskId, childTask: TaskId) =
    database.taskNodeQueries.isParentAncestorOf(
      childTask.toString(),
      parentTask.toString()
    ).executeAsOne() != 0L

  private inline fun simpleUpdate(
    uuid: TaskId,
    crossinline updateOperation: (TaskId) -> Unit
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

fun expandTaskIdList(s: String?) = expandDelimitedTaskIdList(s) { TaskId(uuidFrom(it)) }

fun <T> expandDelimitedTaskIdList(str: String?, delim: String = ",", mapper: (String) -> T) =
  str?.splitToSequence(delim)?.map { child -> mapper(child) }?.toSet() ?: emptySet()
