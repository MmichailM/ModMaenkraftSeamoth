package com.seamoth.item;

import com.seamoth.Seamoth;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
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

    public static void register() {
        net.minecraft.core.Registry.register(
                net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB,
                MODULES_TAB_KEY,
                MODULES_TAB
        );
    }
}