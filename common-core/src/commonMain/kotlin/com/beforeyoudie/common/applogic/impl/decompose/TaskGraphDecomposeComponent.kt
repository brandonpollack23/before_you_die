package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TaskGraphDecomposeComponent(
  override val appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  private val taskGraphEvents: MutableSharedFlow<TaskGraphEvent>,
  coroutineContext: CoroutineContext,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  ComponentContext by componentContext {
  private val coroutineScope = coroutineScopeWithLifecycle(coroutineContext)
  override fun createTask(title: String, description: String?, parent: Uuid?) {
    coroutineScope.launch {
      taskGraphEvents.emit(TaskGraphEvent.CreateTask(title, description, parent))
    }
  }

  override fun deleteTaskAndChildren(uuid: Uuid) {
    coroutineScope.launch {
      taskGraphEvents.emit(TaskGraphEvent.DeleteTaskAndChildren(uuid))
    }
  }
}
