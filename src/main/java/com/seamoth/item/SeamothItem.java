package com.seamoth.item;

import com.seamoth.Seamoth;
import com.seamoth.entity.SeamothEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SeamothItem extends Item {
    public SeamothItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState state = level.getBlockState(clickedPos);

        BlockPos spawnPos;
        if (!state.getFluidState().isEmpty()) {
            spawnPos = clickedPos;
        } else {
            spawnPos = state.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(direction);
        }

        return this.spawnSeamoth(serverLevel, spawnPos, context.getItemInHand(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // useOn() не срабатывает, когда игрок смотрит на открытую воду без
        // блока под прицелом (стандартный ray trace жидкости игнорирует).
        // Поэтому здесь делаем свой рейкаст, разрешая попадание в воду —
        // тот же приём, что у ванильного BucketItem и у SeamothContainerItem.
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(clickedPos);

        if (state.getFluidState().isEmpty()) {
            // Промахнулись мимо воды (например, попали в стенку блока рядом) —
            // отдаём обработку обычному useOn через блок-путь.
            return InteractionResult.PASS;
        }

        return this.spawnSeamoth(serverLevel, clickedPos, player.getItemInHand(hand), player, hand);
    }

    private InteractionResult spawnSeamoth(ServerLevel serverLevel, BlockPos spawnPos, ItemStack stack, Player player, InteractionHand hand) {
        SeamothEntity seamoth = Seamoth.SEAMOTH.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);

        if (seamoth == null) {
            return InteractionResult.FAIL;
        }

        float yaw = Mth.wrapDegrees(serverLevel.getRandom().nextFloat() * 360.0F);
        seamoth.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yaw, 0.0F);
        serverLevel.addFreshEntity(seamoth);

        if (player != null) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.swing(hand);
        }

        return InteractionResult.SUCCESS;
    }
}