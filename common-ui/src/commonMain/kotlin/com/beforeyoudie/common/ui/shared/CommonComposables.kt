package com.beforeyoudie.common.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.beforeyoudie.common.resources.MR
import com.beforeyoudie.common.util.getLocalizedResource

@Composable
fun BydTopAppBar(onBackPressed: (() -> Unit)? = null) {
  val navigationIcon: @Composable (() -> Unit)? = if (onBackPressed != null) {
    @Composable {
      Icon(
        Icons.Filled.ArrowBack,
        contentDescription = getLocalizedResource(MR.strings.back),
        modifier = Modifier.clickable { onBackPressed() })
    }
  } else {
    null
  }

  TopAppBar(
    title = { Text(getLocalizedResource(MR.strings.app_title)) },
    navigationIcon = navigationIcon
  )
}