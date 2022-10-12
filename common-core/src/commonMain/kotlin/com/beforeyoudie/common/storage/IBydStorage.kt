package com.beforeyoudie.common.storage

import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode

// TODO(#2) STORAGE convenience of deleting a parent and reparenting children one level up.
//  Could be useful for deeper lists

/**
 * Implementation agnostic interface for storage.  At first will only be backed by sqlite, but
 * could be backed by memory, firebase, or whatever.
 */
interface IBydStorage {
  val isInMemory: Boolean

  /**
   * Get all the task nodes from the database.
   */
  fun selectAllTaskNodeInformation(): Map<TaskId, TaskNode>

  /**
   * Returns only task nodes who's direct blockers are complete
   */
  fun selectAllActionableTaskNodeInformation(): Map<TaskId, TaskNode>

  /**
   * Insert a task node with the given information into the database.
   *
   * @returns [Result] of with fail type [com.beforeyoudie.common.util.BYDFailure.InsertionFailure]
   */
  fun insertTaskNode(
    id: TaskId,
    title: String,
    description: String? = null,
    parent: TaskId? = null,
    complete: Boolean = false
  ): Result<TaskNode>

  /** Mark a task as done, returning an error if it doesn't exist. */
  fun markComplete(uuid: TaskId): Result<Unit>

  /** Mark a task as incomplete, returning an error if it doesn't exist. */
  fun markIncomplete(uuid: TaskId): Result<Unit>

  /**
   * Add a child to a task mode, if you need to reparent use that operation instead.
   * I figured making them seperate would catch any bugs where you try to assign a parent to
   * existing node and losing original parent on accident because of weird state.
   *
   * @returns [Result] of with fail type [com.beforeyoudie.common.util.BYDFailure.DuplicateParent]
   */
  fun addChildToTaskNode(parent: TaskId, child: TaskId): Result<Unit>

  /**
   * See [addChildToTaskNode], this is like that but only works on children that already have a
   * parent and moves them
   */
  fun reparentChildToTaskNode(newParent: TaskId, child: TaskId): Result<Unit>

  /**
   * Add dependency relationship
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun addDependencyRelationship(blockingTask: TaskId, blockedTask: TaskId): Result<Unit>

  /**
   * Update title
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun updateTaskTitle(uuid: TaskId, title: String): Result<Unit>

  /**
   * Update description
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun updateTaskDescription(uuid: TaskId, description: String?): Result<Unit>

  /**
   * Removes a given task node along with all relationships.
   *
   * @returns the node ids removed or [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun removeTaskNodeAndChildren(uuid: TaskId): Result<Collection<TaskId>>

  /**
   * Remove a depedency relationship between two nodes
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId] or [com.beforeyoudie.common.util.BYDFailure.NoSuchDependencyRelationship]
   */
  fun removeDependencyRelationship(blockingTask: TaskId, blockedTask: TaskId): Result<Unit>
}
