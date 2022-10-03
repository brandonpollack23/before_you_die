package com.beforeyoudie.common.util

object ResultExt {
    public fun <T> asResult(errorConstructor: (Throwable) -> BYDResult, c: () -> T): Result<T> {
        return try {
            Result.success(c())
        } catch (e: Exception) {
            Result.failure(errorConstructor(e))
        }
    }
}

sealed class BYDResult(cause: Throwable) : Throwable(cause) {
    class InsertionFailure(inner: Throwable) : BYDResult(inner)
    class DuplicateParent(inner: Throwable) : BYDResult(inner)
}