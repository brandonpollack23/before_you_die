package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

fun ComponentContext.coroutineScopeWithLifecycle(coroutineContext: CoroutineContext): CoroutineScope {
  val scope = CoroutineScope(coroutineContext) + Job()
  lifecycle.doOnDestroy(scope::cancel)
  return scope
}