# Change Log

Aperture's change log.

## Aperture 1.1.1

This is a minor update which fixes some stuff and adds some random stuff regarding the camera. The highlight of this update is the outside mode, which can be used to playback camera while also body acting (detaching yourself from the camera). In some situations it might be glitchy, causing a chunk freeze, but this can be easily fixed by changing render distance.

* Added version updater (added by ycwei982)
* Added outside mode to camera options (it allows to act while being in camera, but it can be glitchy sometimes)
* Added `drag` modifier
* Added reset of FOV back in camera editor (reported by KazerLight)
* Added mouse wheel scrolling in `path` fixture's points widget (suggested by Andruxioid)
* Added `/minecraft:tp` and `/tp` config options for the server camera playback (added by ycwei982)
* Added Russian localization (thanks to Andruxioid)
* Fixed server crash related to `CLIENT`
* Fixed issue with crash related to inability to create a client camera profile folder with illegal filename characters (thanks to ItsRitchieW)
* Fixed player's pitch not going over 90 when running a camera profile
* Fixed sync issues with cloning `follow` modifier and saving `shake` modifier toggle
* Fixed crosshair appearing in the camera editor

## Aperture 1.1

This update fixes a lot of issues and adds few features to camera editor, and introduces camera modifiers. Camera modifiers are special camera behavior modifiers which can be added to camera fixtures. They can process fixture's output in a lot of different ways like shaking camera, looking or following an entity (while still be in the path), apply math formulas and much more!

<a href="https://youtu.be/dKmurdnlj1A"><img src="https://img.youtube.com/vi/dKmurdnlj1A/0.jpg"></a> 

* Added a camera option in camera editor to display camera properties (during camera playback or sync mode)
* Added a trackpad field to change current frame in the camera editor 
* Added `/aperture play` command (which can be used to force play a camera profile on the server side)
* Added camera modifiers
    * Shake modifier – can apply different kinds of shake to the camera
    * Math modifier – allows users to specify a math equation which will modify camera based on that equation
    * Look modifier – just like look fixture, but as modifier
    * Follow modifier – just like follow fixture, but as modifier
    * Translate modifier – translates the fixture by XYZ
    * Angle modifier – modify the angle
    * Orbit modifier – revolves around given entity, can also copy entity's rotation
* Added `default` camera profile to camera editor for easier setup
* Added rewind (going to `0` frame) when camera playback reaches the end
* Added renaming of camera profiles in profile manager
* Added `Ctrl + S` saving shortcut to camera editor
* Changed color of highlighted point to a darker shade
* Fixed CFM mirror camera orientation problem
* Fixed scrubbing event not getting sent when pressing points in path fixture
* Fixed server camera profiles not getting loaded into current camera editor camera profile
* Fixed the way camera playback was working (it was fundamentally wrong)
* Refactored camera fixture and modifier registration (modders can use an API to add their own fixtures or modifiers)
* Switched camera profile rendering to a config option

## Aperture 1.0.1

This is a small patch update that aims to improve capabilities of the camera editor by adding some new features and fixing some bugs.

<a href="https://youtu.be/y7-WsAq6Vlg"><img src="https://img.youtube.com/vi/y7-WsAq6Vlg/0.jpg"></a> 

* Added shortcuts in the camera editor:
    * `D` – deselect current camera fixture
    * `S` – toggle sync option
    * `F` – toggle flight mode option
    * `Space` – play/pause
    * `C` – copy player's position
    * `M` – move duration to cursor
    * `Left Arrow` & `Right Arrow` – to move next or previous frame
    * `Left Arrow` & `Right Arrow` while holding Shift – to move to next or previous fixture
* Added indicators for sync and flight mode
* Added path fixture to set per point duration
* Allow to use `Enter` to create a new camera profile in profiles manager
* Allow to fly in camera editor (flight mode)
* Allow camera to surpass 90 degree limit (in camera runner)
* Convert camera networking code from JSON to ByteBuf
* Improved sync feature – doesn't teleport the user straight away, only after the user will scrub the timeline
* Return user back to his previous game mode
* Send `CameraEditorScrubbedEvent` only when scrub was actually (or it was indirect, like from the jump buttons) scrubbed by the user

## Aperture 1.0

First release of Aperture mod. This release has everything Blockbuster cameras had but more. It includes its own camera editor, and stuff that other camera mods has.

Its all features are frame based, meaning this camera mod would work perfectly with Minema mod and without it.

<a href="https://youtu.be/F2LdjUH-4Qs"><img src="https://img.youtube.com/vi/F2LdjUH-4Qs/0.jpg"></a> 

#### General

General features which were included in the first release. Following features are included:

* Camera editor – a GUI that allows you to edit camera profiles easier using specially designed GUI
* Camera profile fixtures rendering
* Lots of config options
* Different camera fixtures
    * Idle fixture – static camera
    * Path fixture – animating camera using path points
    * Look fixture – stationary camera fixture which looks on an entity specified by entity selector
    * Follow fixture – stationary camera fixture which sticks to an entity specified by entity selector
    * Circular fixture – camera which makes a circle around given point
* Key bindings for positioning and rotating your player 
* Smooth camera (vanilla cinematic camera alternative which is Minema-friendly)

#### Commands

Includes lots of commands for managing camera profile, camera fixtures, configuring your player position, rotation, camera roll and FOV, and lots more!

* Commands for managing camera profile
    * `/camera new` – creates new camera profile
    * `/camera save` – saves camera profile
    * `/camera load` – loads camera profile
    * `/camera clear` – removes all fixtures from camera profile
    * `/camera list` – lists all camera profiles which are loaded
    * `/camera close` – removes camera profile from the list of currently loaded camera profiles
* Commands for managing camera fixtures within camera profile
    * `/camera add` – adds a camera fixture to camera profile
    * `/camera edit` – edits a camera fixture
    * `/camera remove` – removes a camera fixture
    * `/camera move` – moves a camera fixture to another index
    * `/camera goto` – set player's position and rotation based on camera fixture
* Commands for managing player's attributes related to camera
    * `/camera step` – sets player's position relatively or absolutely
    * `/camera rotate` – sets player's rotation relatively or absolutely
    * `/camera roll` – sets or informs current camera roll (camera tilting)
    * `/camera fov` – sets or informs current camera FOV
    * `/camera default` – resets camera roll and FOV to default values
* Commands for camera playback
    * `/camera start` – starts camera playback
    * `/camera stop` – stops camera playback
* Commands for managing path fixtures
    * `/camera path add` – add a path point
    * `/camera path edit` – edit a path point
    * `/camera path remove` – remove a path point
    * `/camera path move` – move a path point
    * `/camera path goto` – set player's position and rotation based on a point in a path fixture

These are only for legacy purposes. If you don't want to use these, totally cool, camera editor way convenient and time saving than using commands.