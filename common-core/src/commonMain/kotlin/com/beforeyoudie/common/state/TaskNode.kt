package com.beforeyoudie.common.state

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

typealias TaskId = Uuid

/**
 * The in memory representation of a task after reading it from the [com.beforeyoudie.common.storage.IBydStorage]
 *
 * @property id unique UUID of the task
 * @property title the title of the task, required
 * @property isComplete whether this task is completed or not
 * @property description optional extra description/notes of the task
 * @property parent the optional singular parent of a task (for indentation, grouping, deletion)
 * @property children all the children of the task (see parent)
 * @property blockingTasks tasks blocked by this task (that can't be completed or seen in actionable view).
 * @property blockedTasks inverse of blocking, can't be seen in actionable view.
 */
data class TaskNode(
  val id: TaskId = uuid4(),
  val title: String,
  val isComplete: Boolean = false,
  val description: String? = null,
  val parent: Uuid? = null,
  val children: Set<Uuid> = setOf(),
  val blockingTasks: Set<Uuid> = setOf(),
  val blockedTasks: Set<Uuid> = setOf()
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as TaskNode
    if (id != other.id) return false
    return true
  }
}