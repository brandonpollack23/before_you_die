package com.beforeyoudie.common.util

import co.touchlab.kermit.Logger

actual inline fun <reified T> T.getClassLogger(): Logger = Logger.withTag(T::class.java.name)
