package com.seamoth.item;

import com.seamoth.Seamoth;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import static com.seamoth.Seamoth.MOD_ID;
import static com.seamoth.item.RepairToolItem.DAMAGE_CHARGED;
import static com.seamoth.item.SeamothContainerItem.DAMAGE_EMPTY;

public class ModItems {
    public static final ResourceKey<Item> SEAMOTH_CONTAINER_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "seamoth_container"));
    public static final Item SEAMOTH_CONTAINER = new SeamothContainerItem(new Item.Properties().durability(100).component(DataComponents.DAMAGE, DAMAGE_EMPTY).setId(SEAMOTH_CONTAINER_KEY));

    public static final ResourceKey<Item> STORAGE_MODULE_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "storage_module"));
    public static final Item STORAGE_MODULE = new StorageModuleItem(new Item.Properties().stacksTo(1).setId(STORAGE_MODULE_KEY));

    public static final ResourceKey<Item> REPAIR_TOOL_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "repair_tool"));
    public static final Item REPAIR_TOOL = new RepairToolItem(new Item.Properties().durability(101).component(DataComponents.DAMAGE, DAMAGE_CHARGED).setId(REPAIR_TOOL_KEY));

    public static final ResourceKey<Item> SEAMOTH_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "seamoth_item"));
    public static final Item SEAMOTH_ITEM = new SeamothItem(new Item.Properties().stacksTo(1).setId(SEAMOTH_ITEM_KEY));

    public static final ResourceKey<Item> COPPER_BATTERY_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "copper_battery"));
    public static final Item COPPER_BATTERY = new CopperBatteryItem(new Item.Properties().stacksTo(1).setId(COPPER_BATTERY_KEY));

    public static final ResourceKey<Item> COPPER_POWERCELL_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "copper_powercell"));
    public static final Item COPPER_POWERCELL = new CopperPowerCellItem(new Item.Properties().stacksTo(1).setId(COPPER_POWERCELL_KEY));

    public static void registerModItems() {
        Registry.register(BuiltInRegistries.ITEM, SEAMOTH_CONTAINER_KEY, SEAMOTH_CONTAINER);
        Registry.register(BuiltInRegistries.ITEM, STORAGE_MODULE_KEY, STORAGE_MODULE);
        Registry.register(BuiltInRegistries.ITEM, REPAIR_TOOL_KEY, REPAIR_TOOL);
        Registry.register(BuiltInRegistries.ITEM, SEAMOTH_ITEM_KEY, SEAMOTH_ITEM);
        Registry.register(BuiltInRegistries.ITEM, COPPER_BATTERY_KEY, COPPER_BATTERY);
        Registry.register(BuiltInRegistries.ITEM, COPPER_POWERCELL_KEY, COPPER_POWERCELL);

        Seamoth.LOGGER.info("Registered Mod Items");
    }
}
