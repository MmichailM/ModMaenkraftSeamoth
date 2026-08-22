package com.seamoth.client.mixin;

import com.seamoth.entity.SeamothEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void seamoth$hideHandsInSeamoth(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof SeamothEntity) {
            ci.cancel();
        }
    }
}
