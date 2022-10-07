package com.beforeyoudie.common.storage

import com.beforeyoudie.CommonTest
import com.beforeyoudie.common.di.TestBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.create
import com.beforeyoudie.common.state.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.benasher44.uuid.uuidFrom
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtMostSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import me.tatarka.inject.annotations.Component

// TODO(#3) TESTING make also make run as android instrumentation test

@Component
abstract class SqlDelightStorageTestComponent(
  @Component val parent: TestBydKotlinInjectAppComponent =
    TestBydKotlinInjectAppComponent::class.create()
) {
  abstract val storage: IBydStorage
}

class SqlDelightStorageTest : CommonTest() {
  lateinit var storage: IBydStorage

  init {
    beforeTest {
      storage = SqlDelightStorageTestComponent::class.create().storage
    }

    test("database is in memory for test") {
      storage.isInMemory shouldBe true
    }

    test("basic retrieval and insertion with no deps or children") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2", isComplete = true, description = ""),
        TaskNode(uuid3, "uuid3", description = "captain picard baby")
      )
    }

    test("update title") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      )

      storage.updateTaskTitle(uuid1, "new uuid1 title") shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "new uuid1 title"),
        TaskNode(uuid2, "uuid2")
      )
    }

    test("update title fail") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d8f7dd6-c345-49a8-aa1d-404fb9ea3598")

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      )

      storage.updateTaskTitle(uuid3, "new uuid1 title") shouldBeFailure
        BYDFailure.NonExistentNodeId(uuid3)
    }

    test("update description") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      )

      storage.updateTaskDescription(uuid1, "new uuid1 description") shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", description = "new uuid1 description"),
        TaskNode(uuid2, "uuid2")
      )
    }

    test("update descprption fail") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d8f7dd6-c345-49a8-aa1d-404fb9ea3598")

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2")
      )

      storage.updateTaskDescription(uuid3, "new uuid1 description") shouldBeFailure
        BYDFailure.NonExistentNodeId(uuid3)
    }

    test("set mark complete should work") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")
      storage.insertTaskNode(uuid3, "uuid3", null, false)

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      )

      storage.markComplete(uuid1)
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      )
      storage.markComplete(uuid2)
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2", true),
        TaskNode(uuid3, "uuid3")
      )
      storage.markComplete(uuid3)
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2", true),
        TaskNode(uuid3, "uuid3", true)
      )
    }

    test("parent child relation, including multiple children") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid1, uuid4)
      storage.addChildToTaskNode(uuid2, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactlyInAnyOrder
        setOf(
          TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
          TaskNode(
            uuid2,
            "uuid2",
            isComplete = true,
            description = "",
            parent = uuid1,
            children = setOf(uuid3)
          ),
          TaskNode(uuid3, "uuid3", description = "captain picard baby", parent = uuid2),
          TaskNode(uuid4, "uuid4", description = "no love for worf", parent = uuid1)
        )
    }

    test("parent child relation, including multiple children using properties") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid1, uuid4)
      storage.addChildToTaskNode(uuid2, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactlyInAnyOrder
        setOf(
          TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
          TaskNode(
            uuid2,
            "uuid2",
            isComplete = true,
            description = "",
            parent = uuid1,
            children = setOf(uuid3)
          ),
          TaskNode(uuid3, "uuid3", description = "captain picard baby", parent = uuid2),
          TaskNode(uuid4, "uuid4", description = "no love for worf", parent = uuid1)
        )
    }

    test("child can only have one parent") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)

      storage.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess (Unit)
      storage.addChildToTaskNode(uuid3, uuid2) shouldBeFailure BYDFailure.DuplicateParent(uuid3)
    }

    test("dependencies work in a line") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      val allResults = storage.selectAllTaskNodeInformation()

      allResults shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2)),
        TaskNode(
          uuid2,
          "uuid2",
          isComplete = true,
          description = "",
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
      )
    }

    test("dependencies work diamond (one to many and many to one)") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

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

      allResults shouldContainExactlyInAnyOrder setOf(
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
      )
    }

    test("dependencies fail with a small loop") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)

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
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", null, false)

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
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", null, false)

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
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addDependencyRelationship(uuid1, uuid2)
      storage.addDependencyRelationship(uuid1, uuid3)
      storage.addDependencyRelationship(uuid2, uuid4)
      storage.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )

      storage.markComplete(uuid0) shouldBeSuccess Unit
      storage.markComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
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
      )

      storage.markComplete(uuid2) shouldBeSuccess Unit
      // only 3 should still be actionable, since 4 is blocked by 3 still
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        )
      )

      storage.markComplete(uuid3) shouldBeSuccess Unit
      // now 4 is opened up!
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(
          uuid4,
          "uuid4",
          description = "no love for worf",
          blockingTasks = setOf(uuid3, uuid2)
        )
      )

      storage.markComplete(uuid4) shouldBeSuccess Unit
      // now 4 is opened up!
      storage.selectAllActionableTaskNodeInformation() shouldHaveAtMostSize 0
    }

    test("marking incomplete should force dependent tasks to no longer be visibile") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addDependencyRelationship(uuid1, uuid2)
      storage.addDependencyRelationship(uuid1, uuid3)
      storage.addDependencyRelationship(uuid2, uuid4)
      storage.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )

      storage.markComplete(uuid0) shouldBeSuccess Unit
      storage.markComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
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
      )

      storage.markIncomplete(uuid1) shouldBeSuccess Unit
      storage.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )
    }

    test("reparenting operation completes successfully") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)

      storage.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      storage.reparentChildToTaskNode(uuid2, uuid1) shouldBeSuccess Unit

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", parent = uuid2),
        TaskNode(uuid2, "uuid2", children = setOf(uuid1))
      )
    }

    test("reparenting operation fails when there is no existing parent") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)

      storage.reparentChildToTaskNode(uuid2, uuid1) shouldBeFailure BYDFailure.ChildHasNoParent(
        uuid1
      )
    }

    test("reparenting operation fails when creates a cycle") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", null, false)

      storage.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      storage.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess Unit
      storage.addChildToTaskNode(uuid2, uuid3) shouldBeSuccess Unit
      storage.reparentChildToTaskNode(
        uuid3,
        uuid1
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(uuid3, uuid1)
    }

    test("remove task node removes task, children, and blocked/blocking info") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", null, false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597")
      storage.insertTaskNode(uuid4, "uuid4", null, false)

      storage.addChildToTaskNode(uuid0, uuid1)
      storage.addChildToTaskNode(uuid3, uuid0)
      storage.addDependencyRelationship(uuid0, uuid2)
      storage.addDependencyRelationship(uuid4, uuid0)

      storage.removeTaskNodeAndChildren(uuid0)

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3"),
        TaskNode(uuid4, "uuid4")
      )
    }

    test("remve task recursively removes all chilrdren") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", null, false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597")
      storage.insertTaskNode(uuid4, "uuid4", null, false)

      storage.addChildToTaskNode(uuid0, uuid1)
      storage.addChildToTaskNode(uuid1, uuid2)
      storage.addChildToTaskNode(uuid2, uuid3)
      storage.addChildToTaskNode(uuid3, uuid4)

      storage.removeTaskNodeAndChildren(uuid0) shouldBeSuccess Unit
      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf()
    }

    test("remove nonexistant node fails") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      storage.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")

      storage.removeTaskNodeAndChildren(uuid1) shouldBeFailure BYDFailure.NonExistentNodeId(uuid1)
    }

    test("remove dependency relationship works") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      storage.addDependencyRelationship(uuid2, uuid1)
      storage.addDependencyRelationship(uuid3, uuid2)
      storage.addDependencyRelationship(uuid4, uuid3)

      storage.removeDependencyRelationship(uuid3, uuid2) shouldBeSuccess Unit

      storage.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2)),
        TaskNode(uuid2, "uuid2", description = "", blockedTasks = setOf(uuid1)),
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid4)
        ),
        TaskNode(uuid4, "uuid4", description = "no love for worf", blockedTasks = setOf(uuid3))
      )
    }

    test("fails to remove non existant dep") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      storage.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      storage.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      storage.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      storage.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

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