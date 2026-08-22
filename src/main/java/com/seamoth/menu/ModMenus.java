package com.seamoth.menu;

import com.seamoth.Seamoth;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;


public class ModMenus {
    private static final ResourceKey<MenuType<?>> SEAMOTH_MENU_KEY = ResourceKey.create(Registries.MENU, Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "seamoth"));

    public static final MenuType<SeamothMenu> SEAMOTH_MENU = Registry.register(
            BuiltInRegistries.MENU,
            SEAMOTH_MENU_KEY,
            new ExtendedMenuType<>(
                    (syncId, inventory, entityId) -> new SeamothMenu(syncId, inventory, entityId),
                    ByteBufCodecs.VAR_INT
            )
    );


    public static void register() {
        Seamoth.LOGGER.info("Registered Seamoth menu type");
    }

}
