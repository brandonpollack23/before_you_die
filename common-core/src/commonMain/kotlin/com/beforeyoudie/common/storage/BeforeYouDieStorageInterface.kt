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
    fun getAllTaskNodeInformation(): List<TaskNode>

    fun insertTaskNode(id: Uuid, title: String, description: String?, complete: Boolean)

    fun addChildToTaskNode(parent: Uuid, child: Uuid)
}