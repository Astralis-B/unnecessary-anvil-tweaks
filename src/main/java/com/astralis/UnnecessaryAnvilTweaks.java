package com.astralis;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnnecessaryAnvilTweaks implements ModInitializer {
	public static final String MOD_ID = "unnecessary-anvil-tweaks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Cargar configuración al iniciar
		ModConfig.load();
		LOGGER.info("(Un)Necessary Anvil Tweaks loaded successfully!");
	}
}