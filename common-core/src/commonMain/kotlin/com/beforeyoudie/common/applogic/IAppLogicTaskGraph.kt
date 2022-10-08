package com.beforeyoudie.common.applogic

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

// TODO NOW doc all

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IAppLogicTaskGraph {
  val appLogicTaskGraphConfig: AppLogicTaskGraphConfig

  fun createTask(title: String, description: String?, parent: TaskId?)

  fun deleteTaskAndChildren(uuid: TaskId)

  fun openEdit(uuid: TaskId)
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
  data class DeleteTaskAndChildren(val uuid: TaskId) : TaskGraphEvent
}

/**
 * Sets up a flow for the task graph CoreLogic. The constructor of this flow passes in flows (generally lenses on its own state) to be reacted to accordingly.
 */
fun createTaskGraphEventsFlow(
  storage: IBydStorage,
  taskNodes: MutableStateFlow<Collection<TaskNode>>,
  logger: Logger
): MutableSharedFlow<TaskGraphEvent> {
  val events = MutableSharedFlow<TaskGraphEvent>()
  events.onEach {
    when (it) {
      is TaskGraphEvent.CreateTask -> {
        storage.insertTaskNode(
          TaskId(uuid4()),
          it.title,
          it.description,
          complete = false
        ).onFailure { error ->
          // TODO ERRORS add failure state (flow) and handle failures?
          logger.e("Failed to insert node! $error")
        }.onSuccess { task ->
          taskNodes.value += task
        }
      }

      is TaskGraphEvent.DeleteTaskAndChildren -> storage.removeTaskNodeAndChildren(it.uuid)
    }
  }
  return events
}
