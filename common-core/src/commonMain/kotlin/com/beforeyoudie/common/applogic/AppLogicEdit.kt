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

  private val mutableEditTaskEvents: MutableSharedFlow<EditTaskEvents> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  /**
   * The immutable view on the events emitted by the edit task to be consumed by owning AppLogic
   * components.
   */
  val editTaskEvents = mutableEditTaskEvents.asSharedFlow()

  fun editTitle(newTitle: String) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(EditTaskEvents.EditTitle(appLogicEditConfig.taskNodeId, newTitle))
    }
  }

  fun editDescription(newDescription: String) {
    coroutineScope.launch {
      mutableEditTaskEvents.emit(
        EditTaskEvents.EditDescription(
          appLogicEditConfig.taskNodeId,
          newDescription
        )
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
sealed interface EditTaskEvents {
  data class EditTitle(val taskId: TaskId, val newTitle: String) : EditTaskEvents
  data class EditDescription(val taskId: TaskId, val newDescription: String) : EditTaskEvents

  // TODO NOW add children, blocking, blockers, change parent
  // TODO NOW make methods and tests for all these
}
