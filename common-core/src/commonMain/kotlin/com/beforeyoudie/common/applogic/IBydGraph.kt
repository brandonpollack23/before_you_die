package com.beforeyoudie.common.applogic

/** Interface representing any task graph app logic (this includes list, filtered lists, and graph). */
interface IBydGraph {
  val config: BydGraphConfig
}

data class BydGraphConfig(
  val viewMode: ViewMode,
  val visibilityMode: VisibilityMode,
)

enum class VisibilityMode {
  AllTasks,
  ActionableTasks,
}

enum class ViewMode {
  List,
  NodalGraph
}
