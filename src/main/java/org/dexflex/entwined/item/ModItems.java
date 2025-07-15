package org.dexflex.entwined.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.dexflex.entwined.Entwined;

public class ModItems {

    // Create the item instance
    public static final Item THORNLASH = new ThornlashItem(new FabricItemSettings().maxCount(1));

    // Register all mod items here
    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Entwined.MOD_ID, "thornlash"), THORNLASH);
        System.out.println("Registered Thornlash item");
    }
}
