package com.beforeyoudie.common.storage

import com.beforeyoudie.common.CommonTest
import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.benasher44.uuid.uuidFrom
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtMostSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.koin.test.get

// TODO(#3) TESTING make thsi run as android instrumentation test
class SqlDelightBeforeYouDieDbTest : CommonTest() {
  private lateinit var db: BeforeYouDieStorageInterface

  init {
    beforeTest {
      db = get()
    }

    test("database is in memory for test") {
      db.isInMemory shouldBe true
    }

    test("basic retrieval and insertion with no deps or children") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)

      val allResults = db.selectAllTaskNodeInformation()

      allResults shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2", isComplete = true, description = ""),
        TaskNode(uuid3, "uuid3", description = "captain picard baby")
      )
    }

    test("set mark complete should work") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")
      db.insertTaskNode(uuid3, "uuid3", null, false)

      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1"),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      )

      db.markComplete(uuid1)
      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3")
      )
      db.markComplete(uuid2)
      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2", true),
        TaskNode(uuid3, "uuid3")
      )
      db.markComplete(uuid3)
      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", true),
        TaskNode(uuid2, "uuid2", true),
        TaskNode(uuid3, "uuid3", true)
      )
    }

    test("parent child relation, including multiple children") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addChildToTaskNode(uuid1, uuid2)
      db.addChildToTaskNode(uuid1, uuid4)
      db.addChildToTaskNode(uuid2, uuid3)

      val allResults = db.selectAllTaskNodeInformation()

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
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addChildToTaskNode(uuid1, uuid2)
      db.addChildToTaskNode(uuid1, uuid4)
      db.addChildToTaskNode(uuid2, uuid3)

      val allResults = db.selectAllTaskNodeInformation()

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
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)

      db.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess (Unit)
      db.addChildToTaskNode(uuid3, uuid2) shouldBeFailure BYDFailure.DuplicateParent(uuid3)
    }

    test("dependencies work in a line") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", true)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addDependencyRelationship(uuid2, uuid1)
      db.addDependencyRelationship(uuid3, uuid2)
      db.addDependencyRelationship(uuid4, uuid3)

      val allResults = db.selectAllTaskNodeInformation()

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
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      // Block the deps like so:
      //      1
      //     / \
      //    2   3
      //    \   /
      //      4

      db.addDependencyRelationship(uuid2, uuid1)
      db.addDependencyRelationship(uuid3, uuid1)

      db.addDependencyRelationship(uuid4, uuid2)
      db.addDependencyRelationship(uuid4, uuid3)

      val allResults = db.selectAllTaskNodeInformation()

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
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)

      // Block the deps like so:
      // 1 -> 2
      // 2 -> 2

      db.addDependencyRelationship(uuid2, uuid1) shouldBeSuccess Unit
      db.addDependencyRelationship(
        uuid1,
        uuid2
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(uuid1, uuid2)
    }

    test("dependencies fail with a larger loop") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", null, false)

      // Block the deps like so:
      // 1 -> 2 -> 3
      // ^         |
      //  \--------/

      db.addDependencyRelationship(uuid1, uuid2) shouldBeSuccess Unit
      db.addDependencyRelationship(uuid2, uuid3) shouldBeSuccess Unit
      db.addDependencyRelationship(uuid3, uuid1)
        .shouldBeFailure<BYDFailure.OperationWouldIntroduceCycle>()
    }

    test("child parent relationship fail with a loop") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", null, false)

      // Parent the deps like so:
      // 1 -> 2 -> 3
      // ^         |
      //  \--------/

      db.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess Unit
      db.addChildToTaskNode(uuid2, uuid3) shouldBeSuccess Unit
      db.addChildToTaskNode(uuid3, uuid1) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(
        uuid3,
        uuid1
      )
    }

    test("select all actionable selects non blocked nodes and top level nodes") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addDependencyRelationship(uuid1, uuid2)
      db.addDependencyRelationship(uuid1, uuid3)
      db.addDependencyRelationship(uuid2, uuid4)
      db.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )

      db.markComplete(uuid0) shouldBeSuccess Unit
      db.markComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
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

      db.markComplete(uuid2) shouldBeSuccess Unit
      // only 3 should still be actionable, since 4 is blocked by 3 still
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(
          uuid3,
          "uuid3",
          description = "captain picard baby",
          blockingTasks = setOf(uuid1),
          blockedTasks = setOf(uuid4)
        )
      )

      db.markComplete(uuid3) shouldBeSuccess Unit
      // now 4 is opened up!
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(
          uuid4,
          "uuid4",
          description = "no love for worf",
          blockingTasks = setOf(uuid3, uuid2)
        )
      )

      db.markComplete(uuid4) shouldBeSuccess Unit
      // now 4 is opened up!
      db.selectAllActionableTaskNodeInformation() shouldHaveAtMostSize 0
    }

    test("marking incomplete should force dependent tasks to no longer be visibile") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addDependencyRelationship(uuid1, uuid2)
      db.addDependencyRelationship(uuid1, uuid3)
      db.addDependencyRelationship(uuid2, uuid4)
      db.addDependencyRelationship(uuid3, uuid4)

      // First only 1 should be actionable
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )

      db.markComplete(uuid0) shouldBeSuccess Unit
      db.markComplete(uuid1) shouldBeSuccess Unit
      // Now 2 and 3 should be actionable, since 4 is blocked and 1 is complete.
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
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

      db.markIncomplete(uuid1) shouldBeSuccess Unit
      db.selectAllActionableTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid1, "uuid1", blockedTasks = setOf(uuid2, uuid3))
      )
    }

    test("reparenting operation completes successfully") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)

      db.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      db.reparentChildToTaskNode(uuid2, uuid1) shouldBeSuccess Unit

      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid0, "uuid0"),
        TaskNode(uuid1, "uuid1", parent = uuid2),
        TaskNode(uuid2, "uuid2", children = setOf(uuid1))
      )
    }

    test("reparenting operation fails when there is no existing parent") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)

      db.reparentChildToTaskNode(uuid2, uuid1) shouldBeFailure BYDFailure.ChildHasNoParent(uuid1)
    }

    test("reparenting operation fails when creates a cycle") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", null, false)

      db.addChildToTaskNode(uuid0, uuid1) shouldBeSuccess Unit
      db.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess Unit
      db.addChildToTaskNode(uuid2, uuid3) shouldBeSuccess Unit
      db.reparentChildToTaskNode(
        uuid3,
        uuid1
      ) shouldBeFailure BYDFailure.OperationWouldIntroduceCycle(uuid3, uuid1)
    }

    test("remove task node removes task, children, and blocked/blocking info") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", null, false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597")
      db.insertTaskNode(uuid4, "uuid4", null, false)

      db.addChildToTaskNode(uuid0, uuid1)
      db.addChildToTaskNode(uuid3, uuid0)
      db.addDependencyRelationship(uuid0, uuid2)
      db.addDependencyRelationship(uuid4, uuid0)

      db.removeTaskNodeAndChildren(uuid0)

      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
        TaskNode(uuid2, "uuid2"),
        TaskNode(uuid3, "uuid3"),
        TaskNode(uuid4, "uuid4")
      )
    }

    test("remve task recursively removes all chilrdren") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", null, false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", null, false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-504fb9ea3597")
      db.insertTaskNode(uuid4, "uuid4", null, false)

      db.addChildToTaskNode(uuid0, uuid1)
      db.addChildToTaskNode(uuid1, uuid2)
      db.addChildToTaskNode(uuid2, uuid3)
      db.addChildToTaskNode(uuid3, uuid4)

      db.removeTaskNodeAndChildren(uuid0) shouldBeSuccess Unit
      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf()
    }

    test("remove nonexistant node fails") {
      val uuid0 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3589")
      db.insertTaskNode(uuid0, "uuid0", null, false)
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3588")

      db.removeTaskNodeAndChildren(uuid1) shouldBeFailure BYDFailure.NonExistentNodeId(uuid1)
    }

    test("remove dependency relationship works") {
      val uuid1 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3599")
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addDependencyRelationship(uuid2, uuid1)
      db.addDependencyRelationship(uuid3, uuid2)
      db.addDependencyRelationship(uuid4, uuid3)

      db.removeDependencyRelationship(uuid3, uuid2) shouldBeSuccess Unit

      db.selectAllTaskNodeInformation() shouldContainExactlyInAnyOrder setOf(
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
      db.insertTaskNode(uuid1, "uuid1", null, false)
      val uuid2 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3598")
      db.insertTaskNode(uuid2, "uuid2", "", false)
      val uuid3 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3597")
      db.insertTaskNode(uuid3, "uuid3", "captain picard baby", false)
      val uuid4 = uuidFrom("3d7f7dd6-c345-49a8-aa1d-404fb9ea3596")
      db.insertTaskNode(uuid4, "uuid4", "no love for worf", false)

      db.addDependencyRelationship(uuid2, uuid1)
      db.addDependencyRelationship(uuid3, uuid2)
      db.addDependencyRelationship(uuid4, uuid3)

      db.removeDependencyRelationship(
        uuid2,
        uuid3
      ) shouldBeFailure BYDFailure.NoSuchDependencyRelationship(uuid2, uuid3)
    }
  }
}
