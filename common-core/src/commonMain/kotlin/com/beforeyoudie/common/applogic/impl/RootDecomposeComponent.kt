package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.BydEditConfig
import com.beforeyoudie.common.applogic.BydGraphConfig
import com.beforeyoudie.common.applogic.IBydEdit
import com.beforeyoudie.common.applogic.IBydGraph
import com.beforeyoudie.common.applogic.IBydRoot
import com.beforeyoudie.common.storage.IBydStorage
import me.tatarka.inject.annotations.Inject

// TODO NOW DOC, make sure to mention how only root is injected, others are construced on demand
// TODO NOW finish this along with other basic components using Flow and couroutines etc.
// https://github.com/JetBrains/compose-jb/blob/master/examples/todoapp/common/root/src/commonMain/kotlin/example/todo/common/root/integration/TodoRootComponent.kt

@Inject
class RootDecomposeComponent(
  componentContext: ComponentContext,
  private val storage: IBydStorage,
  private val bydGraphConstructor: BydGraphConstructor,
  private val bydEditConstructor: BydEditConstructor
) :
  IBydRoot,
  ComponentContext by componentContext {
  init {
    // TODO NOW placeholder, remove
    storage.selectAllTaskNodeInformation()
  }
}

/** Function signature to create an [IBydGraph] for use by [RootDecomposeComponent] */
typealias BydGraphConstructor = (BydGraphConfig, ComponentContext) -> IBydGraph

/** Function signature to create an [IBydEdit] for use by [RootDecomposeComponent] */
typealias BydEditConstructor = (BydEditConfig, ComponentContext) -> IBydEdit
