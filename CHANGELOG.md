# Change Log

Aperture's change log.

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