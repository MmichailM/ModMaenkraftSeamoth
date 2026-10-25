package com.seamoth.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

import static com.seamoth.item.SeamothContainerItem.DAMAGE_LOADED;

public class BatteryItem extends Item {
    public BatteryItem(Properties properties) {
        super(properties.durability(100).component(DataComponents.DAMAGE, DAMAGE_LOADED));
    }
}
