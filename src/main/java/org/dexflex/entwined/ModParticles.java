package org.dexflex.entwined;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;

public class ModParticles {
    public static final DefaultParticleType VINE_BASE = FabricParticleTypes.simple();
    public static final DefaultParticleType DEAD_VINE = FabricParticleTypes.simple();
    public static final DefaultParticleType VINE_LEAF = FabricParticleTypes.simple();

    public static void register() {
        // No registration here; registration done by field initialization and registration happening in ModInitializer
        // This method can be used for future logic if needed.
    }
}
