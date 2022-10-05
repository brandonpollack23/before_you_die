package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.IBydGraph

class TodoGraphDecomposeComponent(componentContext: ComponentContext) :
  IBydGraph,
  ComponentContext by componentContext {

}
