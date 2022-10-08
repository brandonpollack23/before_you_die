package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskNode
import kotlinx.coroutines.flow.MutableStateFlow

// TODO NOW doc

/** Interface representing the root applogic component. */
interface IAppLogicRoot {
  val appState: MutableStateFlow<AppState>

  sealed class Child {
    data class TaskGraph(val appLogic: IAppLogicTaskGraph) : Child()
    data class EditTask(val appLogic: IAppLogicEdit) : Child()
  }
}

// TODO(#9) Deep links add deep link functionality
/** Deep link types for the app. */
sealed interface DeepLink {
  object None : DeepLink
}

data class AppState(val taskGraph: Collection<TaskNode> = emptySet())
