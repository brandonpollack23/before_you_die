package com.beforeyoudie.common.applogic

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IAppLogicTaskGraph {
  val config: AppLogicTaskGraphConfig
}

/**
 * Task Graph CoreLogic configuration.
 *
 * @property viewMode is it a list or a graph view (when supported)
 * @property visibilityMode Are all tasks visible or only non-blocked ones?
 */
data class AppLogicTaskGraphConfig(
  val viewMode: ViewMode = ViewMode.List,
  val visibilityMode: VisibilityMode = VisibilityMode.AllTasks
)

enum class ViewMode {
  List,
  NodalGraph
}

enum class VisibilityMode {
  AllTasks,
  ActionableTasks
}
