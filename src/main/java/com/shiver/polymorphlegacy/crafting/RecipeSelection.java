package com.shiver.polymorphlegacy.crafting;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public final class RecipeSelection {
    private ResourceLocation selected;
    private int craftingDepth;

    @Nullable
    public ResourceLocation resolve(List<ResourceLocation> candidates) {
        ResourceLocation result = candidates.contains(selected) ? selected
                : candidates.isEmpty() ? null : candidates.get(0);
        if (!isCrafting()) {
            selected = result;
        }
        return result;
    }

    public boolean select(ResourceLocation id, List<ResourceLocation> candidates) {
        if (!candidates.contains(id)) {
            return false;
        }
        selected = id;
        return true;
    }

    @Nullable
    public ResourceLocation getSelected() {
        return selected;
    }

    public void beginCraft() {
        craftingDepth++;
    }

    public void endCraft() {
        craftingDepth--;
    }

    public boolean isCrafting() {
        return craftingDepth > 0;
    }

    public void clear() {
        selected = null;
        craftingDepth = 0;
    }
}
