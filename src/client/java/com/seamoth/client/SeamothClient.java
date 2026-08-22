package com.seamoth.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.seamoth.Seamoth; // Импортируешь твой главный класс Seamoth
import com.seamoth.client.render.SeamothModel;
import com.seamoth.client.render.SeamothEntityRenderer;
import com.seamoth.client.screen.SeamothScreen;
import com.seamoth.entity.SeamothEntity;
import com.seamoth.menu.ModMenus;
import com.seamoth.network.SeamothSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public class SeamothClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Раньше здесь была ручная перезапись позиции камеры через reflection
		// (с фиксированным offsetY, без учёта тангажа мотылька) — она конфликтовала
		// с позицией пассажира и была источником бага с наклоном камеры.
		// Теперь позиция игрока в SeamothEntity.positionRider() сама корректно
		// поворачивается вместе с моделью (и по курсу, и по тангажу), поэтому
		// обычная ванильная камера от игрока работает правильно без хаков.

		ModelLayerRegistry.registerModelLayer(SeamothModel.LAYER_LOCATION, SeamothModel::createBodyLayer);
		EntityRendererRegistry.register(Seamoth.SEAMOTH, SeamothEntityRenderer::new);

		MenuScreens.register(ModMenus.SEAMOTH_MENU, SeamothScreen::new);

		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_UP);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_DOWN);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_FORWARD);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_BACKWARD);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_LEFT);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_RIGHT);

		ClientTickEvents.START_CLIENT_TICK.register(SeamothClientHandler::onClientTickEarly);
		ClientTickEvents.END_CLIENT_TICK.register(SeamothClientHandler::onClientTick);

		ClientPlayNetworking.registerGlobalReceiver(SeamothSyncPacket.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				if (context.client().level == null) {
					return;
				}

				Entity entity = context.client().level.getEntity(payload.entityId());
				if (!(entity instanceof SeamothEntity seamoth)) {
					return;
				}

				boolean isPilotClient = seamoth.getControllingPassenger() instanceof Player player && player.isLocalPlayer();

				if (isPilotClient) {
					double dx = seamoth.getX() - payload.x();
					double dy = seamoth.getY() - payload.y();
					double dz = seamoth.getZ() - payload.z();
					double distSqr = dx * dx + dy * dy + dz * dz;

					if (distSqr > 0.75D * 0.75D) {
						seamoth.absSnapTo(payload.x(), payload.y(), payload.z(), payload.yRot(), payload.xRot());
					}
				} else {
					seamoth.absSnapTo(payload.x(), payload.y(), payload.z(), payload.yRot(), payload.xRot());
				}
			});
		});
	}
}