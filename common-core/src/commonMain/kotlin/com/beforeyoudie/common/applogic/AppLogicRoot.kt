package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** Interface representing the root applogic component. */
abstract class AppLogicRoot(private val storage: IBydStorage) {
  val logger = getClassLogger()
  abstract val coroutineScope: CoroutineScope

  abstract val appState: MutableStateFlow<AppState>
  // AppState lenses.
  private val taskGraphStateFlow = run {
    val f = MutableStateFlow(appState.value.taskGraph)
    coroutineScope.launch {
      f.collect {
        appState.value = appState.value.copy(taskGraph = it)
      }
    }
    f
  }

  abstract fun onOpenEdit(taskId: TaskId)

  protected fun subscribeToTaskGraphEvents(taskGraphEvents: SharedFlow<TaskGraphEvent>) {
    coroutineScope.launch {
      taskGraphEvents.collect {
        when (it) {
          is TaskGraphEvent.CreateTask -> {
            storage.insertTaskNode(
              TaskId(),
              it.title,
              it.description,
              complete = false
            ).onFailure { error ->
              // TODO ERRORS add failure state (flow) and handle failures?
              logger.e("Failed to insert node! $error")
            }.onSuccess { task ->
              taskGraphStateFlow.value += task
            }
          }

          is TaskGraphEvent.DeleteTaskAndChildren -> storage.removeTaskNodeAndChildren(it.taskId)
          is TaskGraphEvent.OpenEdit -> onOpenEdit(it.taskId)
        }
      }
    }
  }

  sealed class Child {
    data class TaskGraph(val appLogic: AppLogicTaskGraph) : Child()
    data class EditTask(val appLogic: AppLogicEdit) : Child()
  }
}

/**
 * The overall application state, this includes the state of the graph, any ui elements, etc.
 */
data class AppState(val taskGraph: Collection<TaskNode> = emptySet())

// TODO(#9) Deep links add deep link functionality
/** Deep link types for the app. */
sealed interface DeepLink {
  object None : DeepLink
}
