package com.seamoth.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

public class PowerCellItem extends Item {
    public PowerCellItem(Properties properties) {
        super(properties.durability(200).component(DataComponents.DAMAGE, 1));
    }
}
