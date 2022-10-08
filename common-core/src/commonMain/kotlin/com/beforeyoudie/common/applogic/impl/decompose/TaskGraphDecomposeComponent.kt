package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.state.TaskNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class TaskGraphDecomposeComponent(
  private val taskGraph: MutableStateFlow<Collection<TaskNode>>,
  private val appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  private val coroutineScope: CoroutineScope,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  ComponentContext by componentContext
