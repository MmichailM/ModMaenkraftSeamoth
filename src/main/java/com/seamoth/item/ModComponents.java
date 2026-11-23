package com.seamoth.item;

import com.mojang.serialization.Codec;
import com.seamoth.Seamoth;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModComponents {
    public static final DataComponentType<Integer> ENERGY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Seamoth.id("energy"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static void register() {
        Seamoth.LOGGER.info("Registered Mod Components");
    }
}
