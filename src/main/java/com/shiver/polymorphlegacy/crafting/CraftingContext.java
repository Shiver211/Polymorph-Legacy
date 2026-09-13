package com.shiver.polymorphlegacy.crafting;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public abstract class CraftingContext {
    public final Container container;

    protected CraftingContext(Container container) {
        this.container = container;
    }

    @Nullable
    public static CraftingContext of(Container container) {
        if (container instanceof CraftingContextProvider) {
            return ((CraftingContextProvider) container).polymorph$getCraftingContext();
        }
        if (container != null && container.getClass() == ContainerWorkbench.class) {
            ContainerWorkbench workbench = (ContainerWorkbench) container;
            return new VanillaContext(container, workbench.craftMatrix);
        }
        if (container != null && container.getClass() == ContainerPlayer.class) {
            ContainerPlayer inventory = (ContainerPlayer) container;
            return new VanillaContext(container, inventory.craftMatrix);
        }
        return null;
    }

    public abstract InventoryCrafting getMatrix();

    public abstract Slot getOutputSlot();

    public boolean ownsMatrix(InventoryCrafting matrix) {
        return getMatrix() == matrix;
    }

    public void refresh() {
        container.onCraftMatrixChanged(getMatrix());
    }

    public void detectChanges() {
    }

    public void receive(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
    }

    public CraftingState state() {
        return ((CraftingStateHolder) container).polymorph$getCraftingState();
    }

    private static final class VanillaContext extends CraftingContext {
        private final InventoryCrafting matrix;

        private VanillaContext(Container container, InventoryCrafting matrix) {
            super(container);
            this.matrix = matrix;
        }

        @Override
        public InventoryCrafting getMatrix() {
            return matrix;
        }

        @Override
        public Slot getOutputSlot() {
            return container.getSlot(0);
        }
    }
}
