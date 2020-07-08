![Aperture](https://i.imgur.com/Wras78u.png)

[Planet Minecraft 页面](https://www.planetminecraft.com/mod/aperture-3978432/) – [CurseForge 页面](https://www.curseforge.com/minecraft/mc-mods/aperturee) – [GitHub](https://github.com/mchorse/aperture) – [百科](https://github.com/mchorse/aperture/wiki) – [中文百科](https://github.com/ycwei982/aperture/wiki) – [中文用户交流群](https://jq.qq.com/?_wv=1027&k=584nNVF)

**免责声明**: 该 Mod 和游戏 Portal 里的光圈科技没有任何关联。请阅览这篇 [光圈](https://zh.wikipedia.org/wiki/%E5%85%89%E5%9C%88) 来了解什么是光圈。

**Aperture** 是一个让您可以使用 GUI 来创作（适用于摄影场景或者短片的）高级相机动作的 Minecraft mod。本 mod 适用 Minecraft 1.12.2 的 Forge 版本（旧版本适用于 1.10.2 和 1.11.2）。不支持网易我的世界中国版。

## 特性

**Aperture** mod 提供许多灵活的功能来编辑相机动作。

* **可保存和加载的相机配置**。您不必担心丢失您的相机设置。有了 Aperture 的相机配置，您可以在服务器上（如果服务器安装了 Aperture）或客户端上（在 `config/aperture/cameras` 文件夹）储存您的相机配置。
* **时间单位为 Tick**。一个 Tick 大致上是一次游戏逻辑的更新。Minecraft 的逻辑更新频率为每秒 20 Ticks。为什么要用 Tick？因为他们相对帧率而言是稳定的，并且可以简单地转换成秒（不依赖真实时间）。
* **在一个相机配置里使用多个相机关键点的灵活配置**。其他相机 mod 通常情况下只能一次设置一个路径。Aperture 允许您在一个相机配置里设爆。Aperture 里有以下几种相机关键点：
    * 静止关键点 – 让相机保持在给定的位置和角度上。
    * 环形关键点 – 绕圆心旋转，并看向圆心。
    * 路径关键点 – 通过一组路径点并选择多种插值（线性、三次曲线、艾米插值等）中的一种让相机动起来。同时还支持关键帧的速度控制。
    * 关键帧关键点 – 可以让您使用关键帧创建一个灵活的相机路径（可以有不同的插值，包括贝塞尔和缓入缓出）。
    * 空关键点 – 占位关键点，用于延续下一个关键点的第一个位置或上一个关键点的最后一个位置。
    * 手动关键点 – 可以让您记录完全自定义的相机运动。
* **调整器让相机更加灵活**。相机调整器是用于处理相机关键点输出的模块。有了这些调整器，您可以添加相机的摇晃，应用数学函数，实现类似 GoPro 的相机运动，在路径运动时跟踪一个实体等。详情请查看[百科](https://github.com/mchorse/aperture/wiki)。
* **提供平滑摄像机** 作为对 Minema 更友好的用于替换原版电影平滑相机的功能。
* **兼容 [Minema](https://github.com/daipenger/minema/releases)**，如果您很想在自己的渣配电脑上，录制出如丝般流畅的视频，安装 [Minema](https://github.com/daipenger/minema/releases) mod 即可录制出流畅的视频！

## 视频

该播放列表是一个教程系列。它能够教你如何从头开始使用 Aperture mod。它是基于 Aperture 1.3.4 进行演示的。在你观看了这些视频并且学会了如何使用它后，您就可以随时观看更新日志视频来获取更多关于新特性的信息了。

<a href="https://youtu.be/_KLU8VnMiCQ?list=PLLnllO8nnzE8MGDb6QzE2kt4-KVC1dRRl"><img src="https://img.youtube.com/vi/_KLU8VnMiCQ/0.jpg"></a> 

此外，该播放列表包含了一系列关于 **Aperture 更新内容**的视频。也就是所谓的更新日志视频，展示了 Aperture mod 的新变化。

<a href="https://youtu.be/2ToSwrFiVOo?list=PL6UPd2Tj65nFLGMBqKaeKOPNp2HOO86Uw"><img src="https://img.youtube.com/vi/2ToSwrFiVOo/0.jpg"></a> 

您也可前往 [McHorse's Mods 哔哩哔哩中文频道](https://space.bilibili.com/472615413) 观看。

## 安装

安装 [Minecraft Forge](http://files.minecraftforge.net/)，下载对应 Minecraft 版本的最新稳定版 jar 文件。同时，需要安装以下 mod：[McLib](https://www.curseforge.com/minecraft/mc-mods/mchorses-mclib)。将其放在 Minecraft 的`mods`文件夹中，并启动游戏。

启动后，Blockbuster mod 应该会安装成功，并会出现在 Minecraft 的 Mods 菜单中。如果Blockbuster 没有出现在 Mods 菜单中，那就说明出问题了。

如果您对此项目感兴趣，您可以关注我的社交媒体账号：

[![YouTube](http://i.imgur.com/yA4qam9.png)](https://www.youtube.com/channel/UCSLuDXxxql4EVK_Ktd6PNbw) [![Discord](http://i.imgur.com/gI6JEpJ.png)](https://discord.gg/qfxrqUF) [![Twitter](http://i.imgur.com/6b8vHcX.png)](https://twitter.com/McHorsy) [![GitHub](http://i.imgur.com/DmTn1f1.png)](https://github.com/mchorse)  

另外，如果您在 Patreon 上支持我的话，我会很感激的！

[![成为赞助者](https://i.imgur.com/4pQZ2xW.png)](https://www.patreon.com/McHorse)

## Bug 反馈

如果您发现了一个 Bug，或因本 mod 导致游戏崩溃，我希望您可以将 Bug 或崩溃提交至 [issue tracker](https://github.com/mchorse/aperture/issues/)，或在 [Twitter](https://twitter.com/McHorsy) 私信。也请麻烦在 [pastebin](http://pastebin.com) 贴一份日志文件，并描述下 Bug 与崩溃的内容，及重现 Bug 或崩溃的方法。感谢！

如果您没有英文交流能力，您也可以选择在 [中文用户QQ群 (328380393)](https://jq.qq.com/?_wv=1027&k=584nNVF) 反馈您的 Bug 或崩溃，Mod 的翻译者将会协助您。