package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * TaskGraph CoreLogic.  This includes the graph view, the list view, etc.
 * [AppLogicTaskGraphConfig] controls how it should be rendered.
 */
abstract class AppLogicTaskGraph(
  val appLogicTaskGraphConfig: AppLogicTaskGraphConfig
) {
  /** See [AppLogicRoot] */
  abstract val coroutineScope: CoroutineScope

  private val mutableTaskGraphEvents: MutableSharedFlow<TaskGraphEvent> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  /**
   * The immutable view on the events emitted by the task graph to be consumed by owning AppLogic
   * components.
   */
  val taskGraphEvents = mutableTaskGraphEvents.asSharedFlow()

  fun createTask(title: String, description: String?, parent: TaskId?) {
    coroutineScope.launch {
      mutableTaskGraphEvents.emit(TaskGraphEvent.CreateTask(title, description, parent))
    }
  }

  fun deleteTaskAndChildren(uuid: TaskId) {
    coroutineScope.launch {
      mutableTaskGraphEvents.emit(TaskGraphEvent.DeleteTaskAndChildren(uuid))
    }
  }

  fun openEdit(uuid: TaskId) {
    coroutineScope.launch {
      mutableTaskGraphEvents.emit(TaskGraphEvent.OpenEdit(uuid))
    }
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
 * These are the events emitted by the reactive stream/flow to be responded to upstream.
 */
sealed interface TaskGraphEvent {
  /** Create a task with the specified parameters. */
  data class CreateTask(val title: String, val description: String?, val parent: TaskId?) :
    TaskGraphEvent

  /** Delete a task with the specified uuid. */
  data class DeleteTaskAndChildren(val taskId: TaskId) : TaskGraphEvent

  data class OpenEdit(val taskId: TaskId) : TaskGraphEvent
}
