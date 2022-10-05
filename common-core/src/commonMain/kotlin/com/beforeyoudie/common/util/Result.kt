package com.beforeyoudie.common.util

import com.benasher44.uuid.Uuid

/**
 * Extension functions for [Result]
 */
object ResultExt {
  /**
   * Surround a block in a try catch and return the given failure on thrown exception.
   *
   * @param errorConstructor the constructor to call to wrap the Throwable called in [c]
   * @param c the block that will be surrounded in try catch.
   */
  fun <T> asResult(errorConstructor: (Throwable) -> BYDFailure, c: () -> T): Result<T> {
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

  /** These two tasks don't have a dependency relationship, so nothing to remove, etc. */
  data class NoSuchDependencyRelationship(val blockingTask: Uuid, val blockedTask: Uuid) :
    BYDFailure()
}
