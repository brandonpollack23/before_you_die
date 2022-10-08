package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

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

/**
 * The overall application state, this includes the state of the graph, any ui elements, etc.
 */
data class AppState(val taskGraph: Collection<TaskNode> = emptySet()) {
  companion object {
    fun createTaskGraphStateFlow(appStateFlow: MutableStateFlow<AppState>): MutableStateFlow<Collection<TaskNode>>{
      val f = MutableStateFlow(appStateFlow.value.taskGraph)
      f.onEach {
        appStateFlow.value = appStateFlow.value.copy(taskGraph = it)
      }
      return f
    }
  }
}
