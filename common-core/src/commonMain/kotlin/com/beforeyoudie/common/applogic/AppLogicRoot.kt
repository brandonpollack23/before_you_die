package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Interface representing the root applogic component. */
abstract class AppLogicRoot(private val storage: IBydStorage) {
  protected val logger = getClassLogger()
  abstract val coroutineScope: CoroutineScope

  abstract val _appState: MutableStateFlow<AppState>

  // AppState and lenses. They are lazy initialized because this class's coroutine scope and state may not yet be initialized.
  val appState by lazy { _appState.asStateFlow() }
  private val taskGraphStateFlow by lazy {
    run {
      val f = MutableStateFlow(_appState.value.taskGraph)
      coroutineScope.launch {
        f.collect {
          _appState.value = _appState.value.copy(taskGraph = it)
        }
      }
      f
    }
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
              logger.i("Node ${task.id} inserted, updating in memory state")
              taskGraphStateFlow.value += task
            }
          }

          is TaskGraphEvent.DeleteTaskAndChildren -> {
            storage.removeTaskNodeAndChildren(it.taskId).onFailure { error ->
              logger.e("Failed to remove tasks and children! $error")
            }.onSuccess { removedNodes ->
              logger.i(
                "Node ${it.taskId} and children (total of ${removedNodes.size}) " +
                  "removed, updating in memory state"
              )
              taskGraphStateFlow.value =
                taskGraphStateFlow.value.filter { taskNode -> !removedNodes.contains(taskNode.id) }
            }
          }

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
