package com.shiver.polymorphlegacy.client;

import com.shiver.polymorphlegacy.crafting.CraftingContext;
import com.shiver.polymorphlegacy.network.OpenViewPacket;
import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import com.shiver.polymorphlegacy.network.SelectRecipePacket;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class ClientRecipes {
    private static CraftingOverlay overlay;
    private static int nextViewId;
    private static boolean pendingOpenView;

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiInventory) && !(event.getGui() instanceof GuiCrafting)) {
            return;
        }
        GuiContainer gui = (GuiContainer) event.getGui();
        if (CraftingContext.of(gui.inventorySlots) == null) {
            return;
        }
        // 背包的 windowId 总是 0，额外的会话号用于丢弃上次打开界面的延迟消息。
        overlay = new CraftingOverlay(gui, ++nextViewId);
        pendingOpenView = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void draw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (isCurrent(event.getGui())) {
            overlay.draw(event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void mouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isCurrent(event.getGui())) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        int x = Mouse.getEventX() * event.getGui().width / mc.displayWidth;
        int y = event.getGui().height - Mouse.getEventY() * event.getGui().height / mc.displayHeight - 1;
        if (overlay.mouse(x, y, Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void keyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (isCurrent(event.getGui()) && Keyboard.getEventKeyState() && overlay.key(Keyboard.getEventKey())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            overlay = null;
            pendingOpenView = false;
        } else if (pendingOpenView && isCurrent(mc.currentScreen)) {
            // 工作台在 InitGuiEvent.Post 之后才赋予 windowId，等开窗流程完成后再请求配方。
            PolymorphNetwork.CHANNEL.sendToServer(new OpenViewPacket(overlay.gui.inventorySlots.windowId, overlay.viewId));
            pendingOpenView = false;
        }
    }

    private static boolean isCurrent(Object gui) {
        Minecraft mc = Minecraft.getMinecraft();
        return overlay != null && overlay.gui == gui && mc.player != null
                && mc.player.openContainer == overlay.gui.inventorySlots;
    }

    public static void receive(RecipesPacket packet) {
        Minecraft mc = Minecraft.getMinecraft();
        if (overlay != null && mc.player != null && mc.player.openContainer == overlay.gui.inventorySlots
                && overlay.gui.inventorySlots.windowId == packet.windowId && overlay.viewId == packet.viewId) {
            overlay.update(packet.choices, packet.selected);
        }
    }

    public static void select(ResourceLocation recipe) {
        if (overlay != null) {
            PolymorphNetwork.CHANNEL.sendToServer(new SelectRecipePacket(overlay.gui.inventorySlots.windowId,
                    overlay.viewId, recipe));
        }
    }

    public static void selectFromJei(Container container, ResourceLocation recipe) {
        if (overlay != null && overlay.gui.inventorySlots == container && CraftingContext.of(container) != null) {
            select(recipe);
        }
    }

    public static List<Rectangle> extraAreas(GuiContainer gui) {
        return isCurrent(gui) ? overlay.extraAreas() : Collections.emptyList();
    }

    @Nullable
    public static ItemStack ingredientUnderMouse(GuiContainer gui, int mouseX, int mouseY) {
        return isCurrent(gui) ? overlay.ingredientUnderMouse(mouseX, mouseY) : null;
    }
}
