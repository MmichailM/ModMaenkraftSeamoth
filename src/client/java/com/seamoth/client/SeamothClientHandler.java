package com.seamoth.client;

import com.seamoth.entity.SeamothEntity;
import com.seamoth.network.SeamothInputPacket;
import net.minecraft.client.Minecraft;

public class SeamothClientHandler {
    private static boolean lastUp = false;
    private static boolean lastDown = false;
    private static boolean lastForw = false;
    private static boolean lastBack = false;
    private static boolean lastLeft = false;
    private static boolean lastRight = false;

    public static void onClientTick(Minecraft client) {
        if (client.player == null || !(client.player.getVehicle() instanceof SeamothEntity seamoth)) {
            if (lastUp || lastDown || lastForw || lastBack || lastLeft || lastRight) {
                SeamothInputPacket.send(false, false, false, false, false, false);
                lastUp = lastDown = lastForw = lastBack = lastLeft = lastRight = false;
            }
            return;
        }

        boolean isUpPressed = SeamothKeybindings.KEY_UP.isDown();
        boolean isDownPressed = SeamothKeybindings.KEY_DOWN.isDown();
        boolean isForwPressed = SeamothKeybindings.KEY_FORWARD.isDown();
        boolean isBackPressed = SeamothKeybindings.KEY_BACKWARD.isDown();
        boolean isLeftPressed = SeamothKeybindings.KEY_LEFT.isDown();
        boolean isRightPressed = SeamothKeybindings.KEY_RIGHT.isDown();

        // Применяем ввод сразу к локальной (клиентской) копии сущности — это даёт
        // мгновенный отклик и плавное предсказание движения для самого пилота.
        // Теперь это безопасно: SeamothEntity.tick() сам явно вызывает travel()
        // каждый тик на ОБЕИХ сторонах, так что сервер больше не "замирает" —
        // он честно считает ту же физику по данным из пакета ниже.
        seamoth.setInputs(isUpPressed, isDownPressed, isForwPressed, isBackPressed, isLeftPressed, isRightPressed);

        // Пакет серверу шлём только по факту изменения — сервер помнит
        // последнее полученное состояние между пакетами, спамить каждый тик не нужно.
        if (isUpPressed != lastUp || isDownPressed != lastDown || isForwPressed != lastForw
                || isBackPressed != lastBack || isLeftPressed != lastLeft || isRightPressed != lastRight) {

            SeamothInputPacket.send(isUpPressed, isDownPressed, isForwPressed, isBackPressed, isLeftPressed, isRightPressed);

            lastUp = isUpPressed;
            lastDown = isDownPressed;
            lastForw = isForwPressed;
            lastBack = isBackPressed;
            lastLeft = isLeftPressed;
            lastRight = isRightPressed;
        }
    }
}