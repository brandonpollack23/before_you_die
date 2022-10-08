package com.beforeyoudie.common.applogic

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.benasher44.uuid.Uuid

/** Interface representing the editor logic for a task. */
interface IAppLogicEdit

/**
 * Configuration for the edit CoreLogic, such as waht task node to edit, if certain options are
 * enabled ().
*/
@Parcelize
data class AppLogicEditConfig(val taskNodeId: Uuid) : Parcelable
