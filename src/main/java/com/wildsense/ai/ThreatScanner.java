package com.wildsense.ai;

import com.wildsense.compat.WildsenseTags;
import com.wildsense.config.WildsenseConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class ThreatScanner {
    private ThreatScanner() {
    }

    public static Entity nearestThreat(Animal animal, double radius) {
        if (!(animal.level() instanceof ServerLevel level)) return null;
        AABB box = animal.getBoundingBox().inflate(radius);
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(animal, box, entity -> isThreat(animal, entity))) {
            double dist = entity.distanceToSqr(animal);
            if (dist < bestDist) {
                bestDist = dist;
                best = entity;
            }
        }
        return best;
    }

    private static boolean isThreat(Animal animal, Entity entity) {
        if (!entity.isAlive()) return false;
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(WildsenseTags.PREDATORS)) return true;
        if (entity instanceof Player player) {
            long gameTime = animal.level().getGameTime();
            if (WildsenseConfig.trustEnabled && AnimalMemoryStore.get(animal).trusts(player.getUUID(), gameTime)) {
                return false;
            }
            return player.isSprinting() && player.distanceToSqr(animal) < 36.0;
        }
        return false;
    }
}
