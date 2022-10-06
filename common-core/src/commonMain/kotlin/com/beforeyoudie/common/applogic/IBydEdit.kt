package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskNode

// TODO NOW doc all

/** Interface representing the editor logic for a task. */
interface IBydEdit

data class BydEditConfig(val taskNode: TaskNode)
