package com.beforeyoudie.common.util

import co.touchlab.kermit.Logger

expect inline fun <reified T> T.getClassLogger(): Logger
