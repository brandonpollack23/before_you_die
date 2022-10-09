package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// TODO NOW doc all

/**
 * TaskGraph CoreLogic.  This includes the graph view, the list view, etc.
 * [AppLogicTaskGraphConfig] controls how it should be rendered.
 */
abstract class AppLogicTaskGraph(
  val appLogicTaskGraphConfig: AppLogicTaskGraphConfig
) {
  abstract val coroutineScope: CoroutineScope

  private val _taskGraphEvents: MutableSharedFlow<TaskGraphEvent> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  val taskGraphEvents = _taskGraphEvents.asSharedFlow()
  init {
  }

  fun createTask(title: String, description: String?, parent: TaskId?) {
    coroutineScope.launch {
      _taskGraphEvents.emit(TaskGraphEvent.CreateTask(title, description, parent))
    }
  }

  fun deleteTaskAndChildren(uuid: TaskId) {
    coroutineScope.launch {
      _taskGraphEvents.emit(TaskGraphEvent.DeleteTaskAndChildren(uuid))
    }
  }

  // TODO NOW this should open a task child node with navigation using the task graph events flow
  fun openEdit(uuid: TaskId) {
    TODO()
  }
}

/**
 * Task Graph CoreLogic configuration.  This is exposed to the top level so the UI can change based on it.
 *
 * @property viewMode is it a list or a graph view (when supported)
 * @property visibilityMode Are all tasks visible or only non-blocked ones?
 */
@Parcelize
data class AppLogicTaskGraphConfig(
  val viewMode: ViewMode = ViewMode.List,
  val visibilityMode: VisibilityMode = VisibilityMode.AllTasks
) : Parcelable
enum class ViewMode {
  List,
  NodalGraph
}
enum class VisibilityMode {
  AllTasks,
  ActionableTasks
}

/**
 * These are the events emitted by the reactive stream/flow for UI constructed by [createTaskGraphEventsFlow]
 */
sealed interface TaskGraphEvent {
  /** Create a task with the specified parameters. */
  data class CreateTask(val title: String, val description: String?, val parent: TaskId?) :
    TaskGraphEvent

  /** Delete a task with the specified uuid. */
  data class DeleteTaskAndChildren(val taskId: TaskId) : TaskGraphEvent

  data class OpenEdit(val taskId: TaskId) : TaskGraphEvent
}
