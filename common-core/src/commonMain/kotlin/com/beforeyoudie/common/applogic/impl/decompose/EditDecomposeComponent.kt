package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicEdit
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import kotlin.coroutines.CoroutineContext

/** This is the Decompose implementation of the corelogic for the edit views of a note. */
class EditDecomposeComponent(
  appLogicEditConfig: AppLogicEditConfig,
  parentCoroutineContext: CoroutineContext,
  componentContext: ComponentContext
) : AppLogicEdit(appLogicEditConfig),
  ComponentContext by componentContext {
  override val coroutineScope = coroutineScopeWithLifecycle(parentCoroutineContext)
}
