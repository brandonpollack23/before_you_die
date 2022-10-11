package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicEdit
import com.beforeyoudie.common.applogic.AppLogicEditConfig

/** This is the Decompose implementation of the corelogic for the edit views of a note. */
class EditDecomposeComponent(
  appLogicEditConfig: AppLogicEditConfig,
  componentContext: ComponentContext) :
  AppLogicEdit(appLogicEditConfig),
  ComponentContext by componentContext
