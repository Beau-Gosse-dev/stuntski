# StuntSki
This is the official source code for the [StuntSki game](https://deaddropgames.com/stuntski/) mobile game that can be 
found on 
[Google Play](https://play.google.com/store/apps/details?id=com.deaddropgames.stuntmountain.android&hl=en_CA&gl=US) and 
[Apple App Store](https://apps.apple.com/fi/app/stuntski-lite/id961485093). This repository includes the core game files and a desktop version of the game only. The mobile 
projects are not included (mostly because I wanted to avoid accidentally exposing the publishing secrets). If you want
to generate your own mobile version, it should be relatively easy to do so by following libGDX's documentation. To 
create the iOS version, I used [RoboVM](https://github.com/MobiVM/robovm).

This game was inspired by [Ski Stunt Simulator](https://www.cs.ubc.ca/~van/sssjava/javademo.html); I used to play the
original when I was procrastinating studying in University. I wanted to recreate the game for mobile devices and this is
the result. After many years of procrastinating open sourcing the code, I finally got around to it. The code is likely
poorly documented and not perfectly written, but it works. I look forward to anyone who wants to contribute to the code.

**NOTE:** I was going to originally call this game "Stunt Mountain" but I found out that name was already in use when I
was ready to publish, and I never ended up changing the Java package names. So the package names are 
`com.deaddropgames.stuntmountain.*`, just FYI.

## Building and Running
This is project managed by the build tool [gradle](https://gradle.org/). Easiest way to build and run it is to install
IntelliJ IDEA and import the project. IntelliJ will automatically download gradle and all the dependencies. You can then
navigate to the `Desktop` project, open the `DesktopLauncher` class and click the green play button to run the game.

I think any version of Java above 1.6 should work.

**NOTE:** As this was designed for mobile devices, the UI list screens are scrolled by dragging the screen up and down.

## Credits
### UI Graphics
UI Graphics by _Raymond "Raeleus" Buckley_ and licensed with [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
See https://github.com/czyzby/gdx-skins/tree/master/freezing for more details and other great libGDX skins.

### Game Engine
This game was built using the [libGDX](https://libgdx.com/) game engine and uses [Box2D](https://box2d.org/) physics 
engine via this game engine.

### Sound Effects
The sound effects were sourced from a royalty-free sound effects website. I can't remember which one, so if you 
recognize the sounds, please let me know so I can give proper credit. I suspect it was https://freesound.org/.