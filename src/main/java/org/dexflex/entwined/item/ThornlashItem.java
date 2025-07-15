package org.dexflex.entwined.item;

import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import org.dexflex.entwined.ModParticles;
import org.dexflex.entwined.VineMarkerManager;
public class ThornlashItem extends Item {

    private static final int MAX_DISTANCE = 128;
    private static final int COOLDOWN_TICKS = 0;

    public ThornlashItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            // Client: do nothing here (particles handled by server-to-client packets or world)
            return TypedActionResult.success(stack);
        }

        // Server side:

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        // Raycast from eyes forward, 128 blocks max distance
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookVec.multiply(MAX_DISTANCE));

        BlockHitResult raycastResult = world.raycast(
                new RaycastContext(eyePos, endPos, ShapeType.COLLIDER, FluidHandling.NONE, user)
        );

        if (raycastResult.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos pos = raycastResult.getBlockPos().up();
            MarkerEntity marker = new MarkerEntity(net.minecraft.entity.EntityType.MARKER, world);

            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            marker.updatePosition(x, y, z);

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnEntity(marker);

                // Register the marker to the manager with the owner UUID and initial data
                VineMarkerManager.getInstance().addVine(marker, user.getUuid());

                // Consume item and cooldown
                if (!user.isCreative()) {
                    stack.decrement(1);
                }
                user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

                // Spawn vine leaf particles along ray
                VineMarkerManager.spawnVineLeafParticleLine(serverWorld, eyePos, new Vec3d(x, y + 0.5, z));

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.fail(stack);
    }

    // Helper to spawn vine leaf particle line between two points
    private void spawnParticleLine(ServerWorld world, Vec3d start, Vec3d end) {
        final int STEPS = 50;
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= STEPS; i++) {
            double t = (double) i / STEPS;
            Vec3d pos = start.add(diff.multiply(t));
            world.spawnParticles(ModParticles.VINE_LEAF, pos.x, pos.y, pos.z,
                    1,
                    0, 0, 0, 0.0); // stationary particle for the effect
        }
    }
}
