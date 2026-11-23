package com.seamoth.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

public class CopperPowerCellItem extends PowerCellItem {
    public static final int CAPACITY = 200;

    public CopperPowerCellItem(Properties properties) {
       super(properties, CAPACITY);
    }
}
