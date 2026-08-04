package com.seamoth.item;

import com.seamoth.Seamoth;
import com.seamoth.entity.SeamothEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.xml.crypto.Data;

public class SeamothContainerItem extends Item {
    public static final int DAMAGE_EMPTY = 99;
    public static final int DAMAGE_LOADED = 1;

    public SeamothContainerItem(Properties properties) {
        super(properties.durability(100).component(DataComponents.DAMAGE, DAMAGE_EMPTY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(item)) {
            return InteractionResult.PASS;
        }

        if (item.getDamageValue() == 1 && item.has(DataComponents.CUSTOM_DATA)) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                FluidState fluidState = level.getFluidState(pos);

                if (fluidState.is(FluidTags.WATER)) {

                    if(!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        CustomData customData = item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();

                        if (tag.contains("SeamothData")) {
                            CompoundTag seamothDataTag = tag.getCompoundOrEmpty("SeamothData");

                            Entity entity = EntityType.loadEntityRecursive(seamothDataTag, serverLevel, EntitySpawnReason.LOAD, createdEntity -> {
                                createdEntity.absSnapTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, player.getYRot(), 0.0F);
                                return createdEntity;
                            });

                            if (entity != null) {
                                serverLevel.addFreshEntityWithPassengers(entity);
                                item.remove(DataComponents.CUSTOM_DATA);
                                item.setDamageValue(99);
                                player.getCooldowns().addCooldown(item, 5);
                            } else {
                                Seamoth.LOGGER.warn("SeamothContainerItem: loadEntityRecursive вернул null, SeamothData не восстановлен: {}", seamothDataTag);
                            }

                        } else {
                            Seamoth.LOGGER.warn("SeamothContainerItem: DAMAGE==1, но тег SeamothData отсутствует: {}", tag);
                        }

                        item.remove(DataComponents.CUSTOM_DATA);
                        item.setDamageValue(DAMAGE_EMPTY);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type) {
        Level level = player.level();

        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.PASS;
        }

        if (target instanceof SeamothEntity seamoth && !level.isClientSide()) {
            if (itemStack.getDamageValue() == 99 || !itemStack.has(DataComponents.CUSTOM_DATA)) {
                TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                seamoth.saveWithoutId(output);
                output.putString("id", EntityType.getKey(seamoth.getType()).toString());
                CompoundTag entityTag = (CompoundTag) output.buildResult();

                CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.put("SeamothData", entityTag));

                itemStack.setDamageValue(DAMAGE_LOADED);
                player.getCooldowns().addCooldown(itemStack, 5);

                seamoth.discard();

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
