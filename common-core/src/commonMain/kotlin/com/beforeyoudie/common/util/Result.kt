package com.beforeyoudie.common.util

import com.benasher44.uuid.Uuid

object ResultExt {
    public fun <T> asResult(errorConstructor: (Throwable) -> BYDFailure, c: () -> T): Result<T> {
        return try {
            Result.success(c())
        } catch (e: Exception) {
            Result.failure(errorConstructor(e))
        }
    }
}

sealed class BYDFailure(cause: Throwable? = null) : Throwable(cause) {
    /** There was a failure on insertion caused by {@param inner} */
    class InsertionFailure(inner: Throwable) : BYDFailure(inner)
    /** A child can only have one parent, remove existing relationship first or use the reparent operation. */
    class DuplicateParent(inner: Throwable) : BYDFailure(inner)
    /** Operation tried to use a node id that didn't exist */
    class NonExistentNodeId(val uuid: Uuid) : BYDFailure()
    /** Cycles aren't allowed, the dependency graph is a DAG */
    class OperationWouldIntroduceCycle(val uuid1: Uuid, val uuid2: Uuid) : BYDFailure()
}
