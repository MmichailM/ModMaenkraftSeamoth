package com.seamoth.client.mixin;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow @Final private Quaternionf rotation;

    @Inject(method = "setRotation", at = @At("RETURN"))
    private void seamoth$overrideCameraRotation(float yRot, float xRot, CallbackInfo ci) {
        // Проверяем, сидит ли игрок в Мотыльке
        if (this.entity != null && this.entity.getVehicle() instanceof SeamothEntity seamoth) {

            // Получаем плавный partialTick в 1.21.2+
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

            // Сглаживаем Pitch и Yaw Мотылька
            float seamothPitch = Mth.lerp(partialTick, seamoth.xRotO, seamoth.getXRot());
            float seamothYaw = Mth.lerp(partialTick, seamoth.yRotO, seamoth.getYRot());

            // 1. Обновляем градусы в полях камеры
            this.xRot = seamothPitch;
            this.yRot = seamothYaw;

            // 2. В 1.21.2+ рендер мира использует кватернион! Пересчитываем его вручную под Мотылек:
            this.rotation.identity();
            this.rotation.rotationYXZ(
                    (float) Math.toRadians(180.0F - seamothYaw),
                    (float) Math.toRadians(-seamothPitch),
                    0.0F
            );
        }
    }
}