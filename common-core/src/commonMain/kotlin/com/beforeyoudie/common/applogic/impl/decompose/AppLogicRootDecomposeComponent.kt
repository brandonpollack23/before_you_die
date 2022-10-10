package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.beforeyoudie.common.applogic.AppLogicEdit
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.AppLogicTaskGraph
import com.beforeyoudie.common.applogic.AppLogicTaskGraphConfig
import com.beforeyoudie.common.applogic.AppState
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
class AppLogicRootDecomposeComponent(
  private val storage: IBydStorage,
  private val deepLink: DeepLink = DeepLink.None,
  private val appLogicTaskGraphFactory: AppLogicTaskGraphFactory,
  private val appLogicEditFactory: AppLogicEditFactory,
  private val applicationCoroutineContext: ApplicationCoroutineContext,
  componentContext: ComponentContext
) :
  AppLogicRoot(storage),
  ComponentContext by componentContext {
  // The couroutine scope could come from external and the methods on the children of the root
  // could be "suspend", but since we maintain the lifecycle of these components separately there
  // is no reason to force the burden onto consumers of this library, we can use our own tree of
  // coroutine scopes.
  //
  // In other words, the source of truth of app state is not the UI but this heirarchy, and so it is
  // our coroutine context tree that should be used.
  override val coroutineScope = coroutineScopeWithLifecycle(applicationCoroutineContext)

  private val appStateInstanceKeeper = instanceKeeper.getOrCreate { RetainedAppState() }
  override val _appState: MutableStateFlow<AppState> = appStateInstanceKeeper.appState
  private class RetainedAppState : InstanceKeeper.Instance {
    private val logger = getClassLogger()
    val appState = MutableStateFlow(AppState())
    override fun onDestroy() {
      logger.i { "destroying app state, app must be exiting" }
    }
  }

  init {
    // Lifecycle setup.
    lifecycle.subscribe(object : Lifecycle.Callbacks {
      override fun onCreate() {
        coroutineScope.launch {
          val initialTaskGraph = withContext(Dispatchers.IO) {
            logger.v { "Loading initial state from the storage" }
            storage.selectAllTaskNodeInformation()
          }

          _appState.value = appState.value.copy(taskGraph = initialTaskGraph, isLoading = false)
        }
      }
    })
  }

  private val navigation = StackNavigation<NavigationConfig>()

  // In decompose based UI implementation Composable widget, just check the interface is this type, then access this directly (NOT through the interface).
  val childStack: Value<ChildStack<*, Child>> = childStack(
    source = navigation,
    initialStack = { getInitialStack(deepLink) },
    childFactory = ::createChild,
    handleBackButton = true
  )

  private fun createChild(
    config: NavigationConfig,
    componentContext: ComponentContext
  ): Child {
    logger.v("Creating a child with config $config")
    return when (config) {
      is NavigationConfig.TaskGraph -> {
        val taskGraph = appLogicTaskGraphFactory.createTaskGraph(
          config.taskGraphConfig,
          applicationCoroutineContext,
          componentContext
        )

        subscribeToTaskGraphEvents(taskGraph.taskGraphEvents)
        Child.TaskGraph(taskGraph)
      }

      // TODO NOW implement edit now as well, just passing UUID will be fine since its all accessible from
      //  the higher level root and can be passed to children from there, no need to duplicate that work here
      is NavigationConfig.Edit -> Child.EditTask(
        appLogicEditFactory.createEdit(applicationCoroutineContext, componentContext)
      )
    }
  }

  override fun onOpenEdit(taskId: TaskId) {
    if (appState.value.isLoading) {
      logger.e(
        "Open edit called while loading, this shouldn't be possible! " +
          "Returning and doing nothing"
      )
      return
    }

    logger.v("Open edit triggered for task $taskId")
    navigation.push(NavigationConfig.Edit(AppLogicEditConfig(taskId)))
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
 * Interface for constructing [AppLogicTaskGraph].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicTaskGraphFactory {
  fun createTaskGraph(
    appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
    parentCoroutineContext: CoroutineContext,
    componentContext: ComponentContext
  ): AppLogicTaskGraph
}

/**
 * Main implementation of [AppLogicTaskGraphFactory] that constructs Decompose components.
 */
@Inject
class AppLogicTaskGraphFactoryImpl : AppLogicTaskGraphFactory {
  override fun createTaskGraph(
    appLogicTaskGraphConfig: AppLogicTaskGraphConfig,
    parentCoroutineContext: CoroutineContext,
    componentContext: ComponentContext
  ): AppLogicTaskGraph = TaskGraphDecomposeComponent(
    appLogicTaskGraphConfig,
    parentCoroutineContext,
    componentContext
  )
}

/**
 * Interface for constructing [IAppLogicEdit].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicEditFactory {
  fun createEdit(
    parentCoroutineContext: CoroutineContext,
    componentContext: ComponentContext
  ): AppLogicEdit
}

/**
 * Main implementation of [AppLogicEditFactory] that constructs Decompose components.
 */
@Inject
class AppLogicEditFactoryImpl : AppLogicEditFactory {
  override fun createEdit(
    parentCoroutineContext: CoroutineContext,
    componentContext: ComponentContext
  ) =
    EditDecomposeComponent(componentContext)
}
