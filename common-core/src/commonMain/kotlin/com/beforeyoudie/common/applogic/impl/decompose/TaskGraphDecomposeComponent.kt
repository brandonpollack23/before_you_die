package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraph
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Though it seems at first glance this class isn't really necessary part of the Decompose framework,
 * The [coroutineScopeWithLifecycle] function utilizes the [ComponentContext]'s [LifecycleOwner] so
 * these coroutines are tied to the lifecycle of this component.
 */
class TaskGraphDecomposeComponent(
  appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  coroutineContext: CoroutineContext,
  componentContext: ComponentContext
) :
  AppLogicTaskGraph(appLogicTaskGraphConfig),
  ComponentContext by componentContext {
  override val coroutineScope = coroutineScopeWithLifecycle(coroutineContext)
}
