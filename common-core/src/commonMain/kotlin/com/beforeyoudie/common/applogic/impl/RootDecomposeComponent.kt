package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.BydEditConfig
import com.beforeyoudie.common.applogic.BydGraphConfig
import com.beforeyoudie.common.applogic.IBydEdit
import com.beforeyoudie.common.applogic.IBydGraph
import com.beforeyoudie.common.applogic.IBydRoot
import com.beforeyoudie.common.storage.IBydStorage
import me.tatarka.inject.annotations.Inject

// TODO NOW finish this along with other basic components using Flow and couroutines etc.
// https://github.com/JetBrains/compose-jb/blob/master/examples/todoapp/common/root/src/commonMain/kotlin/example/todo/common/root/integration/TodoRootComponent.kt

/** This is the root CoreLogic component.  While the other components in the Decompose world are
 * created dynamically by this class, this one is a singleton and is thus injected by my DI
 * framework (kotlin-inject at the time of writing).
 *
 * @param componentContext the component context, contains access to state loaders, lifecycles, instance keepers, and back handler owners.
 * @property storage the backing storage of all the notes (including syncing if implemented, syncing is NOT handled at this level)
 * @property bydGraphConstructor factory method for creating a new graph CoreLogic
 * @property bydEditConstructor factory method for creating a new note edit CoreLogic
 */
@Inject
class RootDecomposeComponent(
  componentContext: ComponentContext,
  private val storage: IBydStorage,
  private val bydGraphConstructor: BydGraphConstructor,
  private val bydEditConstructor: BydEditConstructor
) :
  IBydRoot,
  ComponentContext by componentContext

/** Function signature to create an [IBydGraph] for use by [RootDecomposeComponent] */
typealias BydGraphConstructor = (BydGraphConfig, ComponentContext) -> IBydGraph

/** Function signature to create an [IBydEdit] for use by [RootDecomposeComponent] */
typealias BydEditConstructor = (BydEditConfig, ComponentContext) -> IBydEdit
