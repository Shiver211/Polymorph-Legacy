# Polymorph Legacy

Minecraft 1.12.2 Cleanroom 的合成冲突选择模组，Mod ID 为 `polymorph_legacy`。

把 `polymorph_legacy-1.0.0.jar` 放入客户端和服务端的 `mods` 文件夹。需要 MixinBooter；已内置该组件的 Cleanroom 无需重复安装。

在背包 2×2 或原版工作台 3×3 中放入材料。匹配多个配方时，输出槽上方会显示切换按钮；点击按钮，再点击目标产物即可选择。候选过多时可用滚轮、左右方向键或翻页按钮切换，按 Esc 收起列表。

选择在当前容器中生效；连续合成时，只要配方仍然匹配就会保持选择。关闭容器或材料不再匹配时重置。

JEI `4.16.1.1013` / HEI `4.28.0` 为兼容目标，二选一安装。成功填充合成配方后会按配方注册名选择目标。旧版 JEI/HEI 的自动填料功能要求服务端也安装对应组件；手动切换不依赖配方浏览器。

当前范围仅包括原版背包和工作台，不包含熔炉、冲突报告、第三方工作台或机器，也不提供 FastWorkbench 专项兼容。

保留原有 RFG 工程、MCP stable 39 和 Java 8 编译目标。在 Windows PowerShell 中构建：

```powershell
.\gradlew.ps1 assemble -x test --console=plain
```

产物位于 `build/libs`。`gradlew.ps1` 只为本次 Gradle 进程设置临时目录，解决 Windows 短路径引起的回环连接问题。

本移植采用 LGPL-3.0-or-later，按钮素材来自 Illusive Soulworks 的 Polymorph。源码中的 `LICENSE`、`COPYING`、`COPYING.LESSER` 保留上游声明，`LICENSE-CLEANROOM-TEMPLATE` 保留开发模板的 MIT 声明；这些协议文件不打入 JAR。
