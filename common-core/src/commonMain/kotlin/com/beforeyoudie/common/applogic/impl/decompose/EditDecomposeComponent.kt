package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.IAppLogicEdit

/** This is the Decompose implementation of the corelogic for the edit views of a note. */
class EditDecomposeComponent(componentContext: ComponentContext) :
  IAppLogicEdit,
  ComponentContext by componentContext
