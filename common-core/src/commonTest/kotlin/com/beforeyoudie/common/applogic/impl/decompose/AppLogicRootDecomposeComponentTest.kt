package com.beforeyoudie.common.applogic.impl.decompose

import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.TestBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.randomTaskId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

// TODO NOW seperate Decompose into it's own component provider that binds to AppLogic implementations (for being configurable)
@OptIn(ExperimentalCoroutinesApi::class)
@Component
abstract class AppLogicRootDecomposeTestComponent(
  @Component val parent: TestBydKotlinInjectAppComponent =
    TestBydKotlinInjectAppComponent::class.create()
) {
  abstract val testDispatcherContext: ApplicationCoroutineContext
  val testDispatcher
    get() = testDispatcherContext as TestDispatcher

  abstract val rootDecomposeComponent: AppLogicRoot
  abstract val storage: IBydStorage

  @Provides
  fun mockStorage(): IBydStorage = mockkClass(IBydStorage::class)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppLogicRootDecomposeComponentTest : CommonTest() {
  private lateinit var testDispatcher: TestDispatcher
  private lateinit var appLogicRoot: AppLogicRoot
  private val appLogicRootDecomposeComponent
    get() = appLogicRoot as AppLogicRootDecomposeComponent
  private lateinit var mockStorage: IBydStorage

  private val picardTaskId = randomTaskId()
  private val rikerTaskId = randomTaskId()
  private val laforgeTaskId = randomTaskId()

  init {
    beforeTest {
      val injectComponent = AppLogicRootDecomposeTestComponent::class.create()
      testDispatcher = injectComponent.testDispatcher
      appLogicRoot = injectComponent.rootDecomposeComponent
      mockStorage = injectComponent.storage

      setupMocks()
    }

    test("Is Correct Instance Implementation") {
      appLogicRoot::class shouldBe AppLogicRootDecomposeComponent::class
    }

    test("Initial child is a task graph") {
      appLogicRootDecomposeComponent.childStack.value.active.instance::class shouldBe
        AppLogicRoot.Child.TaskGraph::class
    }

    // TODO NOW test navigation, streams, etc
  }

  private fun setupMocks() {
    every { mockStorage.selectAllTaskNodeInformation() } returns setOf(
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
  }
}
