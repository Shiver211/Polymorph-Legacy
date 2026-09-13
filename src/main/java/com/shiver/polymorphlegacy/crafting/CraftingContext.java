package com.shiver.polymorphlegacy.crafting;

import javax.annotation.Nullable;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryCraftResult;

public final class CraftingContext {
    public final Container container;
    public final InventoryCrafting matrix;
    public final InventoryCraftResult result;

    private CraftingContext(Container container, InventoryCrafting matrix, InventoryCraftResult result) {
        this.container = container;
        this.matrix = matrix;
        this.result = result;
    }

    @Nullable
    public static CraftingContext of(Container container) {
        if (container != null && container.getClass() == ContainerWorkbench.class) {
            ContainerWorkbench workbench = (ContainerWorkbench) container;
            return new CraftingContext(container, workbench.craftMatrix, workbench.craftResult);
        }
        if (container != null && container.getClass() == ContainerPlayer.class) {
            ContainerPlayer inventory = (ContainerPlayer) container;
            return new CraftingContext(container, inventory.craftMatrix, inventory.craftResult);
        }
        return null;
    }

    public CraftingState state() {
        return ((CraftingStateHolder) container).polymorph$getCraftingState();
    }
}
