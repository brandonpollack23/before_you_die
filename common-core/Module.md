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

# Basic Overview

## Important Libraries

The main libraries I used in the (initial) implementation areas follows:
* kotlin-inject for dependency injection (not Koin, which is more of a service locator, I wanted things to be more magical for some reason)
* kotlinx.coroutines for concurrency and flow management.
* SqlDelight for storage.
  * This library parses my *.sq files (which contain plain sqlite dialect sql definitions of tables and queries with some labels) and generates type safe access code.
  * Then you supply the (platform specific) database driver (the bit that actually interacts with SQL, in my case SQLite on all platforms).
  * Finally, on Android I wanted to be sure I was using the later versions of SQLDelight so I can use their features (like JSON later for making my sync operation entries, spellfix1, and FTS4/5 for searching for notes), for this I use requery sqlite library.
* Decompose for AppLogic (what I've come to realize most people call BLOC -- Basic Logic Component).  This provides navigation support, persistence on android, lifecycle management, etc.

## Operational overview

The app is constructed and injected up with kotlin inject (see the di package), and then provided to each platform as a di Component.
After this, i can pull out the app logic StateFlow and use that as an immutable source of truth for state in the UI.  
The AppLogic exposes methods to mutate this state, which updates the object contained within this flow, and can 
therefore be redrawn by a declarative UI framework efficiently.