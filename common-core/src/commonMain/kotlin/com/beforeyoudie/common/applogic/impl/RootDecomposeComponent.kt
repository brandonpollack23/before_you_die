package com.beforeyoudie.common.applogic.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.applogic.IAppLogicEdit
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.IBydRoot
import com.beforeyoudie.common.storage.IBydStorage
import com.benasher44.uuid.Uuid
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
  private val deepLink: DeepLink = DeepLink.None,
  private val storage: IBydStorage,
  private val appLogicTaskGraphFactory: AppLogicTaskGraphFactory,
  private val appLogicEditFactory: AppLogicEditFactory
) :
  IBydRoot,
  ComponentContext by componentContext {
  private val navigation = StackNavigation<NavigationConfig>()
  val childStack: Value<ChildStack<*, IBydRoot.Child>> = childStack(
    source = navigation,
    initialStack = { getInitialStack(deepLink)},
    childFactory = ::createChild,
  )

  // TODO CONTINE from line: 39 https://github.com/arkivanov/Decompose/blob/master/sample/shared/shared/src/commonMain/kotlin/com/arkivanov/sample/shared/root/RootComponent.kt

  private fun createChild(
    config: NavigationConfig,
    componentContext: ComponentContext
  ): IBydRoot.Child =
    when (config) {
      is NavigationConfig.TaskGraph -> IBydRoot.Child.TaskGraph(
        appLogicTaskGraphFactory.createTaskGraph(
          config.taskGraphConfig,
          componentContext
        )
      )
      is NavigationConfig.Edit -> IBydRoot.Child.EditTask(
        appLogicEditFactory.createEdit(
          config.editConfig,
          componentContext
        )
      )
    }


  private companion object {
    fun getInitialStack(deepLink: DeepLink): List<NavigationConfig> = when (deepLink) {
      DeepLink.None -> listOf(NavigationConfig.TaskGraph())
    }
  }
}

private sealed interface NavigationConfig : Parcelable {
  @Parcelize
  data class TaskGraph(
    val taskGraphConfig: AppLogicTaskGraphConfig = AppLogicTaskGraphConfig()
  ) : NavigationConfig

  @Parcelize
  data class Edit(val editConfig: AppLogicEditConfig) : NavigationConfig
}

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
