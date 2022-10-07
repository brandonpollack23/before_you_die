package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph

class TodoGraphDecomposeComponent(
  override val config: AppLogicTaskGraphConfig,
  componentContext: ComponentContext
) :
  IAppLogicTaskGraph,
  ComponentContext by componentContext
