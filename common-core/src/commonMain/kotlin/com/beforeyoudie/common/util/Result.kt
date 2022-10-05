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
  data class InsertionFailure(private val inner: Throwable) : BYDFailure(inner)

  /** A child can only have one parent, remove existing relationship first or use the reparent operation. */
  data class DuplicateParent(private val uuid: Uuid) : BYDFailure()

  /** Moving a child to a new parent requires it already has one. */
  data class ChildHasNoParent(val uuid: Uuid) : BYDFailure()

  /** Operation tried to use a node id that didn't exist */
  data class NonExistentNodeId(val uuid: Uuid) : BYDFailure()

  /** Cycles aren't allowed, the dependency graph is a DAG */
  data class OperationWouldIntroduceCycle(val uuid1: Uuid, val uuid2: Uuid) : BYDFailure()

  /** These two tasks don't have a depenency relationship, so nothing to remove, etc. */
  data class NoSuchDependencyRelationship(val blockingTask: Uuid, val blockedTask: Uuid) :
    BYDFailure()
}
