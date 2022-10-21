package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** Interface representing the editor logic for a task. */
abstract class AppLogicEdit(val appLogicEditConfig: AppLogicEditConfig) : AppLogicChild() {
  /**
   * The immutable view on the events emitted by the edit task to be consumed by owning AppLogic
   * components.
   */
  val editTaskEvents = mutableTaskEvents.asSharedFlow()

  fun editTitle(newTitle: String) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.EditTitle(appLogicEditConfig.taskNodeId, newTitle))
    }
  }

  fun editDescription(newDescription: String) {
    coroutineScope.launch {
      mutableTaskEvents.emit(
        TaskEvent.EditDescription(
          appLogicEditConfig.taskNodeId,
          newDescription
        )
      )
    }
  }

  fun addChild(newChild: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(
        TaskEvent.SetParentChild(appLogicEditConfig.taskNodeId, newChild)
      )
    }
  }

  fun setParent(newParent: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(
        TaskEvent.SetParentChild(newParent, appLogicEditConfig.taskNodeId)
      )
    }
  }

  fun addBlockingTask(blockingTask: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(
        TaskEvent.AddBlockingTask(appLogicEditConfig.taskNodeId, blockingTask)
      )
    }
  }

  fun addBlockedTask(blockedTask: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(
        TaskEvent.AddBlockedTask(appLogicEditConfig.taskNodeId, blockedTask)
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

