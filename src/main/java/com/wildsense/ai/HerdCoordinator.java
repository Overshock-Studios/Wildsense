package com.wildsense.ai;

import com.wildsense.compat.WildsenseTags;
import com.wildsense.config.WildsenseConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public final class HerdCoordinator {
    private HerdCoordinator() {
    }

    public static boolean isHerdable(Animal animal) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(animal.getType()).is(WildsenseTags.HERDABLE);
    }

    public static List<Animal> nearbyHerd(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level) || !isHerdable(animal)) return List.of();
        double radius = WildsenseConfig.herdSearchRadius;
        AABB box = animal.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(Animal.class, box, other ->
                other.isAlive() && other != animal && sameHerd(animal, other));
    }

    public static Animal leaderFor(Animal animal) {
        return nearbyHerd(animal).stream()
                .filter(other -> !other.isBaby())
                .min(Comparator.comparingInt(a -> a.getUUID().hashCode()))
                .orElse(null);
    }

    public static int herdSize(Animal animal) {
        return nearbyHerd(animal).size() + 1;
    }

    private static boolean sameHerd(Animal first, Animal second) {
        if (!isHerdable(second)) return false;
        return first.getType() == second.getType();
    }
}
