package com.shiver.polymorphlegacy.crafting;

import javax.annotation.Nullable;

/** 由可选兼容模块注入到容器；通用逻辑不引用对应模组的类。 */
public interface CraftingContextProvider {
    @Nullable
    CraftingContext polymorph$getCraftingContext();
}
