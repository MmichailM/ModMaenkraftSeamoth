package com.seamoth.menu;

import com.seamoth.entity.SeamothEntity;
import com.seamoth.item.ModItemTags;
import com.seamoth.item.StorageModuleItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SeamothMenu extends AbstractContainerMenu {
    public static final int BATTERY_SLOT = 0;

    public static final int MODULE_SLOT_START = 1;
    public static final int MODULE_SLOT_COUNT = 4;

    public static final int STORAGE_SLOT_START = MODULE_SLOT_START + MODULE_SLOT_COUNT;

    public static final int SPECIAL_SLOT_COUNT = MODULE_SLOT_START + MODULE_SLOT_COUNT + 27;

    private static final int SLOT_SIZE = 18;

    private static final int BATTERY_SLOT_X = 62;
    private static final int BATTERY_SLOT_Y = 18;

    private static final int MODULE_SLOTS_X = BATTERY_SLOT_X + SLOT_SIZE + 8;
    private static final int MODULE_SLOTS_Y = 18;

    private static final int SEAMOTH_INV_Y_OFFSET = 40;
    private static final int PLAYER_INV_Y_OFFSET = 126;

    private final Container specialInventory;

    public final SeamothEntity seamoth;

    public SeamothMenu(int syncId, Inventory playerInventory, SeamothEntity seamoth) {
        super(ModMenus.SEAMOTH_MENU, syncId);
        this.seamoth = seamoth;
        this.specialInventory = seamoth.getSpecialInventory();
        this.specialInventory.startOpen(playerInventory.player);

        this.addSpecialSlots();
        this.addStorageSlots();
        this.addPlayerInventorySlots(playerInventory);
    }

    public SeamothMenu(int syncId, Inventory playerInventory, int seamothEntityId) {
        super(ModMenus.SEAMOTH_MENU, syncId);

        Entity entity = playerInventory.player.level().getEntity(seamothEntityId);
        SeamothEntity found = entity instanceof SeamothEntity s ? s : null;

        this.seamoth = found;
        this.specialInventory = found != null ? found.getSpecialInventory() : new SimpleContainer(SPECIAL_SLOT_COUNT);

        this.addSpecialSlots();
        this.addStorageSlots();
        this.addPlayerInventorySlots(playerInventory);
    }

    private static boolean isPowerCellItem(ItemStack stack) {
        return stack.is(ModItemTags.POWERCELLS);
    }

    private static boolean isModuleItem(ItemStack stack) {
        return stack.is(ModItemTags.MODULES);
    }

    private static boolean isStorageModule(ItemStack stack) {
        return stack.getItem() instanceof StorageModuleItem;
    }

    private boolean hasStorageModule() {
        for (int i = MODULE_SLOT_START; i < MODULE_SLOT_START + MODULE_SLOT_COUNT; i++) {
            if (isStorageModule(this.specialInventory.getItem(i))) {
                return true;
            }
        }

        return false;
    }

    private boolean storageHasItems() {
        for (int i = STORAGE_SLOT_START; i < SPECIAL_SLOT_COUNT; i++) {
            if (!this.specialInventory.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void addSpecialSlots() {
        this.addSlot(new Slot(this.specialInventory, BATTERY_SLOT, BATTERY_SLOT_X, BATTERY_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isPowerCellItem(stack);
            }
        });

        for (int i = 0; i < MODULE_SLOT_COUNT; i++) {
            int slotIndex = MODULE_SLOT_START + i;
            int slotX = MODULE_SLOTS_X + i * SLOT_SIZE;

            this.addSlot(new Slot(this.specialInventory, slotIndex, slotX, MODULE_SLOTS_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isModuleItem(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    if (isStorageModule(this.getItem()) && storageHasItems()) {
                        return false;
                    }

                    return super.mayPickup(player);
                }
            });
        }
    }

    private void addStorageSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = (MODULE_SLOT_COUNT + 1) + col + (row * 9);
                this.addSlot(new Slot(
                        this.specialInventory,
                        slotIndex,
                        8 + col * SLOT_SIZE,
                        SEAMOTH_INV_Y_OFFSET + row * SLOT_SIZE
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return true;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return hasStorageModule() && super.mayPickup(player);
                    }

                    @Override
                    public boolean isActive() {
                        return hasStorageModule();
                    }
                });
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * SLOT_SIZE,
                        PLAYER_INV_Y_OFFSET + row * SLOT_SIZE
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * SLOT_SIZE,
                    8 + PLAYER_INV_Y_OFFSET + 3 * SLOT_SIZE
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.specialInventory.stillValid(player) && (this.seamoth == null || (this.seamoth.isAlive() && player.getVehicle() == this.seamoth));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();

        if (!hasStorageModule() && !(slotStack.getItem() instanceof StorageModuleItem)) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slotStack.copy();
        int mainEnd = SPECIAL_SLOT_COUNT;
        boolean moved;

        if (index < SPECIAL_SLOT_COUNT) {
            // из спецслотов — в трюм, затем в инвентарь игрока
            moved = this.moveItemStackTo(slotStack, mainEnd, this.slots.size(), false)
                    || this.moveItemStackTo(slotStack, SPECIAL_SLOT_COUNT, mainEnd, false);
        } else if (index < mainEnd) {
            // из трюма — в инвентарь игрока, либо в спецслот, если это модуль
            moved = this.moveItemStackTo(slotStack, mainEnd, this.slots.size(), false)
                    || this.moveItemStackTo(slotStack, 0, SPECIAL_SLOT_COUNT, false);
        } else {
            // из инвентаря игрока — в спецслот (если модуль), иначе в трюм
            moved = this.moveItemStackTo(slotStack, 0, SPECIAL_SLOT_COUNT, false)
                    || this.moveItemStackTo(slotStack, SPECIAL_SLOT_COUNT, mainEnd, false);
        }

        if (!moved || slotStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, slotStack);

        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.specialInventory.stopOpen(player);
    }
}
