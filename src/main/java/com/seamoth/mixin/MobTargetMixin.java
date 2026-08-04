package com.seamoth.mixin;

import com.seamoth.entity.SeamothEntity;
import com.seamoth.event.SeamothAggroHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Перехватываем сам момент назначения цели мобу. Это надёжнее, чем чистить
 * цель постфактум в END_LEVEL_TICK: там моб уже успевает потратить один тик
 * на разворот/шаг в сторону игрока, что и выглядит как "агрится". Здесь же
 * игрок-пилот Мотылька в принципе не может стать чьей-то целью.
 */
@Mixin(Mob.class)
public abstract class MobTargetMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void seamoth$redirectOrBlockPlayerTarget(LivingEntity target, CallbackInfo ci) {
        if (!(target instanceof Player player)) {
            return;
        }

        if (!(player.getVehicle() instanceof SeamothEntity seamoth)) {
            return;
        }

        Mob self = (Mob) (Object) this;

        // Отменяем оригинальный вызов setTarget(player) в любом случае.
        ci.cancel();

        if (SeamothAggroHandler.shouldAttackSeamoth(self)) {
            // Морские мобы всё же должны атаковать — но сам Мотылёк.
            self.setTarget(seamoth);
        }
        // Остальные мобы просто не получают никакой цели.
    }
}