package com.beforeyoudie.common.applogic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class AppLogicChild {
  /** See [AppLogicRoot] */
  abstract val coroutineScope: CoroutineScope

  protected val mutableTaskEvents: MutableSharedFlow<TaskEvent> =
    MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  /**
   * The immutable view on the events emitted by the task graph to be consumed by owning AppLogic
   * components.
   */
  val taskGraphEvents = mutableTaskEvents.asSharedFlow()

  open fun onBackPressed() {
    coroutineScope.launch {
      mutableTaskEvents.emit(TaskEvent.Back)
    }
  }
}