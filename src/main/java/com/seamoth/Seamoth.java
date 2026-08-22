package com.seamoth;

import com.seamoth.entity.SeamothEntity;
import com.seamoth.event.SeamothAggroHandler;
import com.seamoth.event.SeamothInteractionHandler;
import com.seamoth.item.ModItems;
import com.seamoth.item.SeamothContainerItem;
import com.seamoth.menu.ModMenus;
import com.seamoth.network.SeamothInputPacket;
import com.seamoth.network.SeamothOpenInventoryPacket;
import com.seamoth.network.SeamothSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seamoth implements ModInitializer {
	public static final String MOD_ID = "seamoth";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier SEAMOTH_ID = Identifier.fromNamespaceAndPath(MOD_ID, "seamoth");
	public static final ResourceKey<EntityType<?>> SEAMOTH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, SEAMOTH_ID);

	public static final EntityType<SeamothEntity> SEAMOTH = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "seamoth"),
			EntityType.Builder.of(SeamothEntity::new, MobCategory.MISC)
					.sized(2.2f, 1.5f)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Seamoth.MOD_ID, "seamoth")))
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		SeamothInputPacket.register();
		SeamothSyncPacket.register();
		FabricDefaultAttributeRegistry.register(SEAMOTH, SeamothEntity.createSeamothAttributes().build());
		SeamothAggroHandler.register();
		ModItems.registerModItems();
		SeamothInteractionHandler.register();
		ModMenus.register();
		SeamothOpenInventoryPacket.register();
		LOGGER.info("Seamoth mod is initialised!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
