package com.seamoth.client.mixin;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Redirect(
            method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F")
    )
    private float seamoth$useSeamothViewYRot(Entity entity, float partialTicks) {
        if (entity instanceof Player player && player.getVehicle() instanceof SeamothEntity seamoth) {
            return seamoth.getViewYRot(partialTicks);
        }

        return entity.getViewYRot(partialTicks);
    }

    @Redirect(
            method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F")
    )
    private float seamoth$useSeamothViewXRot(Entity entity, float partialTicks) {
        if (entity instanceof Player player && player.getVehicle() instanceof SeamothEntity seamoth) {
            return seamoth.getViewXRot(partialTicks);
        }

        return entity.getViewXRot(partialTicks);
    }

    @Redirect(
            method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    )
    private void seamoth$useSeamothViewPosition(Camera camera, double x, double y, double z, float partialTicks) {
        if (this.entity instanceof Player player && player.getVehicle() instanceof SeamothEntity seamoth) {
            Vec3 pos = seamoth.getInterpolatedPilotEyePosition(partialTicks);
            this.setPosition(pos.x, pos.y, pos.z);
            return;
        }

        this.setPosition(x, y, z);
    }
}