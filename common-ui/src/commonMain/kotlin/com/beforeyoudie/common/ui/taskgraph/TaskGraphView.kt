@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)

package com.beforeyoudie.common.ui.taskgraph

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.beforeyoudie.common.applogic.AppLogicTaskGraph
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.ViewMode
import com.beforeyoudie.common.resources.MR
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.ui.shared.BydTopAppBar
import com.beforeyoudie.common.ui.shared.DEFAULT_PADDING
import com.beforeyoudie.common.ui.shared.NOTE_CREATION_PADDING
import com.beforeyoudie.common.ui.shared.handleReturnKey
import com.beforeyoudie.common.util.getLocalizedResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TaskGraphView(
  taskGraph: Map<TaskId, TaskNode>,
  isLoading: Boolean,
  appLogicTaskGraph: AppLogicTaskGraph
) {
  val coroutineScope = rememberCoroutineScope()
  val config = appLogicTaskGraph.appLogicTaskGraphConfig
  // Used to grab focus when the sheet expands.
  val newNoteFocusRequester = FocusRequester()
  val scaffoldState = rememberBottomSheetScaffoldState(DrawerState(DrawerValue.Closed))

  BottomSheetScaffold(
    modifier = collapseDrawerClickableModifier(scaffoldState, coroutineScope),
    scaffoldState = scaffoldState,
    sheetPeekHeight = 0.dp,
    topBar = { BydTopAppBar() },
    sheetContent = {
      CollapsableSheetContent(
        newNoteFocusRequester,
        appLogicTaskGraph,
        coroutineScope,
        scaffoldState
      )
    },
    floatingActionButtonPosition = FabPosition.End,
    floatingActionButton = { GraphViewFab(coroutineScope, scaffoldState, newNoteFocusRequester) },
    sheetGesturesEnabled = false
  ) {
    if (isLoading) {
      Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
      ) {
        CircularProgressIndicator(Modifier.padding(DEFAULT_PADDING))
      }
    } else {
      GraphViewMain(config, taskGraph, appLogicTaskGraph)
    }
  }
}

private fun collapseDrawerClickableModifier(
  scaffoldState: BottomSheetScaffoldState,
  coroutineScope: CoroutineScope
) = Modifier.clickable(
  interactionSource = MutableInteractionSource(),
  indication = null
) {
  if (scaffoldState.bottomSheetState.isExpanded) {
    coroutineScope.launch { scaffoldState.bottomSheetState.collapse() }
  }
}

@Composable
private fun CollapsableSheetContent(
  newNoteFocusRequester: FocusRequester,
  appLogicTaskGraph: AppLogicTaskGraph,
  coroutineScope: CoroutineScope,
  scaffoldState: BottomSheetScaffoldState
) {
  AddNoteDialog(
    modifier = Modifier.focusRequester(newNoteFocusRequester),
    onAddNote = { title, description ->
      appLogicTaskGraph.createTask(
        title,
        description,
        null
      )

      coroutineScope.launch { scaffoldState.bottomSheetState.collapse() }
    })
}

@Composable
private fun GraphViewFab(
  coroutineScope: CoroutineScope,
  scaffoldState: BottomSheetScaffoldState,
  newNoteFocusRequester: FocusRequester
) {
  FloatingActionButton(modifier = Modifier.offset((-12).dp, (-12).dp), onClick = {
    coroutineScope.launch {
      if (scaffoldState.bottomSheetState.isExpanded) {
        scaffoldState.bottomSheetState.collapse()
      } else {
        scaffoldState.bottomSheetState.expand()
        newNoteFocusRequester.requestFocus()
      }
    }
  }) {
    Icon(Icons.Filled.Add, getLocalizedResource(MR.strings.add_note))
  }
}

@Composable
private fun GraphViewMain(
  config: AppLogicTaskGraphConfig,
  taskGraph: Map<TaskId, TaskNode>,
  appLogicTaskGraph: AppLogicTaskGraph
) {
  if (config.viewMode == ViewMode.List) {
    val (completedTasks, incompleteTasks) = taskGraph.values.partition { it.isComplete }

    val isCompletedTaskListExpanded = remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    Box(Modifier.fillMaxSize().padding(end = DEFAULT_PADDING, bottom = DEFAULT_PADDING)) {
      LazyColumn(state = lazyListState) {
        for (note in incompleteTasks) {
          item {
            ListViewTaskRow(
              note,
              onCheckedChanged = { isComplete ->
                appLogicTaskGraph.setTaskAndChildrenComplete(
                  note.id,
                  isComplete
                )
              },
              onTapTask = { appLogicTaskGraph.openEdit(note.id) }
            )
          }
        }

        ExpandableCompletedTasks(
          isCompletedTaskListExpanded,
          completedTasks,
          onCheckedChanged = { taskId, isComplete ->
            appLogicTaskGraph.setTaskAndChildrenComplete(taskId, isComplete)
          })
      }

      VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(lazyListState)
      )
    }
  } else {
    Text(getLocalizedResource(MR.strings.unimplemented_view), color = Color.Red)
  }
}

