package com.beforeyoudie.common.util

import co.touchlab.kermit.Logger

actual inline fun <reified T> getClassLogger() = Logger.withTag(T::class.toString())