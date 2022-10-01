package com.beforeyoudie.common.storage.memorymodel

import com.benasher44.uuid.Uuid

data class TaskNode(
    val id: Uuid,
    val title: String,
    val description: String?,
    val parent: List<Uuid>,
    val children: List<Uuid>,
    val blockingTasks: List<Uuid>,
    val blockedTasks: List<Uuid>,
)