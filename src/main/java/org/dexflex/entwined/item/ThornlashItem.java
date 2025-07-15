package org.dexflex.entwined.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.dexflex.entwined.entity.ModEntities;
import org.dexflex.entwined.entity.VineSnareEntity;

public class ThornlashItem extends Item {
    public ThornlashItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();

        // Raycast from player eyes forward to max distance (say 10 blocks)
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        double maxDistance = 10.0;
        Vec3d endPos = eyePos.add(lookVec.multiply(maxDistance));

        BlockHitResult hit = world.raycast(
                new RaycastContext(eyePos, endPos, RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, player)
        );

        if (hit.getType() == HitResult.Type.BLOCK && world.getBlockState(hit.getBlockPos()).isSolidBlock(world, hit.getBlockPos())) {
            if (world instanceof ServerWorld serverWorld) {
                VineSnareEntity vine = new VineSnareEntity(ModEntities.VINE_SNARE_ENTITY, world);
                BlockPos targetPos = hit.getBlockPos().up(); // Spawn at block top surface (or tweak)
                vine.setPosition(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                vine.setTargetPlayer(player); // For now target closest player or user himself; later find nearest enemy
                serverWorld.spawnEntity(vine);

                // Optionally consume item durability or apply cooldown here...

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.FAIL;
    }
}
