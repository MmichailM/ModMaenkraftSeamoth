package com.seamoth.item;

import net.minecraft.world.item.ItemStack;

public abstract class PowerCellItem extends EnergyItem {
    protected PowerCellItem(Properties properties, int maxEnergy) {
        super(properties, maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        return 0x55FFFF;
    }
}
