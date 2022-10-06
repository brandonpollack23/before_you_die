package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.IBydEdit
import com.beforeyoudie.common.applogic.IBydGraph
import com.beforeyoudie.common.applogic.IBydRoot
import org.koin.core.component.KoinComponent

// TODO NOW finish this along with other basic components using Flow and couroutines etc.
// https://github.com/JetBrains/compose-jb/blob/master/examples/todoapp/common/root/src/commonMain/kotlin/example/todo/common/root/integration/TodoRootComponent.kt

class RootDecomposeComponent(
  componentContext: ComponentContext,
  private val bydGraphConstructor: BydGraphConstructor,
  private val bydEditConstructor: BydEditConstructor
) :
  IBydRoot,
  KoinComponent,
  ComponentContext by componentContext

/** Function signature to create an [IBydGraph] for use by [RootDecomposeComponent] */
typealias BydGraphConstructor = (ComponentContext) -> IBydGraph

/** Function signature to create an [IBydEdit] for use by [RootDecomposeComponent] */
typealias BydEditConstructor = (ComponentContext) -> IBydEdit
