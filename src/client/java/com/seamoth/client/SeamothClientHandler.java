package com.seamoth.client;

import com.seamoth.Seamoth;
import com.seamoth.entity.SeamothEntity;
import com.seamoth.network.SeamothInputPacket;
import net.minecraft.client.Minecraft;

public class SeamothClientHandler {
    private static boolean lastUp = false;
    private static boolean lastDown = false;

    public static void onClientTick(Minecraft client) {
        if (client.player != null && client.player.getVehicle() instanceof SeamothEntity) {
            boolean isUpPressed = SeamothKeybindings.KEY_UP.isDown();
            boolean isDownPressed = SeamothKeybindings.KEY_DOWN.isDown();

            if (isUpPressed != lastUp || isDownPressed != lastDown) {
                SeamothInputPacket.send(isUpPressed, isDownPressed);
                lastUp = isUpPressed;
                lastDown = isDownPressed;
            }

            else if (lastDown || lastUp) {
                SeamothInputPacket.send(false, false);
                lastUp = false;
                lastDown = false;
            }
        }
    }
}
