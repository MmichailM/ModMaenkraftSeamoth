package com.seamoth.item;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class RepairToolItem extends Item {
    public static final int DAMAGE_DISCHARGED = 100;
    public static final int DAMAGE_CHARGED = 0;

    private SeamothEntity seamothEntity;

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        seamothEntity = null;
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        if (seamothEntity == null) {
            return;
        }

        if (ticksRemaining % 5 == 4 && !level.isClientSide()) {
            seamothEntity.heal(3);
        }

        if (seamothEntity.isDeadOrDying()) {
            seamothEntity = null;
            livingEntity.stopUsingItem();
        }
    }

    public RepairToolItem(Properties properties) {
        super(properties.durability(101).component(DataComponents.DAMAGE, DAMAGE_CHARGED));
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type) {
        if (target instanceof SeamothEntity seamoth && type == InteractionHand.MAIN_HAND) {
            this.seamothEntity = seamoth;
            player.startUsingItem(type);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
