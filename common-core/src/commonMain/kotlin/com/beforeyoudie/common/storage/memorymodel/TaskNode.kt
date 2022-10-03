package com.beforeyoudie.common.storage.memorymodel

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

data class TaskNode(
    val id: Uuid = uuid4(),
    val title: String,
    val isComplete: Boolean = false,
    val description: String? = null,
    val parent: Uuid? = null,
    val children: Set<Uuid> = setOf(),
    val blockingTasks: Set<Uuid> = setOf(),
    val blockedTasks: Set<Uuid> = setOf(),
)