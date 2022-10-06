package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.BydGraphConfig
import com.beforeyoudie.common.applogic.IBydGraph

class TodoGraphDecomposeComponent(
  override val config: BydGraphConfig,
  componentContext: ComponentContext
) :
  IBydGraph,
  ComponentContext by componentContext
