package com.beforeyoudie.di

import org.koin.core.context.startKoin

fun startKoin() = startKoin {

}

expect fun loadPlatformSpecificModules()