package com.beforeyoudie.common.applogic.impl.decompose

import MockStoragePlatformComponent
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.ApplicationScope
import com.beforeyoudie.common.di.ApplicationStoragePlatformComponent
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.DecomposeAppLogicComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.randomTaskId
import createTestPlatformComponent
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

// TODO NOW construct common test versions of each subcomponent, construct mock versions of each subcomponent, shared code.
@OptIn(ExperimentalCoroutinesApi::class)
@Component
abstract class AppLogicRootDecomposeTestComponent(
  platformComponent: BydPlatformComponent,
  storageComponent: ApplicationStoragePlatformComponent,
  appLogicComponent: DecomposeAppLogicComponent
) : BydKotlinInjectAppComponent(platformComponent, storageComponent, appLogicComponent) {
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
  AppLogicRootDecomposeTestComponent::class.create(
    platformComponent,
    storageComponent,
    appLogicComponent
  )
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

      // Run onCreate launched load.
      testMainDispatcher.scheduler.advanceUntilIdle()
      // Load from IO.
      testIODispatcher.scheduler.advanceUntilIdle()
      // Complete load state change.
      testMainDispatcher.scheduler.advanceUntilIdle()

      appLogicRootDecomposeComponent.appState.value.isLoading shouldBe false
      appLogicRootDecomposeComponent.appState.value.taskGraph shouldContainExactlyInAnyOrder
        taskNodes
    }

    test("Child navigation causes edit view to open") {
    }
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
