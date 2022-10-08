package com.beforeyoudie.common.storage

import com.beforeyoudie.common.state.TaskNode
import com.benasher44.uuid.Uuid

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
  fun selectAllTaskNodeInformation(): Collection<TaskNode>

  /**
   * Returns only task nodes who's direct blockers are complete
   */
  fun selectAllActionableTaskNodeInformation(): Collection<TaskNode>

  /**
   * Insert a task node with the given information into the database.
   *
   * @returns [Result] of with fail type [com.beforeyoudie.common.util.BYDFailure.InsertionFailure]
   */
  fun insertTaskNode(
    id: Uuid,
    title: String,
    description: String?,
    complete: Boolean
  ): Result<Unit>

  /** Mark a task as done, returning an error if it doesn't exist. */
  fun markComplete(uuid: Uuid): Result<Unit>

  /** Mark a task as incomplete, returning an error if it doesn't exist. */
  fun markIncomplete(uuid: Uuid): Result<Unit>

  /**
   * Add a child to a task mode, if you need to reparent use that operation instead.
   * I figured making them seperate would catch any bugs where you try to assign a parent to
   * existing node and losing original parent on accident because of weird state.
   *
   * @returns [Result] of with fail type [com.beforeyoudie.common.util.BYDFailure.DuplicateParent]
   */
  fun addChildToTaskNode(parent: Uuid, child: Uuid): Result<Unit>

  /**
   * See [addChildToTaskNode], this is like that but only works on children that already have a
   * parent and moves them
   */
  fun reparentChildToTaskNode(newParent: Uuid, child: Uuid): Result<Unit>

  /**
   * Add dependency relationship
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun addDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit>

  /**
   * Update title
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun updateTaskTitle(uuid: Uuid, title: String): Result<Unit>

  /**
   * Update description
   *
   * @returns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun updateTaskDescription(uuid: Uuid, description: String?): Result<Unit>

  /**
   * Removes a given task node along with all relationships.
   *
   * @retuns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId]
   */
  fun removeTaskNodeAndChildren(uuid: Uuid): Result<Unit>

  /**
   * Remove a depedency relationship between two nodes
   *
   * @retuns [com.beforeyoudie.common.util.BYDFailure.NonExistentNodeId] or [com.beforeyoudie.common.util.BYDFailure.NoSuchDependencyRelationship]
   */
  fun removeDependencyRelationship(blockingTask: Uuid, blockedTask: Uuid): Result<Unit>
}
