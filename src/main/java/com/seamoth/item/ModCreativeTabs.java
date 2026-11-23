package com.seamoth.item;

import com.seamoth.Seamoth;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> MODULES_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "modules_tab")
            );

    public static final CreativeModeTab MODULES_TAB = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.seamoth.modules"))
            .icon(() -> ModItems.STORAGE_MODULE.getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.STORAGE_MODULE);
            })
            .build();

    public static final ResourceKey<CreativeModeTab> TOOLS_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "tools_tab")
            );

    public static final CreativeModeTab TOOLS_TAB = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.seamoth.tools"))
            .icon(() -> ModItems.REPAIR_TOOL.getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.REPAIR_TOOL);
                output.accept(ModItems.SEAMOTH_CONTAINER);
            })
            .build();

    public static final ResourceKey<CreativeModeTab> ENERGY_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "energy_tab")
            );

    public static final CreativeModeTab ENERGY_TAB = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.seamoth.energy"))
            .icon(() -> ModItems.COPPER_BATTERY.getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.COPPER_BATTERY);
                output.accept(ModItems.COPPER_POWERCELL);
            })
            .build();

    public static void register() {
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MODULES_TAB_KEY, MODULES_TAB);
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TOOLS_TAB_KEY, TOOLS_TAB);
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ENERGY_TAB_KEY, ENERGY_TAB);
    }
}