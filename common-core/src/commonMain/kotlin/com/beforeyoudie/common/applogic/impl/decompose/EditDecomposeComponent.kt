package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicEdit
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.state.TaskNode
import kotlin.coroutines.CoroutineContext

/** This is the Decompose implementation of the corelogic for the edit views of a note. */
class EditDecomposeComponent(
  appLogicEditConfig: AppLogicEditConfig,
  taskNode: TaskNode,
  parentCoroutineContext: CoroutineContext,
  componentContext: ComponentContext
) : AppLogicEdit(appLogicEditConfig, taskNode),
  ComponentContext by componentContext {
  override val coroutineScope = coroutineScopeWithLifecycle(parentCoroutineContext)
}
