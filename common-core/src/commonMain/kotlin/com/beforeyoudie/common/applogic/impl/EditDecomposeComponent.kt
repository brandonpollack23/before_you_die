package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.BydEditConfig
import com.beforeyoudie.common.applogic.IBydEdit

/** This is the Decompose implementation of the corelogic for the edit views of a note. */
class EditDecomposeComponent(
  editConfig: BydEditConfig,
  componentContext: ComponentContext
) : IBydEdit, ComponentContext by componentContext
