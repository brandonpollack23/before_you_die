package com.beforeyoudie.common.state

/** The full in memory task graph of all tasks. */
data class TaskGraph(
  val tasks: Set<TaskNode> = emptySet()
)