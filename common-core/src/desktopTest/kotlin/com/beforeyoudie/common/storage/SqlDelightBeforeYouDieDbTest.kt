package com.beforeyoudie.common.storage

import com.beforeyoudie.common.CommonTest
import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.beforeyoudie.common.util.BYDFailure
import com.benasher44.uuid.uuidFrom
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.koin.test.get

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

            val allResults = db.getAllTaskNodeInformation()

            allResults shouldContainExactlyInAnyOrder setOf(
                TaskNode(uuid1, "uuid1"),
                TaskNode(uuid2, "uuid2", isComplete = true, description = ""),
                TaskNode(uuid3, "uuid3", description = "captain picard baby")
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

            val allResults = db.getAllTaskNodeInformation()

            allResults shouldContainExactlyInAnyOrder
                setOf(
                    TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
                    TaskNode(uuid2, "uuid2", isComplete = true, description = "", parent = uuid1, children = setOf(uuid3)),
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

            val allResults = db.getAllTaskNodeInformation()

            allResults shouldContainExactlyInAnyOrder
                setOf(
                    TaskNode(uuid1, "uuid1", children = setOf(uuid2, uuid4)),
                    TaskNode(uuid2, "uuid2", isComplete = true, description = "", parent = uuid1, children = setOf(uuid3)),
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

            db.addChildToTaskNode(uuid1, uuid2) shouldBeSuccess(Unit)
            val result = db.addChildToTaskNode(uuid3, uuid2)
            result.shouldBeFailure<BYDFailure.DuplicateParent>()
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

            db.addDependencyRelationship(uuid1, uuid2)
            db.addDependencyRelationship(uuid2, uuid3)
            db.addDependencyRelationship(uuid3, uuid4)

            val allResults = db.getAllTaskNodeInformation()

            allResults shouldContainExactlyInAnyOrder setOf(
                TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2)),
                TaskNode(uuid2, "uuid2", isComplete = true, description = "", blockingTasks = setOf(uuid3), blockedTasks = setOf(uuid1)),
                TaskNode(uuid3, "uuid3", description = "captain picard baby", blockingTasks = setOf(uuid4), blockedTasks = setOf(uuid2)),
                TaskNode(uuid4, "uuid4", description = "no love for worf", blockedTasks = setOf(uuid3))
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

            db.addDependencyRelationship(uuid1, uuid2)
            db.addDependencyRelationship(uuid1, uuid3)

            db.addDependencyRelationship(uuid2, uuid4)
            db.addDependencyRelationship(uuid3, uuid4)

            val allResults = db.getAllTaskNodeInformation()

            allResults shouldContainExactlyInAnyOrder setOf(
                TaskNode(uuid1, "uuid1", blockingTasks = setOf(uuid2, uuid3)),
                TaskNode(uuid2, "uuid2", description = "", blockingTasks = setOf(uuid4), blockedTasks = setOf(uuid1)),
                TaskNode(uuid3, "uuid3", description = "captain picard baby", blockingTasks = setOf(uuid4), blockedTasks = setOf(uuid1)),
                TaskNode(uuid4, "uuid4", description = "no love for worf", blockedTasks = setOf(uuid3, uuid2))
            )
        }

        // TODO NOW deps test with loop

        // TODO TESTING change to property testing
    }
}