// TODO(#18) Make the completed task dropdown way prettier.
private fun LazyListScope.ExpandableCompletedTasks(
  isCompletedTaskListExpanded: MutableState<Boolean>,
  completedTasks: List<TaskNode>,
  onCheckedChanged: (TaskId, Boolean) -> Unit
) {
  item {
    Spacer(Modifier.size(16.dp))
  }

  item {
    val arrowRotation = if (isCompletedTaskListExpanded.value) 0f else 180f
    Row(
      Modifier
        .clickable(
          onClickLabel = getLocalizedResource(MR.strings.expand_completed_tasks),
          onClick = { isCompletedTaskListExpanded.value = !isCompletedTaskListExpanded.value }
        ).fillMaxWidth()
    ) {
      Box(Modifier.height(32.dp).fillMaxWidth()) {
        Spacer(Modifier.size(16.dp))

        Icon(
          Icons.Default.ArrowDropDown,
          "",
          tint = Color.Gray,
          modifier = Modifier.offset(y = 4.dp).rotate(arrowRotation)
        )
        Column(
          modifier = Modifier.align(Alignment.Center).fillMaxWidth()
        ) {
          Text(
            modifier = Modifier.offset(x = 32.dp),
            text = getLocalizedResource(MR.strings.expand_completed_tasks),
            color = Color.Gray,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.caption
          )
          Divider(
            startIndent = 14.dp,
            color = Color.Gray,
          )
        }
      }
    }
  }

  if (isCompletedTaskListExpanded.value) {
    for (note in completedTasks) {
      CompletedListViewTaskRow(note, onCheckedChanged = { onCheckedChanged(note.id, it) })
    }
  }
}

@Composable
private fun ListViewTaskRow(note: TaskNode, onCheckedChanged: ((Boolean) -> Unit), onTapTask: (() -> Unit)) {
  Column { // Column of Divider and row content
    Box(Modifier.clickable { onTapTask() }) {
      Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Start) {
      Checkbox(checked = note.isComplete, onCheckedChange = onCheckedChanged)

        Column(Modifier.padding(2.dp)) {
          Text(note.title, style = MaterialTheme.typography.h6)
          if (note.description != null) {
            Text(note.description!!, color = Color.Gray, style = MaterialTheme.typography.body1)
          }
        }
        // TODO UI application specific right click to delete, hover swipe to delete gesture
      }
    }

    Divider(thickness = 1.dp, color = Color.Black)
  }
}

private fun LazyListScope.CompletedListViewTaskRow(note: TaskNode, onCheckedChanged: ((Boolean) -> Unit)) {
  item {
    val checkBoxColors =
      CheckboxDefaults.colors(checkedColor = Color.DarkGray, checkmarkColor = Color.Black)

    // TODO UI find a way to scale down
    Row {
      Checkbox(
        checked = note.isComplete,
        onCheckedChange = onCheckedChanged,
        colors = checkBoxColors
      )

      Text(
        note.title,
        modifier = Modifier.align(Alignment.CenterVertically),
        style = MaterialTheme.typography.h6.copy(
          textDecoration = TextDecoration.LineThrough,
          fontStyle = FontStyle.Italic
        ),
        color = Color.Gray
      )
      // TODO UI application specific right click to delete, hover swipe to delete gesture
    }
  }

  item {
    Divider(thickness = 1.dp, color = Color.LightGray)
  }
}

@Composable
private fun AddNoteDialog(onAddNote: (String, String?) -> Unit, modifier: Modifier = Modifier) {
  var currentTitle by remember { mutableStateOf("") }
  var currentDescription by remember { mutableStateOf("") }

  val isAddEnabled = currentTitle.isNotBlank()
  Row(modifier.fillMaxWidth().padding(top = NOTE_CREATION_PADDING).onKeyEvent {
    return@onKeyEvent handleReturnKey(it, isAddEnabled) {
      onAddNote(
        currentTitle,
        currentDescription
      )
    }
  }) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = currentTitle,
        onValueChange = { currentTitle = it },
        maxLines = 2,
        placeholder = { Text(getLocalizedResource(MR.strings.input_title_placeholder)) }
      )
      TextField(
        modifier = Modifier.fillMaxWidth(),
        value = currentDescription,
        onValueChange = { currentDescription = it },
        placeholder = { Text(getLocalizedResource(MR.strings.input_description_placeholder)) }
      )
    }

    Column(modifier = Modifier.align(Alignment.CenterVertically).padding(2.dp)) {
      Button(
        enabled = isAddEnabled,
        onClick = {
          onAddNote(currentTitle.trim(), currentDescription.trim())
          currentTitle = ""
          currentDescription = ""
        }) {
        Icon(
          Icons.Filled.Send,
          contentDescription = getLocalizedResource(MR.strings.new_note_confirm),
        )
      }
    }
  }
}
