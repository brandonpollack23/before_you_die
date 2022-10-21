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
  protected abstract fun onOpenEdit(event: TaskEvent.OpenEdit)

  /** To be called on app back navigation. */
  protected abstract fun onBack()

  /** Use this to subscribe to a child navigable view on task graph events after creation.*/
  protected fun subscribeToTaskEvents(taskGraphEvents: SharedFlow<TaskEvent>) {
    coroutineScope.launch {
      taskGraphEvents.collect {
        when (it) {
          is TaskEvent.CreateTask -> handleCreateTaskEvent(it)
          is TaskEvent.DeleteTaskAndChildren -> handleDeleteTaskAndChildrenEvent(it)
          is TaskEvent.SetTaskAndChildrenComplete -> handleSetTaskAndChildrenComplete(it)
          is TaskEvent.OpenEdit -> onOpenEdit(it)
          is TaskEvent.SetParentChild -> onSetParentChild(it)

          is TaskEvent.EditTitle -> handleEditTaskTitle(it)
          is TaskEvent.EditDescription -> handleEditTaskDescription(it)
          is TaskEvent.AddBlockingTask -> handleAddBlockingTask(it)
          is TaskEvent.AddBlockedTask -> handleAddBlockedTask(it)
          TaskEvent.Back -> onBack()
        }
      }
    }
  }

  private fun handleCreateTaskEvent(it: TaskEvent.CreateTask) {
    storage.insertTaskNode(
      id = taskIdGenerator.generateTaskId(),
      title = it.title,
      description = it.description,
      complete = false,
      parent = it.parent
    ).onSuccess { task ->
      logger.i("Node ${task.id} inserted, updating in memory state")
      taskGraphStateFlow.update { taskGraph ->
        val newParent = it.parent?.let { p ->
          val parent = taskGraph[p]!!
          parent.copy(children = parent.children + task.id)
        }

        val tasksToAdd = mutableListOf(task.id to task)
        if (newParent != null) tasksToAdd += newParent.id to newParent

        taskGraph + tasksToAdd
      }
    }.onFailure {
      logger.e("Failed to insert node! $it")
    }
  }

  private fun handleDeleteTaskAndChildrenEvent(it: TaskEvent.DeleteTaskAndChildren) {
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
      }.onFailure {
        logger.e("Failed to remove tasks and children! $it")
      }
  }

  private fun handleSetTaskAndChildrenComplete(it: TaskEvent.SetTaskAndChildrenComplete) {
    if (it.isComplete) {
      storage.markTaskAndChildrenComplete(it.taskId)
    } else {
      storage.markTaskAndChildrenIncomplete(it.taskId)
    }.onSuccess { completionNodes ->
      logger.i(
        "Node ${it.taskId} and children (total of ${completionNodes.size}) " +
          "set completion to: ${it.isComplete}, updating in memory state"
      )

      // TODO(#13) Measure this and make it more efficent, potentially via full reload.
      taskGraphStateFlow.update { taskGraph ->
        // Keep all values but overwrite changed ones.
        taskGraph + taskGraph
          .filter { entry -> completionNodes.contains(entry.key) }
          .mapValues { entry -> entry.value.copy(isComplete = it.isComplete) }
      }
    }.onFailure {
      logger.e("Failed to mark tasks and children complete! $it")
    }
  }

  private fun handleEditTaskTitle(taskEvent: TaskEvent.EditTitle) {
    storage.updateTaskTitle(taskEvent.taskId, taskEvent.newTitle)
      .onSuccess {
        taskGraphStateFlow.update { taskGraph ->
          val newTaskNode =
            taskGraph[taskEvent.taskId]!!.copy(title = taskEvent.newTitle)
          taskGraph + (taskEvent.taskId to newTaskNode)
        }
      }.onFailure {
        logger.e(
          "Failed to update task title for ${taskEvent.taskId} to ${taskEvent.newTitle}: $it"
        )
      }
  }

  private fun handleEditTaskDescription(taskEvent: TaskEvent.EditDescription) {
    storage.updateTaskDescription(taskEvent.taskId, taskEvent.newDescription)
      .onSuccess {
        taskGraphStateFlow.update { taskGraph ->
          val newTaskNode =
            taskGraph[taskEvent.taskId]!!.copy(description = taskEvent.newDescription)
          taskGraph + (taskEvent.taskId to newTaskNode)
        }
      }.onFailure {
        logger.e(
          "Failed to update task description for " +
            "${taskEvent.taskId} to ${taskEvent.newDescription}: $it"
        )
      }
  }

  private fun onSetParentChild(setParentChildEvent: TaskEvent.SetParentChild) {
    handleSetParent(setParentChildEvent.parent, setParentChildEvent.child)
  }

  private fun handleSetParent(parent: TaskId, child: TaskId) {
    val childToUpdate = appStateFlow.value.taskGraph[child]
    if (childToUpdate == null) {
      logger.e { "No such task $child to update parent to $parent" }
    }

    val parentToUpdate = appStateFlow.value.taskGraph[parent]
    if (parentToUpdate == null) {
      logger.e { "No such parent $parent to update with child $child" }
    }

    if (childToUpdate == null || parentToUpdate == null) return

    addChildHelper(parentToUpdate, childToUpdate)
  }

  private fun handleAddBlockingTask(taskEvent: TaskEvent.AddBlockingTask) {
    val blockedTask = appStateFlow.value.taskGraph[taskEvent.taskId]
    if (blockedTask == null) {
      logger.e {
        "No such task ${taskEvent.taskId} to add blocking task to to ${taskEvent.blockingTask}"
      }
    }

    val blockingTask = appStateFlow.value.taskGraph[taskEvent.blockingTask]
    if (blockedTask == null) {
      logger.e { "No such task ${taskEvent.blockingTask} to be blocked by ${taskEvent.taskId}" }
    }

    if (blockedTask == null || blockingTask == null) return

    addTaskDependencyHelper(blockingTask, blockedTask)
  }

  private fun handleAddBlockedTask(taskEvent: TaskEvent.AddBlockedTask) {
    val blockedTask = appStateFlow.value.taskGraph[taskEvent.blockedTask]
    if (blockedTask == null) {
      logger.e { "No such task ${taskEvent.taskId} to add blocking task to to ${taskEvent.taskId}" }
    }

    val blockingTask = appStateFlow.value.taskGraph[taskEvent.taskId]
    if (blockedTask == null) {
      logger.e { "No such task ${taskEvent.taskId} to be blocked by ${taskEvent.taskId}" }
    }

    if (blockedTask == null || blockingTask == null) return

    addTaskDependencyHelper(blockingTask, blockedTask)
  }

  private fun addChildHelper(parentToUpdate: TaskNode, childToUpdate: TaskNode) {
    if (parentToUpdate.children.contains(childToUpdate.id)) {
      // Reparent operation
      storage.reparentChildToTaskNode(parentToUpdate.id, childToUpdate.id)
    } else {
      // First parenting
      storage.addChildToTaskNode(parentToUpdate.id, childToUpdate.id)
    }.onSuccess {
      taskGraphStateFlow.update { taskGraph ->
        val updatedChild = childToUpdate.id to childToUpdate.copy(parent = parentToUpdate.id)
        val parentsNewChildren = parentToUpdate.children + childToUpdate.id
        val updatedNewParent = parentToUpdate.id to parentToUpdate.copy(
          children = parentsNewChildren
        )
        val updatedOldParent = childToUpdate.parent?.let { p ->
          val oldParent = taskGraph[p]!!
          val newChildren = oldParent.children - childToUpdate.id
          oldParent.copy(children = newChildren)
        }

        val tasksToAdd = mutableListOf(updatedChild, updatedNewParent)
        if (updatedOldParent != null) tasksToAdd += updatedOldParent.id to updatedOldParent

        taskGraph + tasksToAdd
      }
    }.onFailure {
      logger.e { "Failure updating parent child relationship: $it" }
    }
  }

  private fun addTaskDependencyHelper(blockingTask: TaskNode, blockedTask: TaskNode) {
    storage.addDependencyRelationship(blockingTask.id, blockedTask.id).onSuccess {
      val blockedTaskBlockingTasks = blockedTask.blockingTasks + blockingTask.id
      val updatedBlockedTask =
        blockedTask.id to blockedTask.copy(blockingTasks = blockedTaskBlockingTasks)

      val blockingTaskBlockedTasks = blockingTask.blockedTasks + blockedTask.id
      val updatedBlockingTask =
        blockingTask.id to blockingTask.copy(blockedTasks = blockingTaskBlockedTasks)

      taskGraphStateFlow.update { taskGraph ->
        taskGraph + listOf(updatedBlockedTask, updatedBlockingTask)
      }
    }.onFailure {
      logger.e { "Failure updating dependency relationship: $it" }
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

// TODO (#14) Add state for errors and set it when there are issues doing any of the possible actions from the child event streams.
/**
 * The overall application state, this includes the state of the graph, any ui elements, etc.
 */
data class AppState(
  val taskGraph: Map<TaskId, TaskNode> = emptyMap(),
  val activeChild: AppLogicRoot.Child,
  val isLoading: Boolean = true
)

/**
 * These are the events emitted by the reactive stream/flow to be responded to upstream.
 */
sealed interface TaskEvent {
  /** Create a task with the specified parameters. */
  data class CreateTask(val title: String, val description: String?, val parent: TaskId?) :
    TaskEvent

  /** Delete a task with the specified uuid. */
  data class DeleteTaskAndChildren(val taskId: TaskId) : TaskEvent
  data class SetTaskAndChildrenComplete(val taskId: TaskId, val isComplete: Boolean) : TaskEvent
  data class OpenEdit(val taskId: TaskId) : TaskEvent
  data class SetParentChild(val parent: TaskId, val child: TaskId) : TaskEvent
  data class EditTitle(val taskId: TaskId, val newTitle: String) : TaskEvent
  data class EditDescription(val taskId: TaskId, val newDescription: String) : TaskEvent
  data class AddBlockingTask(val taskId: TaskId, val blockingTask: TaskId) : TaskEvent
  data class AddBlockedTask(val taskId: TaskId, val blockedTask: TaskId) : TaskEvent
  object Back : TaskEvent
}

// TODO(#9) Deep links add deep link functionality
/** Deep link types for the app. */
sealed interface DeepLink {
  object None : DeepLink
}
