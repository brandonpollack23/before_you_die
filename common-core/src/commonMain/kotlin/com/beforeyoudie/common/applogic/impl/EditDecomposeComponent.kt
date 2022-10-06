package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.BydEditConfig
import com.beforeyoudie.common.applogic.IBydEdit

// TODO NOW doc all
class EditDecomposeComponent(
  editConfig: BydEditConfig,
  componentContext: ComponentContext
) : IBydEdit, ComponentContext by componentContext
