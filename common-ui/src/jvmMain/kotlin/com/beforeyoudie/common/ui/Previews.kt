package com.beforeyoudie.common.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.AppLogicTaskGraph
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.AppState
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

val testLoadingAppState = AppState(
  taskGraph = emptyMap(), AppLogicRoot.Child.TaskGraph(
    object : AppLogicTaskGraph(
      AppLogicTaskGraphConfig()
    ) {
      override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    }
  ), isLoading = true
)
@Preview
@Composable
fun AppLoadingPreview() {
  val flow = MutableStateFlow(testLoadingAppState)
  App(flow)
}

val testWithFlatTasks = AppState(
  taskGraph = listOf(
    TaskNode(TaskId(), "Become Like Picard", "The best"),
    TaskNode(TaskId(), "Sit Like Riker", "over the back"),
    TaskNode(TaskId(), "Fight Like Worf", "Kupluh!"),
  ).associateBy { it.id }
  , AppLogicRoot.Child.TaskGraph(
    object : AppLogicTaskGraph(
      AppLogicTaskGraphConfig()
    ) {
      override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    }
  ), isLoading = false
)
@Preview
@Composable
fun GraphFlatTasks() {
  val flow = MutableStateFlow(testWithFlatTasks)
  App(flow)
}
