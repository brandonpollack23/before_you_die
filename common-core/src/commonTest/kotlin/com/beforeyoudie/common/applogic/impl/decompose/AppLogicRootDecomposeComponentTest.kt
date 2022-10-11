package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DecomposeAppLogicComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.MockStoragePlatformComponent
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.di.createTestPlatformComponent
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.randomTaskId
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestDispatcher
import me.tatarka.inject.annotations.Component

@OptIn(ExperimentalCoroutinesApi::class)
@Component
abstract class AppLogicRootDecomposeTestComponent(
  @Component val bydKotlinInjectAppComponent: BydKotlinInjectAppComponent
) {
  abstract val testMainCoroutineContext: ApplicationCoroutineContext
  val testMainDispatcher
    get() = testMainCoroutineContext as TestDispatcher

  abstract val testIOCoroutineContext: IOCoroutineContext
  val testIODispatcher
    get() = testIOCoroutineContext as TestDispatcher

  abstract val rootDecomposeComponent: AppLogicRoot
  abstract val lifecycleRegistry: LifecycleRegistry
  abstract val storage: IBydStorage
}
fun createAppLogicRootDecomposeTestComponent() = run {
  val platformComponent = createTestPlatformComponent()
  val storageComponent = MockStoragePlatformComponent::class.create()
  val appLogicComponent = DecomposeAppLogicComponent::class.create(storageComponent, platformComponent)

  val component = BydKotlinInjectAppComponent::class.create(
    platformComponent,
    storageComponent,
    appLogicComponent
  )

  AppLogicRootDecomposeTestComponent::class.create(component)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppLogicRootDecomposeComponentTest : CommonTest() {
  private lateinit var testMainDispatcher: TestDispatcher
  private lateinit var testIODispatcher: TestDispatcher

  private lateinit var mockStorage: IBydStorage

  private lateinit var appLogicRoot: AppLogicRoot
  private val appLogicRootDecomposeComponent
    get() = appLogicRoot as AppLogicRootDecomposeComponent
  private lateinit var lifecycleRegistry: LifecycleRegistry

  private val picardTaskId = randomTaskId()
  private val rikerTaskId = randomTaskId()
  private val laforgeTaskId = randomTaskId()

  init {
    beforeTest {
      val injectComponent = createAppLogicRootDecomposeTestComponent()
      testMainDispatcher = injectComponent.testMainDispatcher
      testIODispatcher = injectComponent.testIODispatcher
      mockStorage = injectComponent.storage
      appLogicRoot = injectComponent.rootDecomposeComponent
      lifecycleRegistry = injectComponent.lifecycleRegistry

      setupMocks()

      lifecycleRegistry.create()
    }

    test("Is Correct Instance Implementation") {
      appLogicRoot::class shouldBe AppLogicRootDecomposeComponent::class
    }

    test("Initial child is a task graph") {
      appLogicRootDecomposeComponent.childStack.value.active.instance::class shouldBe
        AppLogicRoot.Child.TaskGraph::class
    }

    test("Loads storage on initialization and transitions from isLoading state") {
      appLogicRootDecomposeComponent.appState.value.isLoading shouldBe true
      finishOnCreate()

      appLogicRootDecomposeComponent.appState.value.isLoading shouldBe false
      appLogicRootDecomposeComponent.appState.value.taskGraph shouldContainExactlyInAnyOrder
        taskNodes
    }

    // TODO NOW comment explaining
    test("Child navigation causes edit view to open") {
      finishOnCreate()
      val graph = appLogicRootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(picardTaskId)

      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.OpenEdit(picardTaskId)
      }

      testMainDispatcher.scheduler.advanceUntilIdle()

      val editTask = appLogicRootDecomposeComponent.childStack.value.active.instance
      editTask::class shouldBe AppLogicRoot.Child.EditTask::class
      editTask as AppLogicRoot.Child.EditTask
      editTask.appLogic.appLogicEditConfig shouldBe AppLogicEditConfig(picardTaskId)
    }

    // TODO NOW for each of these three, make sure in memory state is updated, correct children are opened, and storage mock is called correctly
    test("Delete task event") {
      val graph = appLogicRootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.deleteTaskAndChildren(picardTaskId)

      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.DeleteTaskAndChildren(picardTaskId)
      }
    }

    test("Add task event") {
      val graph = appLogicRootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.createTask(
        "Take This Message To Your Leaders, Gul Macet",
        "We'll be watching",
        picardTaskId
      )

      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.CreateTask(
          "Take This Message To Your Leaders, Gul Macet",
          "We'll be watching",
          picardTaskId
        )
      }
    }
  }

  private fun finishOnCreate() {
    // Run onCreate launched load.
    testMainDispatcher.scheduler.advanceUntilIdle()
    // Load from IO.
    testIODispatcher.scheduler.advanceUntilIdle()
    // Complete load state change.
    testMainDispatcher.scheduler.advanceUntilIdle()
  }

  private val taskNodes: Set<TaskNode> = setOf(
    TaskNode(
      picardTaskId,
      "Captain Picard",
      "Worlds best captain"
    ),
    TaskNode(
      rikerTaskId,
      "William T Riker",
      "Beard or go home",
      parent = picardTaskId,
      blockedTasks = setOf(laforgeTaskId)
    ),
    TaskNode(
      laforgeTaskId,
      "Geordi Laforge",
      "Space Engineering Master",
      blockingTasks = setOf(rikerTaskId)
    )
  )

  private fun setupMocks() {
    every { mockStorage.selectAllTaskNodeInformation() } returns taskNodes
  }
}
