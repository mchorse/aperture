## Aperture 1.8.1

This update was made by Chryfi.

**Compatible** with McLib `2.4.1` and Minema `3.7` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Fix tracking file paths not working properly with spaces in the path
* Fix manual fixture y coordinate not being recalculated for the player eye height

## Aperture 1.8

This huge update was made by MiaoNLI, exceptions are mentioned at the respective items. It features groundbreaking things like the ability to animate any shader variable.

**Compatible** with McLib `2.4` and Minema `3.6.2` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Camera
    * Use eye position instand of feet position
    * Preventing players from entering the void
* Camera Editor
    * Added more keyframes about rendering and Optifine shaderpack
    * Added Dolly Zoom Modifier
    * Added a Distance channel to the keyframe fixture similar to the circular fixture
    * Fixed bug that Math Modifier may skip frames
    * Right-click a fixture no longer jumps to the beginning of the fixture
    * Disabled vanilla fov modifier
    * Change to fps mode when opening
* File Encoding
    * UTF-8 file encoding is used by default
* Aperture Tracking System (by Chryfi)
    * Multiple equal entity names for entity tracking with the entity selector are now possible. It will remap the names to indexed names likes “name, name.1, name.2”
    * Changed the functionality of combine tracker morphs. It is now the option “automatic append”. If the tracker morph’s name already was tracked, it will check if this tracker starts at the next frame where the original tracker has stopped. If it continues at the next frame, it then appends this tracking data to the original one.
    * Motion Blur frames are now internally ignored. The tracking data only includes the actual video frames now.
    * Fixed heldframes - the tracker now tracks the last heldframe which fixes tracking inconsistencies when using for example drag modifier
    * Fixed morph trackers not working when use target is not enabled
    * Made tracking file encode in UTF-8
* Added Minema as optional dependency with a required minimum version. The required minimum version is 3.6.2 (by Chryfi)

## Aperture 1.7

This update was made by Chryfi, and it features a groundbreaking feature which would allow combining CGI and Minecraft footage much much easier using Minema mod and Blender, by exporting tracking data in an importable format.

**Compatible** with McLib `2.3.6` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/v_OpT704Sp8"><img src="https://img.youtube.com/vi/v_OpT704Sp8/0.jpg"></a> 

