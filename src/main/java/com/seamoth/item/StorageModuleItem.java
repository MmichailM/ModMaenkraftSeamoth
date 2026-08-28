package com.seamoth.item;

import net.minecraft.world.item.Item;

public class StorageModuleItem extends Item {
    public StorageModuleItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
