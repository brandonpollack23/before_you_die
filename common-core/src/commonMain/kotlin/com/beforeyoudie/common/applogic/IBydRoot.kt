package com.beforeyoudie.common.applogic

// TODO NOW doc

/** Interface representing the root applogic component. */
interface IBydRoot {
  sealed class Child {
    data class TaskGraph(val component: IAppLogicTaskGraph) : Child()
    data class EditTask(val component: IAppLogicEdit) : Child()
  }
}

// TODO(#9) Deep links add deep link functionality
/** Deep link types for the app. */
sealed interface DeepLink {
  object None: DeepLink
}