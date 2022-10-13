package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** Interface representing the editor logic for a task. */
abstract class AppLogicEdit(val appLogicEditConfig: AppLogicEditConfig) {
  abstract val coroutineScope: CoroutineScope

  private val mutableEditTaskEvents: MutableSharedFlow<EditTaskEvent> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  /**
   * The immutable view on the events emitted by the edit task to be consumed by owning AppLogic
   * components.
   */
  val editTaskEvents = mutableEditTaskEvents.asSharedFlow()

  fun editTitle(newTitle: String) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(EditTaskEvent.EditTitle(appLogicEditConfig.taskNodeId, newTitle))
    }
  }

  fun editDescription(newDescription: String) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvent.EditDescription(
          appLogicEditConfig.taskNodeId,
          newDescription
        )
      )
    }
  }

  fun addChild(newChild: TaskId) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvent.AddChild(appLogicEditConfig.taskNodeId, newChild)
      )
    }
  }

  fun setParent(newParent: TaskId) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvent.SetParent(appLogicEditConfig.taskNodeId, newParent)
      )
    }
  }

  fun addBlockingTask(blockingTask: TaskId) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvent.AddBlockingTask(appLogicEditConfig.taskNodeId, blockingTask)
      )
    }
  }

  fun addBlockedTask(blockedTask: TaskId) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvent.AddBlockedTask(appLogicEditConfig.taskNodeId, blockedTask)
      )
    }
  }
}

/**
 * Configuration for the edit CoreLogic, such as what task node to edit, if certain options are
 * enabled ().
*/
@Parcelize
data class AppLogicEditConfig(val taskNodeId: TaskId) : Parcelable

/**
 * These are the events emitted by the reactive stream/flow to be responded to upstream.
 */
sealed interface EditTaskEvent {
  data class EditTitle(val taskId: TaskId, val newTitle: String) : EditTaskEvent
  data class EditDescription(val taskId: TaskId, val newDescription: String) : EditTaskEvent

  // TODO NOW make tests for all these
  data class AddChild(val taskId: TaskId, val newChild: TaskId) : EditTaskEvent
  data class SetParent(val taskId: TaskId, val newParent: TaskId) : EditTaskEvent
  data class AddBlockingTask(val taskId: TaskId, val blockingTask: TaskId) : EditTaskEvent
  data class AddBlockedTask(val taskId: TaskId, val blockedTask: TaskId) : EditTaskEvent
}
