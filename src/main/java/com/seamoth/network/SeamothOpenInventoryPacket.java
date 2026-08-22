package com.seamoth.network;

import com.seamoth.entity.SeamothEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SeamothOpenInventoryPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SeamothOpenInventoryPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("seamoth", "open_inventory_packet"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, SeamothOpenInventoryPacket> CODEC =
            StreamCodec.unit(new SeamothOpenInventoryPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send() {
        ClientPlayNetworking.send(new SeamothOpenInventoryPacket());
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().getVehicle() instanceof SeamothEntity seamoth) {
                    seamoth.openCustomInventoryScreen(context.player());
                }
            });
        });
    }
}