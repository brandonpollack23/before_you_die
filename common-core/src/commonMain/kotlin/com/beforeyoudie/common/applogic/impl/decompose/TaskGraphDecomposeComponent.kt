package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.TaskGraphOperations
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.CoroutineScope

class TaskGraphDecomposeComponent(
  private val appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  private val coroutineScope: CoroutineScope,
  taskGraphOperations: TaskGraphOperations,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  TaskGraphOperations by taskGraphOperations,
  ComponentContext by componentContext
