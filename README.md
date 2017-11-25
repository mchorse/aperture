![Aperture](https://i.imgur.com/Wras78u.png)

[Planet Minecraft page](https://www.planetminecraft.com/mod/aperture-3978432/) – [Minecraft Forum thread](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2837982-aperture-an-advanced-camera-mod) – [CurseForge page](https://minecraft.curseforge.com/projects/aperture) – [Source code](https://github.com/mchorse/aperture) – [Wiki](https://github.com/mchorse/aperture/wiki) 

**Disclaimer**: this mod has nothing to do with Aperture Science from Portal games.

**Aperture** is a Minecraft mod which allows you to create advanced camera movement (for cinematics or machinimas) using camera editor GUI. It works with Forge for Minecraft 1.10.2, 1.11.2 and 1.12.

## Features

**Aperture** mod provides a lot of features for flexible camera editing.

* **Loadable and saveable camera profiles**. You don't need to worry about losing your camera setup. With Aperture's camera profiles, you can save your camera setup on the server (if the Aperture is installed on the server) or on the client (in `config/aperture/cameras` folder).
* **Duration is measured in ticks**. A tick is basically an update in game's logic. Minecraft's logic loop running at 20 ticks per second. Why ticks? They're static, while frames depend on framerate, and can be easily converted into seconds (while not depending on real-time).
* **Flexible camera setup with multiple camera fixtures within a camera profile**. Other camera mods usually gives you an ability to setup only one path at the time. Aperture allows you to have as much camera paths (and not only) as you need to within same camera profile. There are few types of camera fixtures in Aperture: 
    * Idle fixture – static camera
    * Path fixture – animates the camera through a set of given points using one of the three interpolations: linear, cubic or hermite
    * Look fixture – locks the camera at given position, and looks at given entity (using entity selector)
    * Follow fixture – locks the camera relatively to the given entity (kind of like GoPro but fixed)
    * Circular fixture – circulate around the center point and facing at it
* **More camera flexibility with camera modifiers**. Camera modifiers are modular blocks which post-process camera fixture's output. With these modifiers, you can add camera shake, apply math equation, make a GoPro-like behavior, look at some entity while traveling a path, and much more combined. See [wiki](https://github.com/mchorse/aperture/wiki) for more information.
* **Provides a smooth camera** as a Minema-friendly alternative of vanilla cinematic camera.

## Videos

This playlist contains a list of videos about Aperture's updates and tutorials.

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