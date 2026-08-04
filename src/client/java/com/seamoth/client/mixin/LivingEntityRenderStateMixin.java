package com.seamoth.client.mixin;

import com.seamoth.client.duck.SeamothTiltAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements SeamothTiltAccessor {

    @Unique
    private boolean seamoth$shouldTilt = false;

    @Unique
    private float seamoth$pitch = 0.0F;

    @Unique
    private boolean seamoth$shouldHide = false;

    @Override
    public boolean seamoth$isShouldTilt() {
        return this.seamoth$shouldTilt;
    }

    @Override
    public void seamoth$setShouldTilt(boolean value) {
        this.seamoth$shouldTilt = value;
    }

    @Override
    public float seamoth$getPitch() {
        return this.seamoth$pitch;
    }

    @Override
    public void seamoth$setPitch(float value) {
        this.seamoth$pitch = value;
    }

    @Override
    public boolean seamoth$isShouldHide() {
        return this.seamoth$shouldHide;
    }

    @Override
    public void seamoth$setShouldHide(boolean value) {
        this.seamoth$shouldHide = value;
    }
}