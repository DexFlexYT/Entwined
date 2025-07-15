package org.dexflex.entwined.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class VineSnareEntity extends Entity {

    private boolean deadVines = false;   // "dead" tag for vine state
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100; // 5 seconds

    private Vec3d targetPlayerEyePos = null;

    public VineSnareEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.noClip = true; // no collision
    }

    public void setTargetPlayer(PlayerEntity player) {
        if (player != null) {
            this.targetPlayerEyePos = player.getEyePos();
        }
    }
    @Override
    public boolean isInvisible() {
        return true;
    }


    @Override
    protected void initDataTracker() {
        // no data tracker for now
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.deadVines = nbt.getBoolean("DeadVines");
        this.lifeTicks = nbt.getInt("LifeTicks");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("DeadVines", deadVines);
        nbt.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) {
            // Client-side vine particle rendering happens here (separate system/listener or via packet)
            // We will trigger particle spawning from client or synced via network packet from server
            return;
        }

        lifeTicks++;

        if (lifeTicks >= MAX_LIFE_TICKS) {
            deadVines = true;
        }

        if (deadVines) {
            // vines are dead -- spawn stationary dead vine particles (long-lived)
        } else {
            // vines alive -- spawn 1-tick dead vine particles along path from this to player
        }

        // Update entity logic if needed, maybe remove entity after some grace period
    }

    @Override
    public boolean collides() {
        return false;  // No hitbox
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return EntitySpawnPacket.create(this);
    }

}
