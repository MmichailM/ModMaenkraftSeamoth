package com.seamoth.client.screen;

import com.seamoth.menu.SeamothMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

public class SeamothScreen extends AbstractContainerScreen<SeamothMenu> {
    private static final int PANEL_BORDER_COLOR = 0x80373737;
    private static final int PANEL_FILL_COLOR = 0x802C6BB7/*0xFFC6C6C6*/;
    private static final int SLOT_BORDER_COLOR = 0x80373737;
    private static final int SLOT_WELL_COLOR = 0x802C6BB7/*0x808B8B8B*/;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public SeamothScreen(SeamothMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166 + 40);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        int x = this.titleLabelX;
        int y = this.titleLabelY;

        int piX = this.inventoryLabelX;
        int piY = this.inventoryLabelY;

        graphics.text(this.font, this.title, x, y, TEXT_COLOR);
        graphics.text(this.font, this.playerInventoryTitle, piX, piY, TEXT_COLOR);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL_BORDER_COLOR);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, PANEL_FILL_COLOR);

        // Рисуем серые квадраты под слоты
        for (Slot slot : this.menu.slots) {
            if (!slot.isActive()) {
                continue;
            }

            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_BORDER_COLOR);
            graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, SLOT_WELL_COLOR);
        }
    }
}
