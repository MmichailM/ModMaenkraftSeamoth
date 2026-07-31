package com.seamoth.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.seamoth.client.duck.SeamothTiltAccessor;
import com.seamoth.entity.SeamothEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Unique
    private static final float PIVOT_HEIGHT = 0.5F;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void seamoth$capturePitch(T entity, S state, float partialTick, CallbackInfo ci) {
        SeamothTiltAccessor accessor = (SeamothTiltAccessor) state;
        if (entity.isPassenger() && entity.getVehicle() instanceof SeamothEntity seamoth && seamoth.isInWater()) {
            accessor.seamoth$setShouldTilt(true);
            accessor.seamoth$setPitch(Mth.lerp(partialTick, seamoth.xRotO, seamoth.getXRot()));
        } else {
            accessor.seamoth$setShouldTilt(false);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void seamoth$tiltRiderWithVehicle(S state, PoseStack poseStack, float bodyYRot, float scale, CallbackInfo ci) {
        SeamothTiltAccessor accessor = (SeamothTiltAccessor) state;
        if (accessor.seamoth$isShouldTilt()) {
            poseStack.translate(0.0D, PIVOT_HEIGHT, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-accessor.seamoth$getPitch()));
            poseStack.translate(0.0D, -PIVOT_HEIGHT, 0.0D);
        }
    }
}