package com.beforeyoudie.common.applogic

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IBydGraph {
  val config: BydGraphConfig
}

/**
 * Task Graph CoreLogic configuration.
 *
 * @property viewMode is it a list or a graph view (when supported)
 * @property visibilityMode Are all tasks visible or only non-blocked ones?
 */
data class BydGraphConfig(
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
