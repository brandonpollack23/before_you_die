package com.beforeyoudie.common.applogic

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.beforeyoudie.common.state.TaskNode
import kotlinx.coroutines.flow.MutableStateFlow

// TODO NOW doc

/** Interface representing the root applogic component. */
interface IAppLogicRoot {
  val taskGraphState: MutableStateFlow<Collection<TaskNode>>

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
