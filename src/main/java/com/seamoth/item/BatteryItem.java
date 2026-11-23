package com.seamoth.item;

import net.minecraft.world.item.ItemStack;

public abstract class BatteryItem extends EnergyItem {
    protected BatteryItem(Properties properties, int maxEnergy) {
        super(properties, maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        return 0xFFAA00;
    }
}
