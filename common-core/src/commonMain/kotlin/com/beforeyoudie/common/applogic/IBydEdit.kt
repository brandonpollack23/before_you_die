package com.beforeyoudie.common.applogic

import com.beforeyoudie.common.state.TaskNode

/** Interface representing the editor logic for a task. */
interface IBydEdit

/**
 * Configuration for the edit CoreLogic, such as waht task node to edit, if certain options are
 * enabled (markdown stretch goal for example).
*/
data class BydEditConfig(val taskNode: TaskNode)
