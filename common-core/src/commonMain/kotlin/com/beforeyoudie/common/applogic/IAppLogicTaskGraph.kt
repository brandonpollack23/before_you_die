package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

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
