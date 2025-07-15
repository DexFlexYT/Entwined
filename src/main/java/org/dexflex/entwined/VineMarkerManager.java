package org.dexflex.entwined;

import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VineMarkerManager {

    private static VineMarkerManager instance;

    public static VineMarkerManager getInstance() {
        if (instance == null) {
            instance = new VineMarkerManager();
        }
        return instance;
    }

    private static class VineData {
        UUID ownerUUID;
        UUID targetUUID = null;
        int lifeTicks = 0;
        boolean dead = false;
    }

    private final Map<UUID, VineData> vines = new ConcurrentHashMap<>();

    public void addVine(MarkerEntity marker, UUID ownerUUID) {
        if (marker == null || ownerUUID == null) return;
        VineData data = new VineData();
        data.ownerUUID = ownerUUID;
        vines.put(marker.getUuid(), data);
        System.out.println("[VineMarkerManager] Added vine " + marker.getUuid());
    }

    public void tickVines(ServerWorld world) {
        System.out.println("[VineMarkerManager] TICK start, vines: " + vines.size());

        Iterator<Map.Entry<UUID, VineData>> iterator = vines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, VineData> entry = iterator.next();
            UUID uuid = entry.getKey();
            VineData data = entry.getValue();

            MarkerEntity marker = findMarkerByUUID(world, uuid);
            System.out.println("[VineMarkerManager] Processing vine " + uuid + ", marker=" + marker);

            if (marker == null || !marker.isAlive()) {
                System.out.println("[VineMarkerManager] Removing vine " + uuid + ": marker null or dead");
                iterator.remove();
                continue;
            }

            data.lifeTicks++;
            System.out.println("[VineMarkerManager] Vine " + uuid + " lifeTicks=" + data.lifeTicks + ", dead=" + data.dead);

            PlayerEntity target = null;
            if (data.targetUUID != null) {
                target = world.getPlayerByUuid(data.targetUUID);
            }

            if (!data.dead) {
                if (target == null || !target.isAlive() || target.squaredDistanceTo(marker) > 100) {
                    target = findNearestValidPlayer(world, marker, data.ownerUUID);
                    data.targetUUID = (target != null) ? target.getUuid() : null;
                }

                if (target != null) {
                    spawnVineBaseParticles(world, marker.getPos(), target.getEyePos());
                    pullPlayerTowardsMarker(marker, target);
                    if (target.getPos().distanceTo(marker.getPos()) <= 1.0) {
                        lockPlayerInPlace(target);
                    }
                } else {
                    // No target found: spawn particles at vine position anyway
                    spawnVineBaseParticles(world, marker.getPos(), marker.getPos());
                }

                if (data.lifeTicks >= 100) {
                    data.dead = true;
                    System.out.println("[VineMarkerManager] Vine " + uuid + " is now dead");
                }
            } else { // dead state
                if (target != null) {
                    spawnDeadVineParticles(world, marker.getPos(), target.getEyePos());
                    lockPlayerInPlace(target);
                } else {
                    // Spawn dead particles at vine position if no target
                    spawnDeadVineParticles(world, marker.getPos(), marker.getPos());
                }

                if (data.lifeTicks >= 300) {
                    System.out.println("[VineMarkerManager] Removing vine " + uuid + " after dead period");
                    marker.discard();
                    iterator.remove();
                }
            }
        }
        System.out.println("[VineMarkerManager] TICK end");
    }

    private MarkerEntity findMarkerByUUID(ServerWorld world, UUID uuid) {
        return world.getEntitiesByClass(MarkerEntity.class,
                        new Box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                        e -> e.getUuid().equals(uuid))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private PlayerEntity findNearestValidPlayer(ServerWorld world, MarkerEntity marker, UUID ownerUUID) {
        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class,
                new Box(marker.getX() - 10, marker.getY() - 10, marker.getZ() - 10,
                        marker.getX() + 10, marker.getY() + 10, marker.getZ() + 10),
                p -> p.isAlive() && !p.getUuid().equals(ownerUUID));

        if (players.isEmpty()) return null;
        players.sort(Comparator.comparingDouble(p -> p.squaredDistanceTo(marker)));
        return players.get(0);
    }

    private void spawnVineBaseParticles(ServerWorld world, Vec3d start, Vec3d end) {
        int steps = 20;
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3d pos = start.add(diff.multiply(i / (double) steps));
            world.spawnParticles(ModParticles.VINE_BASE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private void spawnDeadVineParticles(ServerWorld world, Vec3d start, Vec3d end) {
        int steps = 20;
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3d pos = start.add(diff.multiply(i / (double) steps));
            world.spawnParticles(ModParticles.DEAD_VINE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private void pullPlayerTowardsMarker(MarkerEntity marker, PlayerEntity target) {
        Vec3d direction = marker.getPos().subtract(target.getPos());
        double distance = target.getPos().distanceTo(marker.getPos());

        if (distance > 1.0) {
            double pullStrength = 0.15 * (distance - 1) / 9;
            Vec3d newVelocity = target.getVelocity().add(direction.normalize().multiply(pullStrength));
            target.setVelocity(newVelocity);
            target.velocityModified = true;
        }
    }

    private void lockPlayerInPlace(PlayerEntity target) {
        target.setVelocity(Vec3d.ZERO);
        target.fallDistance = 0F;
        target.velocityModified = true;
    }

    public void tryBreakVinesOnPlayerAttack(PlayerEntity player) {
        vines.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            VineData data = entry.getValue();

            if (data.dead && player.getUuid().equals(data.targetUUID)) {
                System.out.println("[VineMarkerManager] Vine " + uuid + " broken by player attack");
                return true;
            }
            return false;
        });
    }

    public static void spawnVineLeafParticleLine(ServerWorld world, Vec3d start, Vec3d end) {
        int steps = 50;
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3d pos = start.add(diff.multiply(i / (double) steps));
            world.spawnParticles(ModParticles.VINE_LEAF, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
}
