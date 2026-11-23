package com.seamoth.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public abstract class EnergyItem extends Item {
    private static final int BAR_MAX_WIDTH = 13;

    private final int maxEnergy;

    protected EnergyItem(Properties properties, int maxEnergy) {
        super(properties.stacksTo(1).component(ModComponents.ENERGY, maxEnergy));
        this.maxEnergy = maxEnergy;
    }

    public int getMaxEnergy() {
        return this.maxEnergy;
    }

    public int getEnergy(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(ModComponents.ENERGY, this.maxEnergy), 0, this.maxEnergy);
    }

    public void setEnergy(ItemStack stack, int energy) {
        stack.set(ModComponents.ENERGY, Mth.clamp(energy, 0, this.maxEnergy));
    }

    public int getPercent(ItemStack stack) {
        return Math.round(100.0F * this.getEnergy(stack) / this.maxEnergy);
    }

    /** Списывает энергию. false — если стек не EnergyItem или энергии не хватило (тогда ничего не списывается). */
    public static boolean drain(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof EnergyItem item) || amount < 0) {
            return false;
        }

        int energy = item.getEnergy(stack);
        if (energy < amount) {
            return false;
        }

        item.setEnergy(stack, energy - amount);
        return true;
    }

    /** Добавляет энергию, возвращает сколько реально влезло. */
    public static int charge(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof EnergyItem item) || amount <= 0) {
            return 0;
        }

        int before = item.getEnergy(stack);
        item.setEnergy(stack, before + amount);
        return item.getEnergy(stack) - before;
    }

    public static boolean hasEnergy(ItemStack stack) {
        return stack.getItem() instanceof EnergyItem item && item.getEnergy(stack) > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int energy = this.getEnergy(stack);
        int width = Math.round((float) energy * BAR_MAX_WIDTH / this.maxEnergy);
        return energy > 0 ? Math.max(1, width) : 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.seamoth.energy", this.getPercent(stack))
                .withStyle(ChatFormatting.GRAY));
    }
}
