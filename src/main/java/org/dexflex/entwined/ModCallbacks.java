package org.dexflex.entwined;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.util.ActionResult;

public class ModCallbacks {
    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            VineMarkerManager.getInstance().tryBreakVinesOnPlayerAttack(player);
            return ActionResult.PASS;
        });
    }
}
