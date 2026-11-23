package com.seamoth.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

import static com.seamoth.item.SeamothContainerItem.DAMAGE_LOADED;

public class CopperBatteryItem extends BatteryItem {
    public static final int CAPACITY = 100;

    public CopperBatteryItem(Properties properties) {
        super(properties, CAPACITY);
    }
}
