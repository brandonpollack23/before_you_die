package com.beforeyoudie.common.ui.edittask

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.resources.MR
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.ui.shared.BydTopAppBar
import com.beforeyoudie.common.util.getLocalizedResource

@Composable
fun EditTaskView(taskGraph: Map<TaskId, TaskNode>, activeChild: AppLogicRoot.Child.EditTask) {
  val editAppLogic = activeChild.appLogic
  val taskId = editAppLogic.appLogicEditConfig.taskNodeId
  val taskNode = taskGraph[taskId]!!
  val parentTaskNode = taskNode.parent?.let { taskGraph[it]!! }

  Scaffold(topBar = { BydTopAppBar(onBackPressed = editAppLogic::onBackPressed) }, bottomBar = {}) {
    Column {
      Text("${getLocalizedResource(MR.strings.edit_title_field)}:")
      Row {
        Spacer(Modifier.size(4.dp))
        TextField(value = taskNode.title, onValueChange = { editAppLogic.editTitle(it) })
      }
      Spacer(Modifier.size(2.dp))

      Text("${getLocalizedResource(MR.strings.edit_description_field)}:")
      Row {
        Spacer(Modifier.size(4.dp))
        TextField(
          value = taskNode.description ?: "",
          onValueChange = { editAppLogic.editDescription(it) })
      }
      Spacer(Modifier.size(2.dp))

      Text("${getLocalizedResource(MR.strings.edit_parent_field)}:")
      // TODO SEARCH allow reparenting with search functionality
      Row {
        Spacer(Modifier.size(4.dp))
        Text(parentTaskNode?.title ?: "")
      }
      Spacer(Modifier.size(2.dp))

      Text("${getLocalizedResource(MR.strings.edit_children_field)}:")
      EditViewSublist(taskNode, taskGraph, taskNode.children)
      Spacer(Modifier.size(2.dp))

      Text("${getLocalizedResource(MR.strings.edit_blockedby_field)}:")
      EditViewSublist(taskNode, taskGraph, taskNode.blockingTasks)
      Spacer(Modifier.size(2.dp))

      Text("${getLocalizedResource(MR.strings.edit_blocking_field)}:")
      EditViewSublist(taskNode, taskGraph, taskNode.blockedTasks)
    }
  }
}

@Composable
private fun IndentedText(text: String) {
  Row {
    Spacer(Modifier.size(4.dp))
    Text(text)
  }
}
@Composable
private fun EditViewSublist(
  taskNode: TaskNode,
  taskGraph: Map<TaskId, TaskNode>,
  sublist: Collection<TaskId>
) {
  for (child in sublist) {
    val childTitle = taskGraph[child]!!.title
    // TODO UI onClick open child
    // TODO UI complete checkbox
    // TODO UI right click/gesture delete
    IndentedText(childTitle)
  }
}

// private fun TextFieldOf()