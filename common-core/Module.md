# Module common-core

This is the shared code module that is shared amongst all platforms, excluding any shared UI.

It is possible iOS application and UI will be implemented as a shared module as well, but it may not be so I've 
kept them separate.

# Package com.beforeyoudie.common.di

DI related setup using Koin

# Package com.beforeyoudie.common.storage

Storage related interface and it's implementations (sqlite only for now)

## SqlDelight

Right now the only storage implementation is using sqldelight, but one could potentially have a syncing wrapper
separately, firebase, MongoDB Room, etc.

# Package com.beforeyoudie.common.memorymodel

The in memory representation of a task and related utilities

# Package com.beforeyoudie.common.applogic

This is where the core business logic goes of how you interact with the application.  

## Naming 

Most people probably call this "viewmodel" classically or "controller"

These are all so overloaded and only server to confuse everyone. This holds state and events and talks to the
storage/syncing for you. UI depends on this.

# Package com.beforeyoudie.common.util

Utility functions for result, logging, etc.
