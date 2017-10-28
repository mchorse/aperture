![Aperture](https://i.imgur.com/Wras78u.png)

[Planet Minecraft 页面](https://www.planetminecraft.com/mod/aperture-3978432/) – [Minecraft Forum 帖子](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2837982-aperture-an-advanced-camera-mod) – [CurseForge 页面](https://minecraft.curseforge.com/projects/aperture) – [源代码](https://github.com/mchorse/aperture)

**Aperture** 是一个让你可以使用GUI（或者命令）来创作相机场景的Minecraft mod。这个Mod支持Forge版本的Minecraft 1.10.2, 1.11.2 和 1.12。不支持网易我的世界中国版。

Aperture的功能完全按帧进行，便可以让你使用 [Minema](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2790594-minema-unofficial-the-smooth-movie-recorder) mod导出流畅的视频。

一些特性让Aperture和其它摄像机Mod与众不同的是：

* 相机配置文件可以从本地或者服务器上（如果服务器安装了这个Mod）被保存和加载。
* 提供不同样式的相机（也称为相机关键点）：
    * 静止关键点 - 什么都不做，就定在那里。
    * 路径关键点 - 让相机用3种动作动起来：线性，三次曲线，艾米插值。
    * 跟着看 - 锁定相机的位置，但随着设定的实体（使用实体选择器）视角跟着看。
    * 跟着走 - 锁定相机的方位，跟着实体走（有点像GoPro但很稳）。
    * 环形 - 确定一个圆心，相机绕着圆看着圆心。
* 提供一个GUI，用来管理相机配置文件和编辑许多相机关键点。如果有需求，还有命令给你用。
* 不同于帧，和毫秒，回放长度由一个单位 **tick** 掌握，让编辑更加一致。Tick是个在帧数和毫秒之间的单位，不依赖于设置里的帧率，也意味着更容易转换成秒。大约20个tick就是一秒。（根据游戏流畅度）
* 提供一个平滑的摄像机，相似于纯净版的平滑视角，但可以让Minema用得到。

## 视频

这是一个关于Aperture的播放列表。最近发布，现在有两个视频：mod的预告片和mod的教程。（由Noble团队制作的[中文教程](http://www.bilibili.com/video/av15303499/)不代表McHorse观点）

<a href="https://youtu.be/y7-WsAq6Vlg?list=PL6UPd2Tj65nFLGMBqKaeKOPNp2HOO86Uw"><img src="https://img.youtube.com/vi/y7-WsAq6Vlg/0.jpg"></a>

## 安装

安装 [Minecraft Forge](http://files.minecraftforge.net/)，下载本mod支持的Minecraft版本的最新稳定版。拷贝到Minecraft的 `mods` 文件夹，启动游戏。

之后，Aperture mod 应该安装了，显示在 Minecraft 的 mods 菜单。如果没有显示在菜单里，那么你可能错误地进行操作了。

## mod 评测和转发者看这里

当你转发和评测我的mod时，请考虑以下内容（如果你想支持我和mod）：

* 不要改mod名字。这是 *Aperture* mod。
* 确保你所描述的东西是真实存在的。失实写的东西，比如支持的Minecraft版本或者不存在的功能，由你背锅。
* 上传一个由你自己编译的版本，你编译的版本是你的锅。
* 请提供来源链接。提供 [CurseForge](https://minecraft.curseforge.com/projects/aperture) 最棒。
* 请提供一个链接到我的 [YouTube 频道](https://www.youtube.com/channel/UCWVDjAcecHHa8UrEWMRGI8w)。非常感谢！
* 当你转发时你可以使用 Aperture 的 [banner](https://i.imgur.com/Wras78u.png) 或 [封面](https://i.imgur.com/rckGnn4.png)  。不要加个水印，那很残忍。

如果你对此项目感兴趣，你可以关注我的社交媒体账号：

[![YouTube](http://i.imgur.com/yA4qam9.png)](https://www.youtube.com/channel/UCWVDjAcecHHa8UrEWMRGI8w) [![Discord](http://i.imgur.com/gI6JEpJ.png)](https://discord.gg/qfxrqUF) [![Twitter](http://i.imgur.com/6b8vHcX.png)](https://twitter.com/McHorsy) [![GitHub](http://i.imgur.com/DmTn1f1.png)](https://github.com/mchorse)  

## Bug 反馈

当你发现了一个Bug，或者mod导致游戏崩溃，我希望你可以在[issue tracker](https://github.com/mchorse/aperture/issues/)报告bug或崩溃, 或私信在 [Twitter](https://twitter.com/McHorsy)。也请麻烦在[pastebin](http://pastebin.com)贴一份日志文件，并描述下发生的事情，及重现bug或崩溃的方法。谢谢！
