# Module android

This is the android application. It should run on the ARC++ system on ChromeOS as well (That will probably be the first
physical device I test it on since I work on ARC++)

There's really nothing special here, it instantiates an Application to initialize the DI (and background syncing once
implemented) then utilizes that in an activity to display the UI using Jetpack Compose from the common-ui package.
