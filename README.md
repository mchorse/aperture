![Aperture](https://i.imgur.com/Wras78u.png)

[Planet Minecraft page](https://www.planetminecraft.com/mod/aperture-3978432/) – [Minecraft Forum thread](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2837982-aperture-an-advanced-camera-mod) – [CurseForge page](https://minecraft.curseforge.com/projects/aperture) – [Source code](https://github.com/mchorse/aperture) 

**Aperture** is a Minecraft mod which allows you to create cinematics using GUI (or commands). It works with Forge for Minecraft 1.10.2, 1.11.2 and 1.12. 

Aperture's features are fully frame based, which allows you to record smooth footage using [Minema](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2790594-minema-unofficial-the-smooth-movie-recorder) mod. 

Here are some features which makes Aperture different from other camera mods:

* Camera profiles can be saved and loaded from config folder or the server (if the mod is installed).
* Provides different camera behavior blocks (which are known as camera fixtures):
    * Idle fixture – does nothing, just locks the camera
    * Path fixture – animates the camera through a set of points using one of the three interpolations: linear, cubic or hermite
    * Look fixture – locks the camera at given position, and follows given entity (using entity selector) by looking at it
    * Follow fixture – locks the camera relatively to the given entity (kind of like GoPro but fixed)
    * Circular fixture – circulate around the center point and facing at it
* Provides a GUI which eases camera profile and fixtures editing a lot. There is also command interface, if needed.
* Playback duration is measured in **ticks**, instead of frames or milliseconds, which makes editing more consistent. Tick is a unit which is in the middle between frames and milliseconds. They don't depend on the frame rate in the settings, meanwhile also can be easily converted to seconds. There are about 20 ticks per a second (depends on the lag).
* Provides a smooth camera which is basically an analogue of vanilla cinematic camera, but Minema friendly.

## Videos

There is a playlist of videos which are about Aperture. At the moment of public release, there are two videos: mod's teaser and mod's tutorial.

<a href="https://youtu.be/y7-WsAq6Vlg?list=PL6UPd2Tj65nFLGMBqKaeKOPNp2HOO86Uw"><img src="https://img.youtube.com/vi/y7-WsAq6Vlg/0.jpg"></a> 

## Install

Install [Minecraft Forge](http://files.minecraftforge.net/), download the latest stable version of jar file for available minecraft version. Put it in minecraft's `mods` folder, and launch the game.

After that, Aperture mod should be installed and will appear in Minecraft's mods menu. If Aperture didn't appear in the mods menu, then something went wrong. 

## For mod reviewers and reposters

When reposting my mod on your own website or reviewing it, please consider following (if you want to support me and my mod):

* Don't distort the mod name. It's the *Aperture* mod.
* Make sure that information and description of my mod is legit. Misleading information, like Minecraft version support or non-existent features, is your responsibility.
* By uploading a custom build of this mod, the build becomes your responsibility.
* Provide the source link, please. [CurseForge](https://minecraft.curseforge.com/projects/aperture) page is preferable.
* Provide a link to my [YouTube channel](https://www.youtube.com/channel/UCWVDjAcecHHa8UrEWMRGI8w), please. This will be really appreciated! 
* You can use Aperture [banner](https://i.imgur.com/Wras78u.png) or [cover](https://i.imgur.com/rckGnn4.png) for your repost page. Don't apply the watermark, though, that's just rude.

If you're interested in this project, you might as well follow me on any of social media accounts listed below:

[![YouTube](http://i.imgur.com/yA4qam9.png)](https://www.youtube.com/channel/UCWVDjAcecHHa8UrEWMRGI8w) [![Discord](http://i.imgur.com/gI6JEpJ.png)](https://discord.gg/qfxrqUF) [![Twitter](http://i.imgur.com/6b8vHcX.png)](https://twitter.com/McHorsy) [![GitHub](http://i.imgur.com/DmTn1f1.png)](https://github.com/mchorse)  

## Bug reports

If you found a bug, or this mod crashed your game. I'll appreciate if you could report a bug or a crash to me either on [issue tracker](https://github.com/mchorse/aperture/issues/), on PM or on [Twitter](https://twitter.com/McHorsy). Please, make sure to attach a crash log ([pastebin](http://pastebin.com) please) and description of a bug or crash and the way to reproduce it. Thanks!