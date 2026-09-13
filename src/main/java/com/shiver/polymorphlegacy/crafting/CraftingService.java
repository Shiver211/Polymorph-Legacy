package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class CraftingService {
    private CraftingService() {
    }

    @Nullable
    public static IRecipe resolve(Container container, InventoryCrafting matrix, World world,
            EntityPlayer player) {
        CraftingContext context = CraftingContext.of(container);
        if (context == null || !context.ownsMatrix(matrix) || world.isRemote) {
            return CraftingManager.findMatchingRecipe(matrix, world);
        }
        return resolve(context, matrix, world, player);
    }

    @Nullable
    public static IRecipe resolve(CraftingContext context, InventoryCrafting matrix, World world,
            EntityPlayer player) {
        List<IRecipe> recipes = new ArrayList<>();
        List<ResourceLocation> ids = new ArrayList<>();
        List<RecipeChoice> choices = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (!recipe.matches(matrix, world) || !canCraft(recipe, player)) {
                continue;
            }
            ItemStack output = recipe.getCraftingResult(matrix);
            if (output.isEmpty()) {
                continue;
            }
            recipes.add(recipe);
            ids.add(recipe.getRegistryName());
            choices.add(new RecipeChoice(recipe.getRegistryName(), output));
        }
        CraftingState state = context.state();
        ResourceLocation selected = state.getSelection().resolve(ids);
        IRecipe recipe = selected == null ? null : recipes.get(ids.indexOf(selected));
        state.resolved(choices, recipe);
        return recipe;
    }

    private static boolean canCraft(IRecipe recipe, EntityPlayer player) {
        return recipe.isDynamic() || !player.world.getGameRules().getBoolean("doLimitedCrafting")
                || ((EntityPlayerMP) player).getRecipeBook().isUnlocked(recipe);
    }

    public static void openView(EntityPlayerMP player, int windowId, int viewId) {
        CraftingContext context = currentContext(player, windowId);
        if (context == null || viewId == 0) {
            return;
        }
        context.state().openView(viewId);
        refresh(context);
    }

    public static boolean select(EntityPlayerMP player, int windowId, int viewId, ResourceLocation id) {
        CraftingContext context = currentContext(player, windowId);
        if (context == null || viewId == 0 || context.state().getViewId() != viewId) {
            return false;
        }
        // 材料可能已被移动；先基于服务端当前输入重新计算，再接受选择。
        resolve(context, context.getMatrix(), player.world, player);
        List<ResourceLocation> ids = new ArrayList<>();
        for (RecipeChoice choice : context.state().getChoices()) {
            ids.add(choice.getId());
        }
        if (!context.state().getSelection().select(id, ids)) {
            refresh(context);
            return false;
        }
        refresh(context);
        return true;
    }

    @Nullable
    private static CraftingContext currentContext(EntityPlayerMP player, int windowId) {
        Container container = player.openContainer;
        return container.windowId == windowId && container.canInteractWith(player)
                ? CraftingContext.of(container) : null;
    }

    public static void refresh(CraftingContext context) {
        context.refresh();
        context.container.detectAndSendChanges();
    }

    public static void sync(Container container, EntityPlayer player) {
        CraftingContext context = CraftingContext.of(container);
        if (context == null || !(player instanceof EntityPlayerMP) || player.openContainer != container) {
            return;
        }
        CraftingState state = context.state();
        if (state.getViewId() != 0 && state.needsSync()) {
            PolymorphNetwork.CHANNEL.sendTo(new RecipesPacket(container.windowId, state.getViewId(),
                    state.getChoices(), state.getSelection().getSelected()), (EntityPlayerMP) player);
            state.markSynced();
        }
    }

    @Nullable
    public static CraftingContext beginCraft(EntityPlayer player, InventoryCrafting matrix) {
        CraftingContext context = CraftingContext.of(player.openContainer);
        if (player.world.isRemote || context == null || !context.ownsMatrix(matrix)) {
            return null;
        }
        context.state().beginCraft();
        return context;
    }

    public static NonNullList<ItemStack> remainingItems(@Nullable CraftingContext context,
            InventoryCrafting matrix, World world) {
        IRecipe recipe = context == null ? null : context.state().getCraftingRecipe();
        return recipe == null ? CraftingManager.getRemainingItems(matrix, world)
                : recipe.getRemainingItems(matrix);
    }

    public static void finishCraft(@Nullable CraftingContext context) {
        if (context != null) {
            context.state().endCraft();
            if (!context.state().getSelection().isCrafting()) {
                refresh(context);
            }
        }
    }
}
