package com.beforeyoudie.common.storage

import com.beforeyoudie.common.storage.memorymodel.TaskNode

/**
 * Implementation agnostic interface for storage.  At first will only be backed by sqlite, but
 * could be backed by memory, firebase, or whatever.
 */
interface BeforeYouDieStorageInterface {
    val isInMemory: Boolean
    fun getAllTaskNodeInformation(): List<TaskNode>
}