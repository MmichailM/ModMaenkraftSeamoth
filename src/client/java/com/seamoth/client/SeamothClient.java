package com.seamoth.client;

import com.seamoth.Seamoth; // Импортируешь твой главный класс Seamoth
import com.seamoth.client.render.SeamothModel;
import com.seamoth.client.render.SeamothEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;


public class SeamothClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(SeamothModel.LAYER_LOCATION, SeamothModel::createBodyLayer);
		EntityRendererRegistry.register(Seamoth.SEAMOTH, SeamothEntityRenderer::new);

		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_UP);
		KeyMappingHelper.registerKeyMapping(SeamothKeybindings.KEY_DOWN);

		ClientTickEvents.END_CLIENT_TICK.register(SeamothClientHandler::onClientTick);
	}
}