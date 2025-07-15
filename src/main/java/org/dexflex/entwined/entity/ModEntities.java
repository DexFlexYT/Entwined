package org.dexflex.entwined.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.dexflex.entwined.Entwined;

public class ModEntities {
    public static final EntityType<VineSnareEntity> VINE_SNARE_ENTITY = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(Entwined.MOD_ID, "vine_snare"),
            EntityType.Builder.<VineSnareEntity>create(VineSnareEntity::new, SpawnGroup.MISC)
                    .setDimensions(0f,0f) // tiny, minimal bounding box
                    .disableSummon()
                    .build("vine_snare")
    );

    public static void register() {
        // Call in main mod initializer
    }
}
