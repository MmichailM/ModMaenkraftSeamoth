package com.seamoth.network;

import com.seamoth.entity.SeamothEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SeamothInputPacket(boolean up, boolean down) implements CustomPacketPayload {

    // ID пакета нового типа
    public static final CustomPacketPayload.Type<SeamothInputPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("seamoth", "input_packet"));

    // Кодек для автоматической записи/чтения boolean
    public static final StreamCodec<FriendlyByteBuf, SeamothInputPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SeamothInputPacket::up,
            ByteBufCodecs.BOOL, SeamothInputPacket::down,
            SeamothInputPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Тот самый метод, который вы вызываете для отправки
    public static void send(boolean up, boolean down) {
        ClientPlayNetworking.send(new SeamothInputPacket(up, down));
    }
    public static void register() {
        // 1. Регистрируем тип пакета C2S (Client to Server) и его кодек
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        // 2. Слушаем пакет на сервере
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().getVehicle() instanceof SeamothEntity seamoth) {
                    seamoth.setInputs(payload.up(), payload.down());
                }
            });
        });
    }

}
