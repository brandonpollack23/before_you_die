package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.state.TaskId

/** Interface representing the editor logic for a task. */
abstract class AppLogicEdit(val appLogicEditConfig: AppLogicEditConfig)

/**
 * Configuration for the edit CoreLogic, such as waht task node to edit, if certain options are
 * enabled ().
*/
@Parcelize
data class AppLogicEditConfig(val taskNodeId: TaskId) : Parcelable
