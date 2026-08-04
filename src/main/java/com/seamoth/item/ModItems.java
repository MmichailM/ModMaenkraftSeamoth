package com.seamoth.item;

import com.seamoth.Seamoth;
import com.seamoth.entity.SeamothEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.seamoth.Seamoth.MOD_ID;
import static com.seamoth.item.SeamothContainerItem.DAMAGE_EMPTY;

public class ModItems {
    public static final ResourceKey<Item> SEAMOTH_CONTAINER_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "seamoth_container"));
    public static final Item SEAMOTH_CONTAINTER = new SeamothContainerItem(new Item.Properties().durability(100).component(DataComponents.DAMAGE, DAMAGE_EMPTY).setId(SEAMOTH_CONTAINER_KEY));

    public static void registerModItems() {
        Registry.register(BuiltInRegistries.ITEM, SEAMOTH_CONTAINER_KEY, SEAMOTH_CONTAINTER);

        Seamoth.LOGGER.info("Registered Mod Items");
    }
}
