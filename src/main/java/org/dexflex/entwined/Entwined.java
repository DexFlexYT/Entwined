package org.dexflex.entwined;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entwined implements ModInitializer {
	public static final String MOD_ID = "entwined";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "vine_base"), ModParticles.VINE_BASE);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "dead_vine"), ModParticles.DEAD_VINE);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "vine_leaf"), ModParticles.VINE_LEAF);
	}
}