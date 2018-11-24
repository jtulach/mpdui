## UI for Music Player Daemon

[![Build Status](https://travis-ci.org/jtulach/mpdui.svg?branch=master)](https://travis-ci.org/jtulach/mpdui)

I am in desparate need to control my [MPD](https://www.musicpd.org) server
from iPad and iPhone. This is an attempt to write such UI. When at it,
I plan to make such UI portable - e.g. run it on desktop, Android and iOS.

Don't get me wrong, [MPDroid](https://play.google.com/store/apps/details?id=com.namelessdev.mpdroid)
is great and works fine, but it just doesn't run on iOS devices, so I have
to do something...

#### Building for iOS

Currently one needs [PR-61](https://github.com/finnyb/javampd/pull/61) to
recompile `javampd` library for iOS ready RoboVM. Then one can:

```bash
$ mvn clean install -DskipTests
$ mvn -f client-ios/ robovm:iphone-sim
$ mvn -f client-ios/ robovm:ios-device
```
