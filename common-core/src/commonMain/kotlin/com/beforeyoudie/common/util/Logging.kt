package com.beforeyoudie.common.util

import co.touchlab.kermit.Logger

/**
 * Gets a class logger using the name of the class.
 */
expect inline fun <reified T> getClassLogger(): Logger
