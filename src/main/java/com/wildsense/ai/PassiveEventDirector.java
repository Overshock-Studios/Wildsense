package com.wildsense.ai;

import com.wildsense.config.WildsenseConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
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
        DangerBroadcaster.rememberAndSpread(animal, danger);
    }

    private static Vec3 dangerPosition(Animal animal, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) attacker = source.getDirectEntity();
        if (attacker != null) return attacker.position();
        Vec3 sourcePos = source.getSourcePosition();
        return sourcePos != null ? sourcePos : animal.position();
    }

}
