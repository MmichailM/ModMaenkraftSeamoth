package com.seamoth.network;

import com.seamoth.entity.SeamothEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SeamothSyncPacket(int entityId, double x, double y, double z, float yRot, float xRot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SeamothSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("seamoth", "sync_packet"));

    public static final StreamCodec<FriendlyByteBuf, SeamothSyncPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SeamothSyncPacket::entityId,
            ByteBufCodecs.DOUBLE, SeamothSyncPacket::x,
            ByteBufCodecs.DOUBLE, SeamothSyncPacket::y,
            ByteBufCodecs.DOUBLE, SeamothSyncPacket::z,
            ByteBufCodecs.FLOAT, SeamothSyncPacket::yRot,
            ByteBufCodecs.FLOAT, SeamothSyncPacket::xRot,
            SeamothSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void broadcast(SeamothEntity seamoth) {
        SeamothSyncPacket packet = new SeamothSyncPacket(
                seamoth.getId(), seamoth.getX(), seamoth.getY(), seamoth.getZ(), seamoth.getYRot(), seamoth.getXRot()
        );

        for (ServerPlayer tracking : PlayerLookup.tracking(seamoth)) {
            ServerPlayNetworking.send(tracking, packet);
        }
        // Игроку, который сам сидит за штурвалом, позиция тоже нужна (для сверки
        // с локальным предсказанием) — PlayerLookup.tracking() обычно включает и
        // его самого, но на случай если Мотылёк только что заспавнен и трекинг
        // ещё не подхватил пассажира, подстрахуемся явно.
        if (seamoth.getControllingPassenger() instanceof ServerPlayer pilot) {
            ServerPlayNetworking.send(pilot, packet);
        }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}