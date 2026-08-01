package com.seamoth.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.seamoth.Seamoth; // Импортируешь твой главный класс Seamoth
import com.seamoth.client.render.SeamothModel;
import com.seamoth.client.render.SeamothEntityRenderer;
import com.seamoth.entity.SeamothEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;


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

		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_UP);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_DOWN);

		ClientTickEvents.END_CLIENT_TICK.register(SeamothClientHandler::onClientTick);
	}
}