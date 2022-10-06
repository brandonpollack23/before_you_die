package com.beforeyoudie.common.applogic

/** Interface representing the root applogic component. */
interface IBydRoot {
  sealed class Child {
    data class BydTaskList(val component: IBydGraph) : Child()
    data class BydEditTask(val component: IBydEdit) : Child()
  }
}
