package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.launch

/**
 * TaskGraph CoreLogic.  This includes the graph view, the list view, etc.
 * [AppLogicTaskGraphConfig] controls how it should be rendered.
 */
abstract class AppLogicTaskGraph(
  val appLogicTaskGraphConfig: AppLogicTaskGraphConfig
) : AppLogicChild() {
  protected val logger = getClassLogger()

  override fun onBackPressed() {
    logger.e("Should not press back from Graph view, ignoring...")
  }

  fun createTask(title: String, description: String?, parent: TaskId?) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.CreateTask(title, description, parent))
    }
  }

  fun deleteTaskAndChildren(uuid: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.DeleteTaskAndChildren(uuid))
    }
  }

  fun setTaskAndChildrenComplete(uuid: TaskId, isComplete: Boolean) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.SetTaskAndChildrenComplete(uuid, isComplete))
    }
  }

  fun openEdit(uuid: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.OpenEdit(uuid))
    }
  }

  fun setParentChildRelation(parent: TaskId, child: TaskId) {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.SetParentChild(parent, child))
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