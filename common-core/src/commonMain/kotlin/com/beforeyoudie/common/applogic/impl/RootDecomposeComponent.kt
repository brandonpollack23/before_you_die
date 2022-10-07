package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.IAppLogicEdit
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.IBydRoot
import com.beforeyoudie.common.storage.IBydStorage
import me.tatarka.inject.annotations.Inject

// TODO NOW finish this along with other basic components using Flow and couroutines etc.
// TODO NOW test this
// https://github.com/JetBrains/compose-jb/blob/master/examples/todoapp/common/root/src/commonMain/kotlin/example/todo/common/root/integration/TodoRootComponent.kt

// TODO NOW LAST implement children

/** This is the root CoreLogic component.  While the other components in the Decompose world are
 * created dynamically by this class, this one is a singleton and is thus injected by my DI
 * framework (kotlin-inject at the time of writing).
 *
 * @param componentContext the component context, contains access to state loaders, lifecycles, instance keepers, and back handler owners.
 * @property storage the backing storage of all the notes (including syncing if implemented, syncing is NOT handled at this level)
 * @property appLogicTaskGraphFactory factory for creating a new graph AppLogic
 * @property appLogicEditFactory factory for creating a new note edit AppLogic
 */
@Inject
class RootDecomposeComponent(
  componentContext: ComponentContext,
  private val storage: IBydStorage,
  private val appLogicTaskGraphFactory: AppLogicTaskGraphFactory,
  private val appLogicEditFactory: AppLogicEditFactory
) :
  IBydRoot,
  ComponentContext by componentContext

/**
 * Interface for constructing [IAppLogicTaskGraph].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicTaskGraphFactory {
  fun createTaskGraph(
    config: AppLogicTaskGraphConfig,
    componentContext: ComponentContext
  ): IAppLogicTaskGraph
}

/**
 * Main implementation of [AppLogicTaskGraphFactory] that constructs Decompose components.
 */
@Inject
class AppLogicTaskGraphFactoryImpl : AppLogicTaskGraphFactory {
  override fun createTaskGraph(
    config: AppLogicTaskGraphConfig,
    componentContext: ComponentContext
  ): IAppLogicTaskGraph = TaskGraphDecomposeComponent(config, componentContext)
}

/**
 * Interface for constructing [IAppLogicEdit].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicEditFactory {
  fun createEdit(config: AppLogicEditConfig, componentContext: ComponentContext): IAppLogicEdit
}

/**
 * Main implementation of [AppLogicEditFactory] that constructs Decompose components.
 */
@Inject
class AppLogicEditFactoryImpl : AppLogicEditFactory {
  override fun createEdit(
    config: AppLogicEditConfig,
    componentContext: ComponentContext
  ) = EditDecomposeComponent(config, componentContext)
}
