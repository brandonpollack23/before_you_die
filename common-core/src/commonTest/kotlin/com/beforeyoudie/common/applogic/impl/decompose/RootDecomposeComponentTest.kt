package com.beforeyoudie.common.applogic.impl.decompose

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.applogic.AppLogicEditConfig
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.TaskGraphEvent
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.MockStoragePlatformComponent
import com.beforeyoudie.common.di.TestDecomposeAppLogicComponent
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.di.createTestPlatformComponent
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskIdGenerator
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.randomTaskId
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestDispatcher
import me.tatarka.inject.annotations.Component

@OptIn(ExperimentalCoroutinesApi::class)
@Component
abstract class RootDecomposeTestComponent(
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

  abstract val taskIdGenerator: TaskIdGenerator
}
fun createAppLogicRootDecomposeTestComponent() = run {
  val platformComponent = createTestPlatformComponent()
  val storageComponent = MockStoragePlatformComponent::class.create()
  val appLogicComponent =
    TestDecomposeAppLogicComponent::class.create(storageComponent, platformComponent)

  val component = BydKotlinInjectAppComponent::class.create(
    platformComponent,
    storageComponent,
    appLogicComponent
  )

  RootDecomposeTestComponent::class.create(component)
}

private val picardTaskId = randomTaskId()
private val rikerTaskId = randomTaskId()
private val laforgeTaskId = randomTaskId()

@OptIn(ExperimentalCoroutinesApi::class)
class AppLogicRootDecomposeComponentTest : CommonTest() {
  private lateinit var testMainDispatcher: TestDispatcher
  private lateinit var testIODispatcher: TestDispatcher

  private lateinit var mockStorage: IBydStorage

  private lateinit var appLogicRoot: AppLogicRoot
  private val rootDecomposeComponent
    get() = appLogicRoot as RootDecomposeComponent
  private lateinit var lifecycleRegistry: LifecycleRegistry

  private lateinit var taskIdGenerator: TaskIdGenerator

  init {
    beforeTest {
      val injectComponent = createAppLogicRootDecomposeTestComponent()
      testMainDispatcher = injectComponent.testMainDispatcher
      testIODispatcher = injectComponent.testIODispatcher
      mockStorage = injectComponent.storage
      appLogicRoot = injectComponent.rootDecomposeComponent
      lifecycleRegistry = injectComponent.lifecycleRegistry
      taskIdGenerator = injectComponent.taskIdGenerator

      setupMocks()

      lifecycleRegistry.create()
    }

    test("Is Correct Instance Implementation") {
      appLogicRoot::class shouldBe RootDecomposeComponent::class
    }

    test("Initial child is a task graph") {
      rootDecomposeComponent.childStack.value.active.instance::class shouldBe
        AppLogicRoot.Child.TaskGraph::class
    }

    test("Loads storage on initialization and transitions from isLoading state") {
      rootDecomposeComponent.appStateFlow.value.isLoading shouldBe true
      finishOnCreate()

      rootDecomposeComponent.appStateFlow.value.isLoading shouldBe false
      rootDecomposeComponent.appStateFlow.value.taskGraph shouldContainExactly
        taskNodes
    }

    test("Child navigation causes edit view to open") {
      // First makes certain the graph loads into memory.
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      // Trigger an open edit call.
      graph.appLogic.openEdit(picardTaskId)

      // Detect this ocurred on the mock.
      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.OpenEdit(picardTaskId)
      }

      // Advance all coroutine flows.
      testMainDispatcher.scheduler.advanceUntilIdle()

      // Ensure edit app logic child is created accordingly.
      val editTask = rootDecomposeComponent.childStack.value.active.instance
      editTask::class shouldBe AppLogicRoot.Child.EditTask::class
      editTask as AppLogicRoot.Child.EditTask
      editTask.appLogic.appLogicEditConfig shouldBe AppLogicEditConfig(picardTaskId)
    }

    test("Delete task event") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      // Setup mock to return what should be removed by storage.
      every { mockStorage.removeTaskNodeAndChildren(picardTaskId) } returns Result.success(
        setOf(
          picardTaskId,
          rikerTaskId
        )
      )

      // Make the call.
      graph.appLogic.deleteTaskAndChildren(picardTaskId)

