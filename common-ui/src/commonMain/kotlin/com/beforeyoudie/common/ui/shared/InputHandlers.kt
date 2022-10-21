@file:OptIn(ExperimentalComposeUiApi::class)

package com.beforeyoudie.common.ui.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

fun handleTab(keyEvent: KeyEvent, isEnabled: Boolean = true, action: () -> Unit): Boolean {
  return handleKey(keyEvent, setOf(Key.Tab), KeyEventType.KeyDown, isEnabled, action)
}

fun handleReturnKey(keyEvent: KeyEvent, isEnabled: Boolean = true, action: () -> Unit): Boolean {
  return handleKey(
    keyEvent,
    setOf(Key.Enter, Key.NumPadEnter),
    KeyEventType.KeyDown,
    isEnabled,
    action
  )
}

private fun handleKey(
  keyEvent: KeyEvent,
  keys: Set<Key>,
  eventType: KeyEventType,
  isEnabled: Boolean,
  action: () -> Unit
): Boolean {
  return if(isEnabled && keys.contains(keyEvent.key) && keyEvent.type == eventType) {
    action()
    true
  } else {
    false
  }
}