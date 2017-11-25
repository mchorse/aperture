![Aperture](https://i.imgur.com/Wras78u.png)

[Planet Minecraft 页面](https://www.planetminecraft.com/mod/aperture-3978432/) – [Minecraft Forum 帖子](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2837982-aperture-an-advanced-camera-mod) – [CurseForge 页面](https://minecraft.curseforge.com/projects/aperture) – [源代码](https://github.com/mchorse/aperture) – [百科](https://github.com/mchorse/aperture/wiki) – [中文百科](https://github.com/ycwei982/aperture/wiki) – [中国用户QQ群](https://jq.qq.com/?_wv=1027&k=584nNVF)

**免责声明**: 这个Mod和Portal游戏里的光圈科技没有任何关系。

**Aperture** 是一个让你可以使用GUI来创作（适用于摄影场景或者短片的）高级相机动作的Minecraft mod。这个Mod支持Forge版本的Minecraft 1.10.2, 1.11.2 和 1.12。不支持网易我的世界中国版。

## 特性

**Aperture** mod提供许多灵活的功能来编辑相机动作。

* **可保存和加载的相机配置**。你不用担心你的相机设置消失。有了Aperture的相机配置，你可以在服务器上储存你的相机配置（如果服务器安装了Aperture）或客户端上（在 `config/aperture/cameras` 文件夹）。
* **时间长度是Tick**。一个Tick基本是一次游戏数据的更新。Minecraft的更新频率是每秒20 ticks。为什么要用Tick？因为他们相对每一帧而言是稳定的，并且可以简单地转换成秒（不依赖真实时间）。
* **在一个相机配置里使用多个相机关键点的灵活配置**。别的相机mod通常情况下只能一次设置一个路径。Aperture允许你在一个相机配置里设爆。在Aperture里有这几种相机关键点：
    * 静止关键点 - 什么都不做，就定在那里。
    * 路径关键点 - 让相机用3种动作动起来：线性，三次曲线，艾米插值。
    * 跟着看 - 锁定相机的位置，但随着设定的实体（使用实体选择器）视角跟着看。
    * 跟着走 - 锁定相机的方位，跟着实体走（有点像GoPro但很稳）。
    * 环形 - 确定一个圆心，相机绕着圆看着圆心。
* **调整器让相机更加灵活**。相机调整器是用于处理相机关键点输出的模块。有了这些调整器，你可以添加相机的摇晃，应用数学函数，做一个GoPro样子的行为，在路径运动时跟踪一个实体，等等。要了解更多，查看[百科](https://github.com/mchorse/aperture/wiki)（[中文版](https://github.com/ycwei982/aperture/wiki)）。
* **提供平滑摄像机** 在Minema上用于替换原版平滑视角的东西。

## 视频

这是一个关于Aperture的播放列表，内含更新和教程。（由Noble团队制作的[中文教程](http://www.bilibili.com/video/av15303499/)不代表McHorse观点）

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

当你发现了一个Bug，或者mod导致游戏崩溃，我希望你可以在[issue tracker](https://github.com/mchorse/aperture/issues/)报告bug或崩溃, 或私信在 [Twitter](https://twitter.com/McHorsy)。但请确保你给了我一份日志文件（请用[pastebin](https://pastebin.com)或者[Ubuntu Paste](https://paste.ubuntu.com)）然后描述一下如何重现Bug或崩溃。谢谢！

如果你没有英文交流能力，你也可以选择在[中国用户QQ群(328380393)](https://jq.qq.com/?_wv=1027&k=584nNVF)反馈你的Bug或崩溃，Mod的翻译者将会协助你。
