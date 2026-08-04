package com.seamoth.event;

import com.seamoth.entity.SeamothEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.player.Player;

public class SeamothAggroHandler {
    public static void register() {
        // Каждый тик проходим по всем мобам, чья цель — игрок за штурвалом
        // Мотылька. "Морские" мобы перенаправляются на сам Мотылёк, все
        // остальные просто теряют игрока как цель (они физически не должны
        // до него докапываться, пока он внутри субмарины).
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Mob mob)) {
                    continue;
                }

                if (!(mob.getTarget() instanceof Player player)) {
                    continue;
                }

                if (!(player.getVehicle() instanceof SeamothEntity seamoth)) {
                    continue;
                }

                if (shouldAttackSeamoth(mob)) {
                    mob.setTarget(seamoth);
                } else {
                    mob.setTarget(null);
                }
            }
        });

        // Подстраховка: END_LEVEL_TICK срабатывает уже после того, как мобы
        // в этот тик походили и могли ударить, поэтому цель снимается только
        // "постфактум". Чтобы игрок гарантированно не получал урон, пока
        // сидит в Мотыльке, отдельно отменяем сам урон от любых мобов.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, source, amount) -> {
            if (livingEntity instanceof Player player
                    && player.getVehicle() instanceof SeamothEntity
                    && source.getEntity() instanceof Mob) {
                return false;
            }
            return true;
        });
    }

    public static boolean shouldAttackSeamoth(Mob mob) {
        if (mob instanceof Drowned || mob instanceof Guardian || mob instanceof WaterAnimal ) {
            return true;
        }

        if (mob instanceof Enemy && mob.getMaxHealth() >= 150.0F) {
            return true;
        }

        return false;
    }
}