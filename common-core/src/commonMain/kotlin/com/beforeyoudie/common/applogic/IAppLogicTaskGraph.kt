package com.beforeyoudie.common.applogic

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

// TODO NOW doc and discuss why flows and not passing around interfaces

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IAppLogicTaskGraph {
  val appLogicTaskGraphConfig: AppLogicTaskGraphConfig

  fun createTask(title: String, description: String?, parent: Uuid?)

  fun deleteTaskAndChildren(uuid: Uuid)
}

/**
 * Task Graph CoreLogic configuration.
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

sealed interface TaskGraphEvent {
  data class CreateTask(val title: String, val description: String?, val parent: Uuid?) :
    TaskGraphEvent

  data class DeleteTaskAndChildren(val uuid: Uuid) : TaskGraphEvent
}

fun createTaskGraphEventsFlow(
  storage: IBydStorage,
  taskNodes: MutableStateFlow<Collection<TaskNode>>,
  logger: Logger
): MutableSharedFlow<TaskGraphEvent> {
  val events = MutableSharedFlow<TaskGraphEvent>()
  events.onEach {
    when (it) {
      is TaskGraphEvent.CreateTask -> {
        val result = storage.insertTaskNode(
          uuid4(),
          it.title,
          it.description,
          false
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
