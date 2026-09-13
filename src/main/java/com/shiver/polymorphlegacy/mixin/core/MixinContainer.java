package com.shiver.polymorphlegacy.mixin.core;

import com.shiver.polymorphlegacy.crafting.CraftingState;
import com.shiver.polymorphlegacy.crafting.CraftingStateHolder;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Container.class)
public abstract class MixinContainer implements CraftingStateHolder {
    @Unique
    private final CraftingState polymorph$state = new CraftingState();

    @Override
    public CraftingState polymorph$getCraftingState() {
        return polymorph$state;
    }

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private void polymorph$deferIntermediateChanges(World world, EntityPlayer player,
            InventoryCrafting matrix, InventoryCraftResult result, CallbackInfo ci) {
        if (polymorph$state.getSelection().isCrafting()) {
            ci.cancel();
        }
    }

    @Redirect(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/crafting/CraftingManager;findMatchingRecipe(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Lnet/minecraft/item/crafting/IRecipe;"))
    private IRecipe polymorph$resolve(InventoryCrafting inventory, World world, World containerWorld,
            EntityPlayer player, InventoryCrafting matrix, InventoryCraftResult result) {
        return CraftingService.resolve((Container) (Object) this, inventory, world, player);
    }

    @Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
    private void polymorph$sync(World world, EntityPlayer player, InventoryCrafting matrix,
            InventoryCraftResult result, CallbackInfo ci) {
        if (!world.isRemote) {
            CraftingService.sync((Container) (Object) this, player);
        }
    }

    @Inject(method = "onContainerClosed", at = @At("HEAD"))
    private void polymorph$close(EntityPlayer player, CallbackInfo ci) {
        polymorph$state.clear();
    }
}
