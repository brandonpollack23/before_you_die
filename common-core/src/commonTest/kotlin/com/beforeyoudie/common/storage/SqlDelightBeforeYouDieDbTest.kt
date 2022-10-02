package com.beforeyoudie.common.storage

import com.beforeyoudie.common.di.startKoin
import com.beforeyoudie.common.storage.memorymodel.TaskNode
import com.benasher44.uuid.uuidFrom
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.MockProvider

class SqlDelightBeforeYouDieDbTest : FunSpec(), KoinTest {
    private val mockProvider = MockProvider
    private val db by inject<BeforeYouDieStorageInterface>()
    init {
        beforeTest {
            startKoin()
        }
        afterTest {
            stopKoin()
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

            allResults shouldContain TaskNode(uuid1, "uuid1")
            allResults shouldContain TaskNode(uuid2, "uuid2", isComplete = true, description = "")
            allResults shouldContain TaskNode(uuid3, "uuid3", description = "captain picard baby")
        }

        // TODO NOW read more about test framework
        // TODO NOW deps test
        // TODO NOW children test
    }
}