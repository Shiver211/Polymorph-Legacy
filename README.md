# Polymorph Legacy

Minecraft 1.12.2 Cleanroom 的合成冲突选择模组。

需要 MixinBooter；已内置该组件的 Cleanroom 无需重复安装。

在背包 2×2、原版工作台 3×3 或匠魂合成站中放入材料。匹配多个配方时，输出槽上方会显示切换按钮；点击按钮，再点击目标产物即可选择。候选过多时可用滚轮、左右方向键或翻页按钮切换，按 Esc 收起列表。

选择在当前容器中生效；连续合成时，只要配方仍然匹配就会保持选择。关闭容器或材料不再匹配时重置。

兼容Tinkers' Antique合成站，支持连续合成、配方返还物和 JEI/HEI 填料选择。同一合成站的多个窗口同步当前选择，新打开的窗口继承该选择；所有玩家关闭后不保存选择。

内置JEI联动。成功填充合成配方后会按配方注册名选择目标。

已支持原版背包、工作台、AE2 有线/无线合成终端和匠魂合成站。

## 其他模组接入

Java API 位于 `com.shiver.polymorphlegacy.api`，客户端入口位于 `api.client`。接入模组需在容器两端实现 `CraftingContextProvider`。上下文应在每个容器实例中复用。

```java
private CraftingContext polymorphContext;

@Override
public CraftingContext polymorph$getCraftingContext() {
    if (polymorphContext == null) {
        polymorphContext = new CraftingContext(this) {
            @Override public InventoryCrafting getMatrix() { return craftMatrix; }
            @Override public Slot getOutputSlot() { return container.getSlot(0); }
        };
    }
    return polymorphContext;
}

// 在容器原有的结果槽更新逻辑中调用；服务端会枚举候选并保持当前选择。
IRecipe recipe = PolymorphApi.resolve(this, craftMatrix, player.world, player);
result.setInventorySlotContents(0,
        recipe == null ? ItemStack.EMPTY : recipe.getCraftingResult(craftMatrix));
PolymorphApi.sync(this, player);
```

这里的容器类实现 `CraftingContextProvider`，代码省略了类声明和字段定义。如果容器直接使用原版 `slotChangedCraftingGrid`，Mixin 已处理配方解析和同步，无需重复调用。自定义结果槽取物时可用 `PolymorphApi.beginCraft(player, craftMatrix)` 创建 `CraftingSession`，通过 `getRemainingItems` 获取所选配方的返还物，并在 `finally` 中调用 `close()`。客户端配方查看器填料后可调用 `PolymorphClientApi.selectRecipe(container, recipeId)`；多面板 GUI 可实现 `CraftingGuiOrigin` 指定按钮坐标基准。

本Mod采用 LGPL-3.0-or-later，按钮素材来自 Illusive Soulworks 的 Polymorph。
