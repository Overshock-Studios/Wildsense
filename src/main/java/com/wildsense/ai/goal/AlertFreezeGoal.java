package com.wildsense.ai.goal;

import com.wildsense.ai.AiLod;
import com.wildsense.ai.ThreatScanner;
import com.wildsense.ai.WildsenseAnimalRules;
import com.wildsense.config.WildsenseConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public final class AlertFreezeGoal extends Goal implements WildsenseGoal {
    private final Animal animal;
    private Entity threat;
    private int alertTicks;
    private int initialTicks;

    public AlertFreezeGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!WildsenseConfig.enabled || !WildsenseConfig.alertEnabled || AiLod.forAnimal(animal) != AiLod.FULL) return false;
        if (WildsenseAnimalRules.skipMovementGoals(animal)) return false;
        threat = ThreatScanner.nearestThreat(animal, WildsenseConfig.alertRadius);
        if (threat == null) return false;
        return animal.distanceToSqr(threat) > WildsenseConfig.panicRadius * WildsenseConfig.panicRadius;
    }

    @Override
    public boolean canContinueToUse() {
        return threat != null && threat.isAlive() && alertTicks > 0
                && animal.distanceToSqr(threat) > WildsenseConfig.panicRadius * WildsenseConfig.panicRadius;
    }

    @Override
    public void start() {
        int rnd = Math.max(1, WildsenseConfig.alertFreezeRandomTicks);
        alertTicks = WildsenseConfig.alertFreezeMinTicks + animal.getRandom().nextInt(rnd);
        initialTicks = alertTicks;
        animal.getNavigation().stop();
    }

    @Override
    public void tick() {
        alertTicks--;
        if (threat != null) {
            animal.getLookControl().setLookAt(threat, 30.0F, 30.0F);
        }
        if (threat != null && alertTicks < initialTicks / 2 && animal.getNavigation().isDone()) {
            net.minecraft.world.phys.Vec3 away = animal.position().subtract(threat.position());
            if (away.lengthSqr() > 0.001) {
                away = away.normalize().scale(6.0);
                animal.getNavigation().moveTo(
                        animal.getX() + away.x,
                        animal.getY(),
                        animal.getZ() + away.z,
                        WildsenseConfig.alertDriftSpeed);
            }
        }
    }

    @Override
    public void stop() {
        threat = null;
        alertTicks = 0;
        initialTicks = 0;
    }
}
