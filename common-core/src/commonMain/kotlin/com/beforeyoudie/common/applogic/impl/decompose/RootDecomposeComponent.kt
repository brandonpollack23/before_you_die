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
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.RootComponentContext
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskIdGenerator
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.util.getClassLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

// TODO NOW implement edit now as well, just passing UUID will be fine since its all accessible from
//  the higher level root and can be passed to children from there, no need to duplicate that work here

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
  private val storage: IBydStorage,
  private val deepLink: DeepLink = DeepLink.None,
  private val appLogicTaskGraphFactory: AppLogicTaskGraphFactory,
  private val appLogicEditFactory: AppLogicEditFactory,
  private val applicationCoroutineContext: ApplicationCoroutineContext,
  private val ioCoroutineContext: IOCoroutineContext,
  taskIdGenerator: TaskIdGenerator,
  componentContext: RootComponentContext
) :
  AppLogicRoot(storage, taskIdGenerator),
  ComponentContext by componentContext {
  // The coroutine scope could come from external and the methods on the children of the root
  // could be "suspend", but since we maintain the lifecycle of these components separately there
  // is no reason to force the burden onto consumers of this library, we can use our own tree of
  // coroutine scopes.
  //
  // In other words, the source of truth of app state is not the UI but this heirarchy, and so it is
  // our coroutine context tree that should be used.
  override val coroutineScope = coroutineScopeWithLifecycle(applicationCoroutineContext)

  // Decompose navigation controller.
  private val navigation = StackNavigation<NavigationConfig>()

  // Decompose child stack navigation.  This internally saves child configurations for
  // reconstruction.
  val childStack: Value<ChildStack<*, Child>> = childStack(
    source = navigation,
    initialStack = { getInitialStack(deepLink) },
    childFactory = ::createChild,
    handleBackButton = true
  )

  // Instance keeper, this saves/loads across configuration changes on multiple platforms.
  private val appStateInstanceKeeper =
    instanceKeeper.getOrCreate { RetainedAppState(childStack.value) }
  override val mutableAppStateFlow: MutableStateFlow<AppState> = appStateInstanceKeeper.appState

  init {
    // Lifecycle setup.
    lifecycle.subscribe(object : Lifecycle.Callbacks {
      override fun onCreate() {
        childStack.subscribe(::updateAppStateBasedOnChildStack)

        coroutineScope.launch {
          val initialTaskGraph = withContext(ioCoroutineContext) {
            logger.v { "Loading initial state from the storage" }
            storage.selectAllTaskNodeInformation()
          }

          mutableAppStateFlow.value = appStateFlow.value.copy(taskGraph = initialTaskGraph, isLoading = false)
        }
      }

      override fun onDestroy() {
        childStack.unsubscribe(::updateAppStateBasedOnChildStack)
      }
    })
  }

  override fun onOpenEdit(taskId: TaskId) {
    if (appStateFlow.value.isLoading) {
      logger.e(
        "Open edit called while loading, this shouldn't be possible! " +
          "Returning and doing nothing"
      )
      return
    }

    logger.v("Open edit triggered for task $taskId")
    navigation.push(NavigationConfig.Edit(AppLogicEditConfig(taskId)))
  }

  private fun createChild(
    config: NavigationConfig,
    componentContext: ComponentContext
  ): Child {
    logger.v("Creating a child with config $config")
    return when (config) {
      is NavigationConfig.TaskGraph -> createTaskGraphChild(config, componentContext)
      is NavigationConfig.Edit -> createEditChild(config, componentContext)
    }
  }

  private fun createTaskGraphChild(
    config: NavigationConfig.TaskGraph,
    componentContext: ComponentContext
  ): Child.TaskGraph {
    val taskGraph = appLogicTaskGraphFactory.createTaskGraph(
      config.taskGraphConfig,
      applicationCoroutineContext,
      componentContext
    )

    subscribeToTaskGraphEvents(taskGraph.taskGraphEvents)
    return Child.TaskGraph(taskGraph)
  }

  private fun createEditChild(
    config: NavigationConfig.Edit,
    componentContext: ComponentContext
  ) = Child.EditTask(
    appLogicEditFactory.createEdit(
      config.editConfig,
      applicationCoroutineContext,
      componentContext
    )
  )

  /**
   * The instance implementation for the app's state.
   *
   * This logs and creates the instance, subscribes to the [Value] and converts it to be part of
   * the app's state [MutableStateFlow], and also logs and unsubscribes on destroy.
   */
  private class RetainedAppState(
    childStack: ChildStack<*, Child>
  ) : InstanceKeeper.Instance {
    private val logger = getClassLogger()
    val appState = MutableStateFlow(AppState(activeChild = childStack.active.instance))

    init { logger.i { "creating app state, app must be initializing" } }
    override fun onDestroy() { logger.i { "destroying app state, app must be exiting" } }
  }

  private fun updateAppStateBasedOnChildStack(childStack: ChildStack<*, Child>) {
    mutableAppStateFlow.value =
      mutableAppStateFlow.value.copy(activeChild = childStack.active.instance)
  }

  private companion object {
    fun getInitialStack(deepLink: DeepLink): List<NavigationConfig> = when (deepLink) {
      DeepLink.None -> listOf(NavigationConfig.TaskGraph())
    }
  }
}

/**
 * NavigationConfig which basically contains the type of child to create containing its necessary
 * configurable construction parameters.
 */
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
 * Interface for constructing [AppLogicEdit].
 *
 * Factory is helpful because it allows construction to be overridden and changed in the future and in tests.
 */
interface AppLogicEditFactory {
  fun createEdit(
    appLogicEditConfig: AppLogicEditConfig,
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
    appLogicEditConfig: AppLogicEditConfig,
    parentCoroutineContext: CoroutineContext,
    componentContext: ComponentContext
  ) =
    EditDecomposeComponent(appLogicEditConfig, componentContext)
}
