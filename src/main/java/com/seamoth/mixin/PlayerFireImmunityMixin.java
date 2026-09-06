package com.seamoth.mixin;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerFireImmunityMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void seamoth$fireImmuneInsideSeamoth(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;

        if (self.getVehicle() instanceof SeamothEntity && source.is(DamageTypeTags.IS_FIRE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
    private void seamoth$noIgniteInsideSeamoth(int remainingTicks, CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (self.getVehicle() instanceof SeamothEntity) {
            ci.cancel();
        }
    }
}