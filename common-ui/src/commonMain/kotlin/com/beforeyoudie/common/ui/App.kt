package com.beforeyoudie.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.AppState
import com.beforeyoudie.common.ui.edittask.EditTaskView
import com.beforeyoudie.common.ui.shared.handleTab
import com.beforeyoudie.common.ui.taskgraph.TaskGraphView
import kotlinx.coroutines.flow.StateFlow

@Composable
fun App(appState: StateFlow<AppState>) {
  val appLogicRootState = appState.collectAsState()

  val focusManager = LocalFocusManager.current
  Box(Modifier.onPreviewKeyEvent {
    handleTab(it) { focusManager.moveFocus(FocusDirection.Down) }
  }) {
    when (val activeChild = appLogicRootState.value.activeChild) {
      is AppLogicRoot.Child.TaskGraph -> TaskGraphView(
        appLogicRootState.value.taskGraph,
        appLogicRootState.value.isLoading,
        activeChild.appLogic
      )

      is AppLogicRoot.Child.EditTask -> EditTaskView(
        appLogicRootState.value.taskGraph,
        activeChild,
      )
    }
  }
}
