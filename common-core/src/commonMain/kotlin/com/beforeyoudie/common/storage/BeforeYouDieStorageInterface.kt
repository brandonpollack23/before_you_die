package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.benasher44.uuid.Uuid

// TODO NOW DOCS

/**
 * Implementation agnostic interface for storage.  At first will only be backed by sqlite, but
 * could be backed by memory, firebase, or whatever.
 */
interface BeforeYouDieStorageInterface {
    val isInMemory: Boolean

    /**
     * Get all the task nodes from the database.
     */
    fun getAllTaskNodeInformation(): List<TaskNode>

    /**
     * Insert a task node with the given information into the database.
     *
     * @returns [Result] of with fail type [InsertionFailure]
     */
    fun insertTaskNode(id: Uuid, title: String, description: String?, complete: Boolean): Result<Unit>

    /**
     * Add a child to a task mode, if you need to reparent use that operation instead.
     *
     * @returns [Result] of with fail type [DuplicateParent]
     */
    fun addChildToTaskNode(parent: Uuid, child: Uuid): Result<Unit>

    /**
     * Add dependency relationship
     *
     * @returns [NonExistantNodeId]
     */
    fun addDependencyRelationship(blockedTask: Uuid, blockingTask: Uuid): Result<Unit>

    // TODO NOW reparent operation
    // TODO NOW remove child, remove dependency relationship, remove node
}