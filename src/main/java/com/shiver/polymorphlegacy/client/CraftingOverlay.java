package com.shiver.polymorphlegacy.client;

import com.shiver.polymorphlegacy.Tags;
import com.shiver.polymorphlegacy.crafting.RecipeChoice;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public final class CraftingOverlay {
    private static final ResourceLocation SELECTOR = texture("selector_button");
    private static final ResourceLocation SELECTOR_HOVER = texture("selector_button_highlighted");
    private static final ResourceLocation OUTPUT = texture("output_button");
    private static final ResourceLocation OUTPUT_HOVER = texture("output_button_highlighted");
    private static final ResourceLocation SELECTED = texture("current_output");
    private static final ResourceLocation SELECTED_HOVER = texture("current_output_highlighted");
    public final GuiContainer gui;
    public final int viewId;
    private List<RecipeChoice> choices = Collections.emptyList();
    private ResourceLocation selected;
    private boolean expanded;
    private int page;
    private int pageSize;
    private int pageCount;
    private int capturedButton = -1;
    private Rectangle button = new Rectangle();
    private Rectangle palette = new Rectangle();

    public CraftingOverlay(GuiContainer gui, int viewId) {
        this.gui = gui;
        this.viewId = viewId;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Tags.MOD_ID, "textures/gui/" + name + ".png");
    }

    public void update(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
        this.choices = choices;
        this.selected = selected;
        if (choices.size() <= 1) {
            expanded = false;
        }
        layout();
    }

    private void layout() {
        Slot slot = gui.inventorySlots.getSlot(0);
        button = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos - 22, 16, 16);
        pageSize = Math.max(1, Math.min(15, (gui.width - 8) / 25));
        pageCount = Math.max(1, (choices.size() + pageSize - 1) / pageSize);
        page = Math.min(page, pageCount - 1);
        int visible = Math.min(pageSize, choices.size() - page * pageSize);
        int width = Math.max(pageCount > 1 ? 75 : 25, visible * 25);
        int height = pageCount > 1 ? 41 : 25;
        int x = Math.max(4, Math.min(button.x + 8 - width / 2, gui.width - width - 4));
        int y = button.y - height - 4;
        if (y < 4) {
            y = button.y + 20;
        }
        palette = new Rectangle(x, Math.max(4, Math.min(y, gui.height - height - 4)), width, height);
    }

    public void draw(int mouseX, int mouseY) {
        if (choices.size() <= 1) {
            return;
        }
        layout();
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 400);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        sprite(button.contains(mouseX, mouseY) ? SELECTOR_HOVER : SELECTOR, button.x, button.y, 16);
        if (expanded) {
            RenderItem renderer = mc.getRenderItem();
            for (int i = page * pageSize; i < Math.min(choices.size(), (page + 1) * pageSize); i++) {
                RecipeChoice choice = choices.get(i);
                int x = palette.x + (i - page * pageSize) * 25;
                int y = palette.y;
                boolean hover = new Rectangle(x, y, 25, 25).contains(mouseX, mouseY);
                boolean active = choice.getId().equals(selected);
                sprite(active ? hover ? SELECTED_HOVER : SELECTED : hover ? OUTPUT_HOVER : OUTPUT, x, y, 25);
                RenderHelper.enableGUIStandardItemLighting();
                renderer.renderItemAndEffectIntoGUI(choice.getOutput(), x + 4, y + 4);
                renderer.renderItemOverlayIntoGUI(mc.fontRenderer, choice.getOutput(), x + 4, y + 4, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepth();
            }
            if (pageCount > 1) {
                Gui.drawRect(palette.x, palette.y + 25, palette.x + palette.width, palette.y + 41, 0xE0101010);
                mc.fontRenderer.drawStringWithShadow("<", palette.x + 8, palette.y + 29, 0xFFFFFF);
                mc.fontRenderer.drawStringWithShadow(">", palette.x + palette.width - 14, palette.y + 29, 0xFFFFFF);
                String label = (page + 1) + "/" + pageCount;
                mc.fontRenderer.drawStringWithShadow(label, palette.x + (palette.width - mc.fontRenderer.getStringWidth(label)) / 2.0F,
                        palette.y + 29, 0xCCCCCC);
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);

        RecipeChoice hovered = choiceAt(mouseX, mouseY);
        if (hovered != null) {
            ItemStack stack = hovered.getOutput();
            boolean advanced = mc.gameSettings.advancedItemTooltips;
            List<String> tooltip = new ArrayList<>(stack.getTooltip(mc.player,
                    advanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL));
            if (!tooltip.isEmpty()) {
                tooltip.set(0, stack.getItem().getForgeRarity(stack).getColor() + tooltip.get(0));
                for (int i = 1; i < tooltip.size(); i++) {
                    tooltip.set(i, TextFormatting.GRAY + tooltip.get(i));
                }
            }
            if (advanced) {
                tooltip.add(TextFormatting.DARK_GRAY + hovered.getId().toString());
            }
            gui.drawHoveringText(tooltip, mouseX, mouseY);
        } else if (button.contains(mouseX, mouseY)) {
            gui.drawHoveringText(Collections.singletonList(I18n.format("polymorph_legacy.choose")), mouseX, mouseY);
        } else if (expanded && pageCount > 1 && palette.contains(mouseX, mouseY)) {
            gui.drawHoveringText(Collections.singletonList(I18n.format("polymorph_legacy.pages")), mouseX, mouseY);
        }
    }

    private static void sprite(ResourceLocation texture, int x, int y, int size) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
    }

    public boolean mouse(int mouseX, int mouseY, int mouseButton, boolean pressed, int wheel) {
        if (mouseButton >= 0 && !pressed && capturedButton == mouseButton) {
            capturedButton = -1;
            return true;
        }
        if (choices.size() <= 1) {
            return false;
        }
        layout();
        if (expanded && wheel != 0 && palette.contains(mouseX, mouseY)) {
            changePage(wheel > 0 ? -1 : 1);
            return true;
        }
        if (mouseButton < 0 || !pressed) {
            return false;
        }
        if (button.contains(mouseX, mouseY)) {
            expanded = !expanded;
            capture(mouseButton);
            return true;
        }
        if (expanded) {
            RecipeChoice choice = choiceAt(mouseX, mouseY);
            if (choice != null) {
                ClientRecipes.select(choice.getId());
                expanded = false;
            } else if (pageCount > 1 && palette.contains(mouseX, mouseY) && mouseY >= palette.y + 25) {
                if (mouseX < palette.x + 25) {
                    changePage(-1);
                } else if (mouseX >= palette.x + palette.width - 25) {
                    changePage(1);
                }
            } else {
                expanded = false;
            }
            // 同时吞掉对应的松开事件，避免点选弹窗时操作到背后的物品槽。
            capture(mouseButton);
            return true;
        }
        return false;
    }

    private void capture(int mouseButton) {
        capturedButton = mouseButton;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    public boolean key(int key) {
        if (!expanded) {
            return false;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            expanded = false;
            return true;
        }
        if (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT) {
            changePage(key == Keyboard.KEY_LEFT ? -1 : 1);
            return true;
        }
        return false;
    }

    private void changePage(int delta) {
        page = Math.floorMod(page + delta, pageCount);
        layout();
    }

    @Nullable
    private RecipeChoice choiceAt(int x, int y) {
        if (!expanded || x < palette.x || y < palette.y || y >= palette.y + 25) {
            return null;
        }
        int column = (x - palette.x) / 25;
        int index = page * pageSize + column;
        return column < pageSize && index < choices.size() ? choices.get(index) : null;
    }

    public List<Rectangle> extraAreas() {
        if (choices.size() <= 1) {
            return Collections.emptyList();
        }
        layout();
        List<Rectangle> areas = new ArrayList<>();
        areas.add(new Rectangle(button));
        if (expanded) {
            areas.add(new Rectangle(palette));
        }
        return areas;
    }

    @Nullable
    public ItemStack ingredientUnderMouse(int x, int y) {
        layout();
        RecipeChoice choice = choiceAt(x, y);
        return choice == null ? null : choice.getOutput();
    }
}
