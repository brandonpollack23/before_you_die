package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskIdGenerator
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Interface representing the root applogic component. */
abstract class AppLogicRoot(
  private val storage: IBydStorage,
  private val taskIdGenerator: TaskIdGenerator
) {
  protected val logger = getClassLogger()

  /**
   * The coroutine scope for this root task.  It should be tied to the lifecycle in some way.
   * In Decompose this is done with the Lifecycle, in raw android it is the ViewModelScope, etc.
   *
   * The same is true for all children.
   */
  protected abstract val coroutineScope: CoroutineScope

  /**
   * This is the mutable version of the state.  It is internally mutable only since we are not
   * allowing consumers of the system to mutate it.  All the logic is contained within the
   * [com.beforeyoudie.common.applogic] package and methods on the classes within should be used to
   * manipulate the state of the application.
   */
  protected abstract val mutableAppStateFlow: MutableStateFlow<AppState>

  /**
   * Immutable view onto AppState. They are lazy initialized because this class's coroutine scope and state
   * may not yet be initialized.
   */
  val appStateFlow by lazy { mutableAppStateFlow.asStateFlow() }

  /** Lenses on the appstate.  These make it easier to change subtrees of the overall app state.*/
  private val taskGraphStateFlow by lazy {
    run {
      val f = MutableStateFlow(mutableAppStateFlow.value.taskGraph)
      coroutineScope.launch {
        f.collect { newTaskGraph ->
          mutableAppStateFlow.update { appState -> appState.copy(taskGraph = newTaskGraph) }
        }
      }
      f
    }
  }

  /** Function to be called when an edit view is requested from the task graph. */
  protected abstract fun onOpenEdit(taskId: TaskId)

  /** Use this to subscribe to a child navigable view on task graph events after creation.*/
  protected fun subscribeToTaskGraphEvents(taskGraphEvents: SharedFlow<TaskGraphEvent>) {
    coroutineScope.launch {
      taskGraphEvents.collect {
        when (it) {
          is TaskGraphEvent.CreateTask -> handleCreateTaskEvent(it)
          is TaskGraphEvent.DeleteTaskAndChildren -> handleDeleteTaskAndChildrenEvent(it)
          is TaskGraphEvent.OpenEdit -> onOpenEdit(it.taskId)
        }
      }
    }
  }

  private fun handleCreateTaskEvent(it: TaskGraphEvent.CreateTask) {
    storage.insertTaskNode(
      id = taskIdGenerator.generateTaskId(),
      title = it.title,
      description = it.description,
      complete = false,
      parent = it.parent
    ).onSuccess { task ->
      logger.i("Node ${task.id} inserted, updating in memory state")
      taskGraphStateFlow.update { taskGraph ->
        taskGraph + (task.id to task)
      }
    }.onFailure { error ->
      // TODO ERRORS add failure state (flow) and handle failures?
      logger.e("Failed to insert node! $error")
    }
  }

  private fun handleDeleteTaskAndChildrenEvent(it: TaskGraphEvent.DeleteTaskAndChildren) {
    storage.removeTaskNodeAndChildren(it.taskId)
      .onSuccess { removedNodes ->
        val removedNodesSet = removedNodes.toSet()
        logger.i(
          "Node ${it.taskId} and children (total of ${removedNodes.size}) " +
            "removed, updating in memory state"
        )

        // TODO(#13) Measure this and make it more efficent, potentially via full reload.
        taskGraphStateFlow.update { taskGraph ->
          taskGraph
            .filter { entry -> !removedNodes.contains(entry.key) }
            .mapValues { entry ->
              val taskNode = entry.value
              taskNode.copy(
                blockingTasks = taskNode.blockingTasks - removedNodesSet,
                blockedTasks = taskNode.blockedTasks - removedNodesSet,
                children = taskNode.children - removedNodesSet
              )
            }
        }
      }.onFailure { error ->
        logger.e("Failed to remove tasks and children! $error")
      }
  }

  protected fun subscribeToEditTaskEvents(editTaskEvents: SharedFlow<EditTaskEvents>) {
    coroutineScope.launch {
      editTaskEvents.collect { taskEvent ->
        when (taskEvent) {
          is EditTaskEvents.EditTitle -> handleEditTaskTitle(taskEvent)
          is EditTaskEvents.EditDescription -> handleEditTaskDescription(taskEvent)
        }
      }
    }
  }

  private fun handleEditTaskTitle(taskEvent: EditTaskEvents.EditTitle) {
    storage.updateTaskTitle(taskEvent.taskId, taskEvent.newTitle)
      .onSuccess {
        taskGraphStateFlow.update { taskGraph ->
          val newTaskNode =
            taskGraph[taskEvent.taskId]!!.copy(title = taskEvent.newTitle)
          taskGraph + (taskEvent.taskId to newTaskNode)
        }
      }.onFailure {
        logger.e("Failed to update task title for ${taskEvent.taskId} to ${taskEvent.newTitle}")
      }
  }

  private fun handleEditTaskDescription(taskEvent: EditTaskEvents.EditDescription) {
    storage.updateTaskDescription(taskEvent.taskId, taskEvent.newDescription)
      .onSuccess {
        taskGraphStateFlow.update { taskGraph ->
          val newTaskNode =
            taskGraph[taskEvent.taskId]!!.copy(description = taskEvent.newDescription)
          taskGraph + (taskEvent.taskId to newTaskNode)
        }
      }.onFailure {
        logger.e(
          "Failed to update task description for ${taskEvent.taskId} to ${taskEvent.newDescription}"
        )
      }
  }

  /** All possible children and their configurations, to be used on navigation for construction.*/
  sealed class Child {
    /** A taskgraph/list view.*/
    data class TaskGraph(val appLogic: AppLogicTaskGraph) : Child()

    /** A task edit view.*/
    data class EditTask(val appLogic: AppLogicEdit) : Child()
  }
}

/**
 * The overall application state, this includes the state of the graph, any ui elements, etc.
 */
data class AppState(
  val taskGraph: Map<TaskId, TaskNode> = emptyMap(),
  val activeChild: AppLogicRoot.Child,
  val isLoading: Boolean = true
)

// TODO(#9) Deep links add deep link functionality
/** Deep link types for the app. */
sealed interface DeepLink {
  object None : DeepLink
}
