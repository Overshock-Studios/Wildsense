package com.wildsense.ai;

import com.wildsense.config.WildsenseConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PassiveEventDirector {
    private PassiveEventDirector() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(PassiveEventDirector::afterDamage);
    }

    private static void afterDamage(LivingEntity entity, DamageSource source,
                                    float baseDamageTaken, float damageTaken, boolean blocked) {
        if (!WildsenseConfig.enabled || damageTaken <= 0.0f || !(entity instanceof Animal animal)) return;
        Vec3 danger = dangerPosition(animal, source);
        long until = animal.level().getGameTime() + WildsenseConfig.memoryTicks;
        AnimalMemoryStore.get(animal).rememberDanger(danger, until);
        shareDanger(animal, danger, until);
    }

    private static Vec3 dangerPosition(Animal animal, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) attacker = source.getDirectEntity();
        if (attacker != null) return attacker.position();
        Vec3 sourcePos = source.getSourcePosition();
        return sourcePos != null ? sourcePos : animal.position();
    }

    private static void shareDanger(Animal victim, Vec3 danger, long until) {
        if (!WildsenseConfig.herdEnabled || !(victim.level() instanceof ServerLevel level)) return;
        double radius = WildsenseConfig.herdSearchRadius;
        AABB box = victim.getBoundingBox().inflate(radius);
        for (Animal herdMate : level.getEntitiesOfClass(Animal.class, box, other ->
                other.isAlive() && other != victim && other.getType() == victim.getType())) {
            AnimalMemoryStore.get(herdMate).rememberDanger(danger, until);
        }
    }
}
