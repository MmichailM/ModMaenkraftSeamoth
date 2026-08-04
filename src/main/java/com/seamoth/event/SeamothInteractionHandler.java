package com.seamoth.event;

import com.seamoth.entity.SeamothEntity;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.system.ffm.mapping.Mapping;

public class SeamothInteractionHandler {
    public static boolean isSealedInSeamoth(Player player) {
        return player.getVehicle() instanceof SeamothEntity;
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isSealedInSeamoth(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (isSealedInSeamoth(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (isSealedInSeamoth(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (isSealedInSeamoth(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (isSealedInSeamoth(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }
}
