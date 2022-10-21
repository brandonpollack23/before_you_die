package com.beforeyoudie.common.storage

import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DecomposeAppLogicComponent
import com.beforeyoudie.common.di.JvmDesktopPlatformSqlDelightStorageComponent
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.di.createTestPlatformComponent
import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.benasher44.uuid.uuidFrom
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import me.tatarka.inject.annotations.Component

// TODO(#3) TESTING make also make run as android instrumentation test: https://kotest.io/docs/extensions/robolectric.html

@Component
abstract class SqlDelightStorageTestComponent(
  @Component val bydKotlinInjectAppComponent: BydKotlinInjectAppComponent
) {
  abstract val storage: IBydStorage
}

fun sqlDelightStorageTestComponent() = run {
  val platformComponent = createTestPlatformComponent()
  val storageComponent = JvmDesktopPlatformSqlDelightStorageComponent::class.create()
  val appLogicComponent =
    DecomposeAppLogicComponent::class.create(storageComponent, platformComponent)

  val component = BydKotlinInjectAppComponent::class.create(
    platformComponent,
    storageComponent,
    appLogicComponent
  )

  SqlDelightStorageTestComponent::class.create(component)
}

class SqlDelightStorageTest : CommonTest() {
  lateinit var storage: IBydStorage

  init {
    beforeTest {
      storage = sqlDelightStorageTestComponent().storage
    }

    test("database is in memory for test") {
      storage.isInMemory shouldBe true
    }

    test("basic retrieval and insertion with no deps and a child") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = true)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588"))
      storage.insertTaskNode(
        uuid3,
        "uuid3",
        "captain picard baby",
        parent = uuid2,
        complete = false
      )

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2", description = "", isComplete = true, children = setOf(uuid3)),
        TaskNode(uuid3, "uuid3", description = "captain picard baby", parent = uuid2)
      ).associateBy { it.id }
    }

    test("update title") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }

      storage.updateTaskTitle(uuid1, "new uuid1 title") shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "new uuid1 title"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }
    }

    test("update title fail") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d8f7dd6-c345-49a8-aa1d-404fb9ea3598"))

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }

      storage.updateTaskTitle(uuid3, "new uuid1 title") shouldBeFailure
        BYDFailure.NonExistentNodeId(uuid3)
    }

    test("update description") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }

      storage.updateTaskDescription(uuid1, "new uuid1 description") shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", description = "new uuid1 description"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }
    }

    test("update descprption fail") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d8f7dd6-c345-49a8-aa1d-404fb9ea3598"))

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      ).associateBy { it.id }

      storage.updateTaskDescription(uuid3, "new uuid1 description") shouldBeFailure
        BYDFailure.NonExistentNodeId(uuid3)
    }

    test("set mark complete should work") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid1)
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", isComplete = true),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      ).associateBy { it.id }
      storage.markTaskAndChildrenComplete(uuid2)
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", isComplete = true),
        TaskNode(uuid2, "uuid2", isComplete = true),
        TaskNode(uuid3, "uuid3")
      ).associateBy { it.id }
      storage.markTaskAndChildrenComplete(uuid3)
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", isComplete = true),
        TaskNode(uuid2, "uuid2", isComplete = true),
        TaskNode(uuid3, "uuid3", isComplete = true)
      ).associateBy { it.id }
    }

    test("set mark complete should work with children") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)

      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid2, uuid3)

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", children = setOf(uuid2)),
        TaskNode(uuid2, "uuid2", parent = uuid1, children = setOf(uuid3)),
        TaskNode(uuid3, "uuid3", parent = uuid2)
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid1)
      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", children = setOf(uuid2), isComplete = true),
        TaskNode(uuid2, "uuid2", parent = uuid1, children = setOf(uuid3), isComplete = true),
        TaskNode(uuid3, "uuid3", parent = uuid2, isComplete = true)
      ).associateBy { it.id }
    }

    test("parent child relation, including multiple children") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = true)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid1, uuid4)
      storage.addChildToTaskNode(uuid2, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          isComplete = true,
          parent = uuid1,
          children = setOf(uuid3)
        ),
        TaskNode(uuid3, "uuid3", description = "captain picard baby", parent = uuid2),
        TaskNode(uuid4, "uuid4", description = "no love for worf", parent = uuid1)
      ).associateBy { it.id }
    }

    test("parent child relation, including multiple children using properties") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = true)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid1, uuid4)
      storage.addChildToTaskNode(uuid2, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          isComplete = true,
          parent = uuid1,
          children = setOf(uuid3)
        ),
        TaskNode(uuid3, "uuid3", description = "captain picard baby", parent = uuid2),
        TaskNode(uuid4, "uuid4", description = "no love for worf", parent = uuid1)
      ).associateBy { it.id }
    }

    test("child can only have one parent") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = true)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)

      storage.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess (Unit)
      storage.addChildToTaskNode(uuid3, uuid2) shouldBeFailure BYDFailure.DuplicateParent(uuid3)
    }

    test("dependencies work in a line") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = true)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2)),
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          isComplete = true,
          blockingTasks = setOf(uuid3),
          blockedTasks = setOf(uuid1)
        ),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid4),
          blockedTasks = setOf(uuid2)
        ),
        TaskNode(
          uuid4,
          "uuid4",
          description = "no love for worf",
          blockedTasks = setOf(uuid3)
        )
      ).associateBy { it.id }
    }

    test("dependencies work diamond (one to many and many to one)") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      // Block the deps like so:
      //      1
      //     / \
      //    2   3
      //    \   /
      //      4

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid1)

      storage.addDependencyRelationship(uuid4, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2, uuid3)),
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          blockingTasks = setOf(uuid4),
          blockedTasks = setOf(uuid1)
        ),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid4),
          blockedTasks = setOf(uuid1)
        ),
        TaskNode(
          uuid4,
          "uuid4",
          description = "no love for worf",
          blockedTasks = setOf(uuid3, uuid2)
        )
      ).associateBy { it.id }
    }

    test("dependencies fail with a small loop") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)

      // Block the deps like so:
      // 1 -> 2
      // 2 -> 2

      storage.addDependencyRelationship(uuid2, uuid1) shouldBeSuccess Unit
      storage.addDependencyRelationship(
        uuid1,
        uuid2
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(uuid1, uuid2)
    }

    test("dependencies fail with a larger loop") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)

      // Block the deps like so:
      // 1 -> 2 -> 3
      // ^         |
      //  \--------/

      storage.addDependencyRelationship(uuid1, uuid2) shouldBeSuccess Unit
      storage.addDependencyRelationship(uuid2, uuid3) shouldBeSuccess Unit
      storage.addDependencyRelationship(uuid3, uuid1)
        .shouldBeFailure<BYDFailure.OperationWouldIntroduceCycle>()
    }

    test("child parent relationship fail with a loop") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)

      // Parent the deps like so:
      // 1 -> 2 -> 3
      // ^         |
      //  \--------/

      storage.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess Unit
      storage.addChildToTaskNode(uuid2, uuid3) shouldBeSuccess Unit
      storage.addChildToTaskNode(
        uuid3,
        uuid1
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(
        uuid3,
        uuid1
      )
    }

    test("select all actionable selects non blocked nodes and top level nodes") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addDependencyRelationship(uuid1, uuid2)
      storage.addDependencyRelationship(uuid1, uuid3)
      storage.addDependencyRelationship(uuid2, uuid4)
      storage.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid0) shouldBeSuccess Unit
      storage.markTaskAndChildrenComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        ),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        )
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid2) shouldBeSuccess Unit
      // only 3 should still be actionable, since 4 is blocked by 3 still
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        )
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid3) shouldBeSuccess Unit
      // now 4 is opened up!
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(
          uuid4,
          "uuid4",
          description = "no love for worf",
          blockingTasks = setOf(uuid3, uuid2)
        )
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid4) shouldBeSuccess Unit
      // now 4 is opened up!
      storage.selectAllActionableTaskNodeInformation() shouldHaveSize 0
    }

    test("marking incomplete should force dependent tasks to no longer be visibile") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addDependencyRelationship(uuid1, uuid2)
      storage.addDependencyRelationship(uuid1, uuid3)
      storage.addDependencyRelationship(uuid2, uuid4)
      storage.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      ).associateBy { it.id }

      storage.markTaskAndChildrenComplete(uuid0) shouldBeSuccess Unit
      storage.markTaskAndChildrenComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(
          uuid2,
          "uuid2",
          description = "",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        ),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        )
      ).associateBy { it.id }

      storage.markTaskAndChildrenIncomplete(uuid1) shouldBeSuccess Unit
      storage.selectAllActionableTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      ).associateBy { it.id }
    }

    test("reparenting operation completes successfully") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)

      storage.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      storage.reparentChildToTaskNode(uuid2, uuid1) shouldBeSuccess Unit

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", parent = uuid2),
        TaskNode(uuid2, "uuid2", children = setOf(uuid1))
      ).associateBy { it.id }
    }

    test("reparenting operation fails when there is no existing parent") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)

      storage.reparentChildToTaskNode(uuid2, uuid1) shouldBeFailure BYDFailure.ChildHasNoParent(
        uuid1
      )
    }

    test("reparenting operation fails when creates a cycle") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)

      storage.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      storage.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess Unit
      storage.addChildToTaskNode(uuid2, uuid3) shouldBeSuccess Unit
      storage.reparentChildToTaskNode(
        uuid3,
        uuid1
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(uuid3, uuid1)
    }

    test("remove task node removes task, children, and blocked/blocking info") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597"))
      storage.insertTaskNode(uuid4, "uuid4", null, complete = false)

      storage.addChildToTaskNode(uuid0, uuid1)
      storage.addChildToTaskNode(uuid3, uuid0)
      storage.addDependencyRelationship(uuid0, uuid2)
      storage.addDependencyRelationship(uuid4, uuid0)

      storage.removeTaskNodeAndChildren(uuid0)

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3"),
        TaskNode(uuid4, "uuid4")
      ).associateBy { it.id }
    }

    test("remve task recursively removes all chilrdren") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", null, complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", null, complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597"))
      storage.insertTaskNode(uuid4, "uuid4", null, complete = false)

      storage.addChildToTaskNode(uuid0, uuid1)
      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid2, uuid3)
      storage.addChildToTaskNode(uuid3, uuid4)

      storage.removeTaskNodeAndChildren(uuid0) shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactly emptyMap()
    }

    test("remove nonexistant node fails") {
      val uuid0 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589"))
      storage.insertTaskNode(uuid0, "uuid0", null, complete = false)
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588"))

      storage.removeTaskNodeAndChildren(uuid1) shouldBeFailure BYDFailure.NonExistentNodeId(uuid1)
    }

    test("remove dependency relationship works") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      storage.removeDependencyRelationship(uuid3, uuid2) shouldBeSuccess Unit

      storage.selectAllTaskNodeInformation() shouldContainExactly setOf(
        TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2)),
        TaskNode(uuid2, "uuid2", description = "", blockedTasks = setOf(uuid1)),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid4)
        ),
        TaskNode(uuid4, "uuid4", description = "no love for worf", blockedTasks = setOf(uuid3))
      ).associateBy { it.id }
    }

    test("fails to remove non existant dep") {
      val uuid1 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599"))
      storage.insertTaskNode(uuid1, "uuid1", null, complete = false)
      val uuid2 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598"))
      storage.insertTaskNode(uuid2, "uuid2", "", complete = false)
      val uuid3 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597"))
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", complete = false)
      val uuid4 = TaskId(uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596"))
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", complete = false)

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      storage.removeDependencyRelationship(
        uuid2,
        uuid3
      ) shouldBeFailure BYDFailure.NoSuchDependencyRelationship(uuid2, uuid3)
    }
  }
}
