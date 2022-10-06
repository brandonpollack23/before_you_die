package com.beforeyoudie.common.applogic

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IBydGraph {
  val config: BydGraphConfig
}

// TODO NOW create issue to add which list for when that feature is added

// TODO NOW add documentation to all below
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