      // Verify the event is sent in the stream.
      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.DeleteTaskAndChildren(picardTaskId)
      }

      // Propogate coroutines and verify the call was made to storage.
      testMainDispatcher.scheduler.advanceUntilIdle()
      verify(exactly = 1) { mockStorage.removeTaskNodeAndChildren(picardTaskId) }

      // Verify in memory state.
      rootDecomposeComponent.appStateFlow.value.taskGraph shouldContainExactly
        mapOf(
          laforgeTaskId to TaskNode(
            laforgeTaskId,
            "Geordi Laforge",
            "Space Engineering Master"
          )
        )
    }

    test("Add task event") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      // Mock the insertion and extract the generated id
      var gulmacetTaskId: TaskId? = null
      every {
        mockStorage.insertTaskNode(
          TaskId(any()),
          any(),
          any(),
          picardTaskId,
          false
        )
      } answers {
        gulmacetTaskId = TaskId(arg(0))
        Result.success(
          TaskNode(
            id = gulmacetTaskId!!,
            title = arg(1),
            description = arg(2),
            false,
            picardTaskId
          )
        )
      }

      graph.appLogic.createTask(
        "Take This Message To Your Leaders, Gul Macet",
        "We'll be watching",
        picardTaskId
      )
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.insertTaskNode(
          gulmacetTaskId!!,
          "Take This Message To Your Leaders, Gul Macet",
          "We'll be watching",
          picardTaskId
        )
      }

      graph.appLogic.taskGraphEvents.onEach {
        it shouldBe TaskGraphEvent.CreateTask(
          "Take This Message To Your Leaders, Gul Macet",
          "We'll be watching",
          picardTaskId
        )
      }

      rootDecomposeComponent.appStateFlow.value.taskGraph shouldContainExactly
        mapOf(
          gulmacetTaskId!! to TaskNode(
            gulmacetTaskId!!,
            "Take This Message To Your Leaders, Gul Macet",
            "We'll be watching",
            parent = picardTaskId
          ),
          picardTaskId to TaskNode(
            picardTaskId,
            "Captain Picard",
            "Worlds best captain",
            children = setOf(rikerTaskId, gulmacetTaskId!!)
          ),
          rikerTaskId to TaskNode(
            rikerTaskId,
            "William T Riker",
            "Beard or go home",
            parent = picardTaskId,
            blockedTasks = setOf(laforgeTaskId)
          ),
          laforgeTaskId to TaskNode(
            laforgeTaskId,
            "Geordi Laforge",
            "Space Engineering Master",
            blockingTasks = setOf(rikerTaskId)
          )
        )
    }

    test("edit title") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(picardTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      every {
        mockStorage.updateTaskTitle(picardTaskId, any())
      } answers {
        Result.success(Unit)
      }

      val edit = appLogicRoot.appStateFlow.value.activeChild as AppLogicRoot.Child.EditTask
      edit.appLogic.editTitle("Admiral Picard")
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.updateTaskTitle(picardTaskId, "Admiral Picard")
      }

      appLogicRoot.appStateFlow.value.taskGraph[picardTaskId] shouldBe TaskNode(
        picardTaskId,
        "Admiral Picard",
        "Worlds best captain",
        children = setOf(rikerTaskId)
      )
    }

    test("edit description") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(picardTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      every {
        mockStorage.updateTaskDescription(picardTaskId, any())
      } answers {
        Result.success(Unit)
      }

      val edit = appLogicRoot.appStateFlow.value.activeChild as AppLogicRoot.Child.EditTask
      edit.appLogic.editDescription("Exemplary Human")
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.updateTaskDescription(picardTaskId, "Exemplary Human")
      }

      appLogicRoot.appStateFlow.value.taskGraph[picardTaskId] shouldBe TaskNode(
        picardTaskId,
        "Captain Picard",
        "Exemplary Human",
        children = setOf(rikerTaskId)
      )
    }

    test("Add Child From Edit -- Reparent") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(picardTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      every {
        mockStorage.addChildToTaskNode(picardTaskId, laforgeTaskId)
      } answers {
        Result.success(Unit)
      }

      val edit = appLogicRoot.appStateFlow.value.activeChild as AppLogicRoot.Child.EditTask
      edit.appLogic.addChild(laforgeTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.addChildToTaskNode(picardTaskId, laforgeTaskId)
      }

      appLogicRoot.appStateFlow.value.taskGraph[picardTaskId] shouldBe TaskNode(
        picardTaskId,
        "Captain Picard",
        "Worlds best captain",
        children = setOf(rikerTaskId, laforgeTaskId)
      )
      appLogicRoot.appStateFlow.value.taskGraph[laforgeTaskId] shouldBe TaskNode(
        laforgeTaskId,
        "Geordi Laforge",
        "Space Engineering Master",
        parent = picardTaskId,
        blockingTasks = setOf(rikerTaskId)
      )
    }

    test("Add Parent From Edit -- New Parent") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(laforgeTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      every {
        mockStorage.addChildToTaskNode(picardTaskId, laforgeTaskId)
      } answers {
        Result.success(Unit)
      }

      val edit = appLogicRoot.appStateFlow.value.activeChild as AppLogicRoot.Child.EditTask
      edit.appLogic.setParent(picardTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.addChildToTaskNode(picardTaskId, laforgeTaskId)
      }

      appLogicRoot.appStateFlow.value.taskGraph[picardTaskId] shouldBe TaskNode(
        picardTaskId,
        "Captain Picard",
        "Worlds best captain",
        children = setOf(rikerTaskId, laforgeTaskId)
      )
      appLogicRoot.appStateFlow.value.taskGraph[laforgeTaskId] shouldBe TaskNode(
        laforgeTaskId,
        "Geordi Laforge",
        "Space Engineering Master",
        parent = picardTaskId,
        blockingTasks = setOf(rikerTaskId)
      )
    }

    test("Add Parent From Edit -- Reparent") {
      finishOnCreate()
      val graph = rootDecomposeComponent.childStack.value.active.instance
      graph as AppLogicRoot.Child.TaskGraph

      graph.appLogic.openEdit(rikerTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      every {
        mockStorage.addChildToTaskNode(laforgeTaskId, rikerTaskId)
      } answers {
        Result.success(Unit)
      }

      val edit = appLogicRoot.appStateFlow.value.activeChild as AppLogicRoot.Child.EditTask
      edit.appLogic.setParent(laforgeTaskId)
      testMainDispatcher.scheduler.advanceUntilIdle()

      verify(exactly = 1) {
        mockStorage.addChildToTaskNode(laforgeTaskId, rikerTaskId)
      }

      appLogicRoot.appStateFlow.value.taskGraph[picardTaskId] shouldBe TaskNode(
        picardTaskId,
        "Captain Picard",
        "Worlds best captain",
      )
      appLogicRoot.appStateFlow.value.taskGraph[rikerTaskId] shouldBe TaskNode(
        rikerTaskId,
        "William T Riker",
        "Beard or go home",
        parent = laforgeTaskId,
        blockedTasks = setOf(laforgeTaskId)
      )
      appLogicRoot.appStateFlow.value.taskGraph[laforgeTaskId] shouldBe TaskNode(
        laforgeTaskId,
        "Geordi Laforge",
        "Space Engineering Master",
        children = setOf(rikerTaskId),
        blockingTasks = setOf(rikerTaskId)
      )
    }

    // TODO NOW make tests for add blocking, add blocked
    // TODO NOW Add child and parent from graph view
  }

  private fun finishOnCreate() {
    // Run onCreate launched load.
    testMainDispatcher.scheduler.advanceUntilIdle()
    // Load from IO.
    testIODispatcher.scheduler.advanceUntilIdle()
    // Complete load state change.
    testMainDispatcher.scheduler.advanceUntilIdle()
  }

  private val taskNodes: Map<TaskId, TaskNode> = mapOf(
    picardTaskId to TaskNode(
      picardTaskId,
      "Captain Picard",
      "Worlds best captain",
      children = setOf(rikerTaskId)
    ),
    rikerTaskId to TaskNode(
      rikerTaskId,
      "William T Riker",
      "Beard or go home",
      parent = picardTaskId,
      blockedTasks = setOf(laforgeTaskId)
    ),
    laforgeTaskId to TaskNode(
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
