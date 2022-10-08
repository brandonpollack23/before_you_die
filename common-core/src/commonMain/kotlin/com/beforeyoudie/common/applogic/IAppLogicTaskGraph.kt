package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.benasher44.uuid.Uuid

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IAppLogicTaskGraph

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

/** The state changing operations a TaskGraph coreLogic can do.  I decided on this instead of updating a state on a flow because it makes it easier to manipulate storage all from [IAppLogicRoot]'s implementation. */
interface TaskGraphOperations {
  fun addTask(
    title: String,
    description: String? = null,
    parent: Uuid? = null
  )

  fun deleteTaskAndChildren(uuid: Uuid)
}