* Added tracking exporter, it exports:
    * Metadata like FPS, video resolution, and more...
    * Camera tracking data (position, rotation, FOV)
    * Entity tracking data (position, body yaw)
    * Morph tracking data (position, rotation, scale)
    * Compatible with motion blur and held frames Minema options
    * Needs [Blender import script](https://github.com/Chryfi/io_import_aperture_tracking) (it has multiple import options for further control)

## Aperture 1.6.1

This patch update fixes a couple of bugs.

**Compatible** with McLib `2.3` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Fixed relative toggle conversion outputting ticks when Use seconds enabled
* Fixed CMDCam hogging camera's roll

## Aperture 1.6

This update is focused on adding Undo/Redo and limitations for non-OP users.

**Compatible** with McLib `2.3` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/F6b_ScRB3gc"><img src="https://img.youtube.com/vi/F6b_ScRB3gc/0.jpg"></a> 

* Added limiting of editing Aperture data (and opening camera editor) on the server to OP only
* Added circular interpolation to path fixture
* Added an option to display everything in seconds (suggested by zoombie)
* Added playback of the scene possible with an empty camera profile
* Added proper handling of fixtures during cutting (suggested by Andruxioid)
* Added alphabetical sorting to camera profile list
* Added **Undo/Redo** (`Ctrl + Z`/`Ctrl + Y`) to camera editor on per camera profile basis
* Added loop region selection (suggested by Chunk7) controlled with:
	* `[` (start) and `]` (end) keys
	* Ctrl + left click to control left end point, Ctrl + right click to control right end point, and Ctrl + middle click to reset loop selection
* Added a keybind to toggle keyframe editor visibility
* Added periodic auto-save option
* Changed `/aperture play` command's syntax to `/aperture play <camera_profile> [target]`
* Changed `v` velocity variable to take in effect modifiers effects as well
* Fixed `/camera step` and `/camera rotate` not accepting relative `~` values
* Fixed timeline scaling issues
* Fixed drag modifier jumping from 0,0,0 when toggling XYZ toggles
* Fixed camera profiles are not checked when duplicating with existing camera profile name (reported by Andruxioid and Chunk7)

## Aperture 1.5.2

This patch update fixes a couple of bugs.

**Compatible** with McLib `2.2.1` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Fixed tick markers looking chaotic upon first load (reported by Ник)
* Fixed duration of playback defined upon start of playback, which disallows resizing last fixture during playback (reported by Ник and Andruxioid)

## Aperture 1.5.1

This decent patch update adds a couple of neat quality of life camera tweaks.

**Compatible** with McLib `2.2` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Added an ability to convert `manual` fixture to `keyframe` fixture
* Added `v` (camera's velocity) variable to `math` modifier
* Added XYZ offset options to look modifier
* Added generation of second keyframe (`1.0` value) to `remapper` modifier and keyframe based envelopes
* Added **Hide the chat** mod option (suggested by Jvonlins)
* Added tick markers to playback timeline
* Changed behavior upon switching between camera profiles, the playback scrub won't get rewind to tick `0`
* Changed timeline to use same code from keyframe elements
* Changed letterbox to be drawn outside of camera editor (suggested by The Darvin Studio)
* Fixed playback scrub event not getting fired upon selecting the fixture during sync mode
* Fixed `follow` modifier's XYZ offset options not being synced
* Fixed stuttering of `manual` fixture's recording code (reported by fad)
* Fixed double modifiers when converting `path` fixture to `keyframe` fixture

## Aperture 1.5

This update adds a couple of QoL tweaks and two new awesome features involving keyframes and modifiers.

**Compatible** with McLib `2.1` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/nitZlB_kG2w"><img src="https://img.youtube.com/vi/nitZlB_kG2w/0.jpg"></a> 

* Added `remapper` modifier
* Added reverse button to `dolly` fixture
* Added orbital flight mode (switch to orbital mode by pressing `V`, and alt + mouse scroll to change the distance)
* Added a keyframe option to modifier envelopes
* Added a context menu (right click) to look modifier which allow to set the look at block coordinates by looking at block or entity
* Changed dolly distance from `1.0` to `0.1`
* Changed toggle `V` keybind to switch between flight modes
* Fixed changing color of a fixture doesn't marking the camera profile
* Fixed crash with converting one point path to keyframe
* Fixed fixtures having `0` duration upon loading
* Fixed `[]` couldn't be used in the filename (reported by Ethobot)
* Fixed flight mode is interrupting when editing keyframes (reported by Chunk7)
* Fixed custom fixture colors not being used in camera profile renderer
* Fixed manual fixture not playing the scene when starting recording (reported by Herr Bergmann)

## Aperture 1.4.2

This is a neat update which fixes a couple of bugs, adds Dolly fixture and brightness animation (which can be used for manual focus via shader packs), giving fixtures colors, drastically improved keyframe editing (see McLib's change log) and removed a couple of outdated features.

**Compatible** with McLib `2.0.3` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/6eil_zvv1KI"><img src="https://img.youtube.com/vi/6eil_zvv1KI/0.jpg"></a> 

* Added keyframes tab to profile manager where you can keyframe brightness (gamma setting), it can be used with shaders to animate some properties
* Added an ability to specify color of the fixture, for organization purposes
* Added Dolly fixture, it's a convenience fixture which allows setting up a simple forward/backward path for given distance and angle
* Added manual fixture's path rendering
* Added McLib's new interpolations to work with path/dolly fixtures (back, elastic and bounce)
* Added converter feature to replace fixture feature, which converts one fixture to another, without losing all of its data. Currently available proper conversion are: `Dolly` to `Path`, `Dolly` to `Keyframe`, `Path` to `Dolly`, and `Path` to `Keyframe`
* Fixed fixture looping feature never stopped recording with Minema panel
* Fixed margin of keyframe channel and display position labels
* Fixed minor typo "Switch to specator" (reported by Lycoon)
* Fixed flight mode being "sticky" sometimes
* Fixed not reset partial ticks shifting keyframe editing at 0th tick
* Moved keyframe code to McLib
* Removed per point duration option from path fixture (per-point duration paths will be converted to keyframe fixtures)
* Removed all channel from dope sheet editor, due to ability to multi-select in McLib **2.0.3**
* Removed **Generate keyframe** button (in favor of replace converter feature)

## Aperture 1.4.1

This is a small quick patch which fixes a couple of minor issues with camera editor GUI.

**Compatible** with McLib `2.0` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

* Fixed crash with toggle interpolation keybind
* Fixed fixture panel's keybinds working through flight mode
* Optimized keyframe graph
* Updated Chinese strings for 1.4 (thanks to Chunk7, KuenYo_ and H2SO4GepaoAX)

## Aperture 1.4

This big update features lots of new quality of life features which should help increase productivity of camera creation, and two features that extend camera capabilities. 

**Compatible** with McLib `2.0` and Minema `3.5` (optionally). It doesn't mean that future versions of McLib (or Minema) would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/2ToSwrFiVOo"><img src="https://img.youtube.com/vi/2ToSwrFiVOo/0.jpg"></a> 

* Added camera modifier envelopes which allow to smoothly enable camera modifier based on two points in time and their fades
* Added `manual` fixture (suggested by Dyl_Art and Lucatim)
* Added Minema recording panel
* Added crosshair option to camera editor (suggested by terbin)
* Added many tooltips explaining what different option do
* Changed timeline cursor to stay within the view when camera plays or jump between fixtures
* Changed start camera profile playback's behavior into toggle playback
* Changed position of add, dupe, change and remove fixture icon buttons when there is too few space (suggested by Andruxioid)
* Changed camera editor profile management to never allow `null` camera profile
* Changed to fully support `double` precision for camera position (XYZ)
* Improved flight mode:
    * Added mouse control to flight mode (left click drag changes yaw and pitch, horizontal right click drag changes roll, and vertical middle click drag changes FOV)
    * Added keybinds config category to flight mode (suggested by Lucatim)
    * Added keybinds in flight mode to change the overall speed of flight mode (suggested by Mr Wolf)
* Improved `follow` modifier:
    * Added relative option to `follow` modifier
    * Added XYZ offset options to `follow` modifier (suggested by Ethobot)
* Fixed dope sheet and graph editor points not rendering when GUI scale: Auto (reported by Lucatim)
* Fixed restoration of roll and FOV to be much stable when entering the camera editor and exiting when camera plays
* Fixed camera slightly falling when opening camera editor with spectator mode
* Removed all configuration options from Mod Options (they were moved into McLib's configuration system)
* Removed render mouse pointer option (moved to McLib)
* Removed stop camera profile playback keybind

## Aperture 1.3.6

This tiny patch offers a couple of bugs and crashes fixes as well as some of my stupidity fixes.

**Compatible** with McLib `1.0.4`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

* Fixed exception being thrown when no camera file was found (internal clean up)
* Fixed flight and sync icons showing in F1 in camera editor (i.e. hide them)
* Fixed unable to turn head up and down when in outside mode
* Fixed crash with empty graph view (reported by SCraft Systems)
* Fixed crash with unpopulated camera profile entry (reported by SCraft Systems)
* Fixed camera profile not being loading due to outdated camera fixtures present

## Aperture 1.3.5

This tiny patch offers a couple of important bugs, crashes and optimizes performance of path preview when used with velocity control.

**Compatible** with McLib `1.0.4`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

* Fixed camera profiles not being shown in the list (sorry, I accidentally broke it)
* Fixed hermite crashing Minecraft due to being unsorted (reported and fixed by terbin)
* Fixed crash which happens if fixture/camera profile related keybinds are pressed when no camera profile is selected (reported by Chunk7)
* Fixed optimize velocity control's performance in path preview (reported by Negative_Entropy and terbin)
* Fixed playback scrub being stuck at 0/0 when switching/adding camera profiles with no fixtures (reported by Koki)

## Aperture 1.3.4

This small patch improves UX experience and also adds Brazilian Portuguese localization, camera profile modifiers, copy-paste modifiers, and improves and simplifies look and follow modifiers.

**Compatible** with McLib `1.0.4`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/goooUv_OWIs"><img src="https://img.youtube.com/vi/goooUv_OWIs/0.jpg"></a> 

* Added an ability for `look` and `follow` modifiers to use multiple entities and just calculate the mid point
* Added an ability for target modifiers to use just a name tag instead of target selector
* Added a config option to disable rendering of F1 tool tip in the camera editor (suggested by Andruxioid)
* Added copy-paste modifiers
* Added global modifiers panel (suggested by KazerLight)
* Added `pt_BR` translations (thanks to D-H-U on GitHub)
* Fixed a bug where camera profiles with `.` in them disappear (reported by Agirres)
* Fixed crash with continuous `null` fixtures
* Fixed little flick when pausing camera editor (basically save `partialTick`)
* Fixed preview not being smooth with sync mode
* Fixed pressing `N` in camera editor not hiding other panels
* Fixed scaling of points in different MC scales (reported by Auraman)
* Fixed fixture's label hide fully instead of partially
* Fixed camera profile list not updating when saving, etc.
* Fixed paths not rendering with shaders
* Fixed camera profile list not updating when converting or saving
* Removed go to frame button and track pad field

## Aperture 1.3.3

This small patch adds more path fixture intepolations, workflow enhancement features (cut, replace and interactively create fixtures) and fixes few annoying bugs.

**Compatible** with McLib `1.0.4`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/5Tnp4z6GWME"><img src="https://img.youtube.com/vi/5Tnp4z6GWME/0.jpg"></a> 

* Added a feature to "cut" fixtures (split a fixture at cursor in two)
* Added more interpolations to `path` fixture
* Added an option to disable auto saving of camera profiles upon exiting (auto save is enabled by default)
* Added a keybind to add a path point to currently selected path (suggested by Kanguste)
* Added a key modifier or something to allow in flight mode to also move vertically where character is looking (suggested by Kanguste)
* Added an interactive fixture creation mode to allow setup markers and then create multiple fixtures at once user finishes
* Added a feature to change/replace selected fixture fixture
* Added a key modifier (alt) to duplicate a keyframe in graph and dope sheet views
* Added display of mouse buttons to GUI cursor rendering mod option
* Changed the default key bind to smooth camera to nothing, so people wouldn't freak out and end up deleting the mod, because it's "broken"
* Changed the mouse wheel flight speed adjustment to support even smaller values (suggested by Kanguste)
* Changed the highlight to be more apparent (suggested by EthoArt)
* Fixed modifiers not getting copied and wrong X values when using "generate keyframe" feature
* Fixed flight mode by disallowing enabling it during playback
* Fixed performance of Show Path feature (reported by Joziah2)
* Fixed issue with clicking on text fields of value/tick it will make the buttons disappear
* Fixed block position gets reset in look modifier (reported by Chunk7)
* Fixed ~~FOV~~ roll with outside and flight modes (reported by Kanguste)
* Fixed and invert flight mode mouse wheel adjustment
* Fixed duplicating dialog to not allow using the same name
* Fixed flight mode or other interactive measures not modifying current fixture before first scrubbing
* Fixed duplicating a camera profile using the same name causing duplication issues

## Aperture 1.3.2

This small patch adds a couple of neat features and fixes few annoying bugs.

**Compatible** with McLib `1.0.3`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/pnXXgouveRk"><img src="https://img.youtube.com/vi/pnXXgouveRk/0.jpg"></a> 

* Added `forward` option to `look` modifier which makes the camera face forward based on the direction of movement (suggested by terbin)
* Added dope sheet GUI for the keyframe fixture (i.e. the All keyframe fixture channel)
* Added an overlay camera option in camera editor (suggested by Lolik)
* Added fixture preview loop (suggested by Agirres)
* Added duration marker for playback scrub
* Changed flight mode to disable when player starts scrubbing (or switching between stuff)
* Changed camera options' height to be relative to the fixture editor area
* Changed interpolation list vertically responsive (suggested by Agirres)
* Changed internal representation of position from `float` to `double` (suggested by terbin for far away recording)
* Changed `drag` modifier to handle normalization of yaw
* Changed channel buttons (All, X, Y, Z, etc.) in the keyframe fixture editor
* Fixed duplicate camera profile not showing duplicate profile in the list
* Fixed overlays not rendering relative to playback scrub
* Fixed outside mode copying actual player location (reported by Braigar)
* Fixed edit position not working properly with modifiers

## Aperture 1.3.1

Unfortunately, before I released Aperture 1.3, I made a couple of bug fixes. I haven't test it properly and haven't let anyone test it either. However, Agirres spotted my stupidity, and aw shit, here we go again.

* Fixed double clicking not working due to another bug fix
* Fixed path fixture go void on first tick when velocity control is enabled
* Fixed velocity control issue where it lags toward the end (reported by Agirres)

## Aperture 1.3

This is a massive update which adds a lot of advanced camera features which would make new camera moves possible. Beside that, many bugs were fixed, and several features of camera editor were greatly enhanced!

**Compatible** with McLib `1.0.1`. It doesn't mean that future versions of McLib would be incompatible, but older versions are most likely incompatible.

<a href="https://youtu.be/_vfZYtiRfRs"><img src="https://img.youtube.com/vi/_vfZYtiRfRs/0.jpg"></a> 

* Added zooming and scrolling to time line bar
* Added rule of thirds camera option (suggested by Sanchan, Agirres and 5DF)
* Added letterbox camera option with configurable aspect ratio (suggested by SeanFilmProduction, Agirres, 5DF, Andruxioid and few others)
* Added `hermite` interpolation to keyframe fixture
* Added duplicate camera profile button in profile manager
* Added `[`, `]` and `'`, `\` keybinds to control FOV and camera roll in flight mode (respectively)
* Added search bar to profile manager (suggested by Agirres)
* Added `null` fixture which acts like a placeholder for next/previous fixture (suggested by Agirres)
* Added keyframe-able velocity control to path fixture (suggested by terbin)
* Changed duration dragging to react not immediately, but rather after mouse move (reported by Andruxioid)
* Changed buttons places in profile manager (suggested by Agirres)
* Changed the player's game mode to spectator mode when spectator is enabling and entering the camera editor
* Changed camera runner and camera editor to restore last roll
* Changed tick field in keyframe fixture integer only
* Changed position of per point duration checkbox
* Improved keyframe/graph view:
    * Added keybinds to vertically or horizontally move keyframes while holding Ctrl or Shift keys (suggested by GroupM and Agirres)
    * Added dropdown to pick interpolation in keyframe fixture editor (suggested by Agirres)
    * Added proper zooming and value marks into graph viewer in keyframe fixture editor
    * Added double clicking in graph view to create/remove keyframes
    * Changed rendering of keyframe points, graph and bezier control points
* Fixed translation interpolation and easing localization keys
* Fixed `bezier` interpolation to have proper mathematical implementation
* Fixed crash related to dragging keyframe tick/value when no keyframe selected
* Fixed crash related to shifting duration
* Fixed crash in camera runner (reported by STH)
* Fixed look modifier GUI elements (reported by Lycoon)
* Fixed issue with incorrect fixture placement when adding new
* Fixed `360˚`+ seizure-like yaw (reported by BenyangOVO, ycwei982, Lycoon and terbin)
* Fixed circular fixture not setting FOV and roll (reported by Agirres)
* Fixed little area which causes scrub rewind near the center of the playback scrub
* Removed `look` and `follow` fixtures (use modifiers counterparts)
* Removed lots of old camera editing sub-commands

## Aperture 1.2

This is a major update which features a new camera fixture called **keyframe fixture** and complete rewrite of GUI code (which allows Blockbuster to more control over integration).

<a href="https://youtu.be/fT7QeCqKMyU"><img src="https://img.youtube.com/vi/fT7QeCqKMyU/0.jpg"></a> 

* Added `mclib` library mod as a dependency
* Added keyframe camera fixture
* Added an ability to remove camera profiles
* Added **Generate keyframe** button in path fixture panel (suggested by GroupM)
* Added offset tick marker to playback scrub in camera editor (suggested by GroupM)
* Added a mechanic to resize durations between two fixtures without indvidually resizing them (suggested by GroupM)
* Changed where added fixture will be placed – if no fixture selected, a new fixture will be placed at the end of camera profile, otherwise, after currently selected fixture
* Full rewrite of camera editor GUI to support McLib GUI framework
* Full rewrite of Aperture's events

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