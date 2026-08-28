package com.seamoth.item;

import com.seamoth.Seamoth;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> MODULES = TagKey.create(Registries.ITEM, Seamoth.id("modules"));
    public static final TagKey<Item> BATTERIES = TagKey.create(Registries.ITEM, Seamoth.id("batteries"));
}
