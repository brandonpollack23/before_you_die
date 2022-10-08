package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.beforeyoudie.common.state.TaskId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * TaskGraph components.  This includes the graph view, the list view, etc.
 * [AppLogicTaskGraphConfig] controls how it should be rendered.
 *
 * Though it seems at first glance this class isn't really necessary part of the Decompose framework,
 * The [coroutineScopeWithLifecycle] function utilizes the [ComponentContext]'s [LifecycleOwner] so
 * these coroutines are tied to the lifecycle of this component.
 */
class TaskGraphDecomposeComponent(
  override val appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
  private val taskGraphEvents: MutableSharedFlow<TaskGraphEvent>,
  coroutineContext: CoroutineContext,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  ComponentContext by componentContext {
  private val coroutineScope = coroutineScopeWithLifecycle(coroutineContext)
  override fun createTask(title: String, description: String?, parent: TaskId?) {
    coroutineScope.launch {
      taskGraphEvents.emit(TaskGraphEvent.CreateTask(title, description, parent))
    }
  }

  override fun deleteTaskAndChildren(uuid: TaskId) {
    coroutineScope.launch {
      taskGraphEvents.emit(TaskGraphEvent.DeleteTaskAndChildren(uuid))
    }
  }

  // TODO NOW this should open a task child node with navigation using the task graph events flow
  override fun openEdit(uuid: TaskId) {
    TODO("Not yet implemented")
  }
}
