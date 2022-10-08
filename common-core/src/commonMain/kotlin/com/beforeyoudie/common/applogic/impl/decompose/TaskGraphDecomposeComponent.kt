package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class TaskGraphDecomposeComponent(
  override val appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  private val coroutineContext: CoroutineContext,
  private val taskGraphEvents: MutableSharedFlow<TaskGraphEvent>,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  ComponentContext by componentContext {
  override fun createTask(title: String, description: String?, parent: Uuid?) {
    runBlocking(coroutineContext) {
      taskGraphEvents.emit(TaskGraphEvent.CreateTask(title, description, parent))
    }
  }

  override fun deleteTaskAndChildren(uuid: Uuid) {
    runBlocking(coroutineContext) {
      taskGraphEvents.emit(TaskGraphEvent.DeleteTaskAndChildren(uuid))
    }
  }
}
