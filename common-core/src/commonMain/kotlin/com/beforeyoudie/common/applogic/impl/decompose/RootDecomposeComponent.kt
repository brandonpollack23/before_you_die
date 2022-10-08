package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.AppState
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.applogic.IAppLogicEdit
import com.beforeyoudie.common.applogic.IAppLogicRoot
import com.beforeyoudie.common.applogic.IAppLogicTaskGraph
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.beforeyoudie.common.applogic.createTaskGraphEventsFlow
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

// TODO NOW test children

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
  private val applicationCoroutineContext: ApplicationCoroutineContext,
  private val deepLink: DeepLink = DeepLink.None,
  private val storage: IBydStorage,
  private val appLogicTaskGraphFactory: AppLogicTaskGraphFactory,
  private val appLogicEditFactory: AppLogicEditFactory
) :
  IAppLogicRoot,
  ComponentContext by componentContext {
  val logger = getClassLogger()
  private val coroutineScope = coroutineScopeWithLifecycle(applicationCoroutineContext, lifecycle)

  // TODO NOW state preservation. Also include coroutine scope: https://arkivanov.github.io/Decompose/component/scopes/#creating-a-coroutinescope-in-a-component
  override val appState = MutableStateFlow(AppState())
  // AppState lenses.
  private val taskGraphStateFlow =
    AppState.createTaskGraphStateFlow(coroutineScope, appState)

  init {
    // Lifecycle setup.
    lifecycle.subscribe(object : Lifecycle.Callbacks {
      override fun onCreate() {
        appState.value = AppState(storage.selectAllTaskNodeInformation())
      }
    })

  }

  private val navigation = StackNavigation<NavigationConfig>()

  // In decompose based UI implementation Composable widget, just check the interface is this type, then access this directly (NOT through the interface).
  val childStack: Value<ChildStack<*, IAppLogicRoot.Child>> = childStack(
    source = navigation,
    initialStack = { getInitialStack(deepLink) },
    childFactory = ::createChild
  )

  private fun createChild(
    config: NavigationConfig,
    componentContext: ComponentContext
  ): IAppLogicRoot.Child = runBlocking(applicationCoroutineContext) {
    when (config) {
      is NavigationConfig.TaskGraph -> {
        val events = createTaskGraphEventsFlow(storage, taskGraphStateFlow, logger)
        IAppLogicRoot.Child.TaskGraph(
          appLogicTaskGraphFactory.createTaskGraph(
            config.taskGraphConfig,
            Job(),
            events,
            componentContext
          )
        )
      }

      // TODO NOW do edit now as well
      is NavigationConfig.Edit -> IAppLogicRoot.Child.EditTask(
        appLogicEditFactory.createEdit(componentContext)
      )
    }
  }

  private companion object {
    fun getInitialStack(deepLink: DeepLink): List<NavigationConfig> = when (deepLink) {
      DeepLink.None -> listOf(NavigationConfig.TaskGraph())
    }
  }
}

private sealed class NavigationConfig : Parcelable {
  @Parcelize
  data class TaskGraph(
    val taskGraphConfig: AppLogicTaskGraphConfig = AppLogicTaskGraphConfig()
  ) : NavigationConfig()

  @Parcelize
  data class Edit(val editConfig: AppLogicEditConfig) : NavigationConfig()
}

/**
 * Interface for constructing [IAppLogicTaskGraph].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicTaskGraphFactory {
  fun createTaskGraph(
    appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
    coroutineContext: CoroutineContext,
    taskGraphEvents: MutableSharedFlow<TaskGraphEvent>,
    componentContext: ComponentContext
  ): IAppLogicTaskGraph
}

/**
 * Main implementation of [AppLogicTaskGraphFactory] that constructs Decompose components.
 */
@Inject
class AppLogicTaskGraphFactoryImpl : AppLogicTaskGraphFactory {
  override fun createTaskGraph(
    appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
    coroutineContext: CoroutineContext,
    taskGraphEvents: MutableSharedFlow<TaskGraphEvent>,
    componentContext: ComponentContext
  ): IAppLogicTaskGraph = TaskGraphDecomposeComponent(
    appLogicTaskGraphConfig,
    coroutineContext,
    taskGraphEvents,
    componentContext
  )
}

/**
 * Interface for constructing [IAppLogicEdit].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicEditFactory {
  fun createEdit(componentContext: ComponentContext): IAppLogicEdit
}

/**
 * Main implementation of [AppLogicEditFactory] that constructs Decompose components.
 */
@Inject
class AppLogicEditFactoryImpl : AppLogicEditFactory {
  override fun createEdit(componentContext: ComponentContext) =
    EditDecomposeComponent(componentContext)
}

private fun coroutineScopeWithLifecycle(coroutineContext: CoroutineContext, lifecycle: Lifecycle): CoroutineScope {
  val scope = CoroutineScope(coroutineContext) + Job()
  lifecycle.doOnDestroy(scope::cancel)
  return scope
}

