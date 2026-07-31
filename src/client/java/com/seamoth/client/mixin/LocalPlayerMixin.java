package com.seamoth.client.mixin;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void seamoth$updatePitch(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (player.getVehicle() instanceof SeamothEntity seamoth) {
            // Рассчитываем, насколько Мотылек изменил наклон за этот тик
            float pitchDelta = seamoth.getXRot() - seamoth.xRotO;

            // Прибавляем наклон Мотылька к текущему наклону головы игрока
            // Благодаря этому мышь продолжает работать, но при наклоне корабля камера наклоняется!
            if (pitchDelta != 0.0F) {
                player.setXRot(player.getXRot() + pitchDelta);
                player.xRotO += pitchDelta;
            }
        }
    }
}