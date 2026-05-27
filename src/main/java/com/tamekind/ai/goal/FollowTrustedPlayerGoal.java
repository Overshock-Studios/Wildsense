package com.tamekind.ai.goal;

import com.tamekind.ai.AiLod;
import com.tamekind.ai.AnimalMemoryStore;
import com.tamekind.ai.TamekindAnimalRules;
import com.tamekind.config.TamekindConfig;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public final class FollowTrustedPlayerGoal extends Goal implements TamekindGoal {
    private final Animal animal;
    private Player target;
    private int nextTryTick;

    public FollowTrustedPlayerGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!TamekindConfig.enabled || !TamekindConfig.followTrustedEnabled || !TamekindConfig.trustEnabled) return false;
        if (TamekindAnimalRules.skipMovementGoals(animal)) return false;
        if (AiLod.forAnimal(animal) == AiLod.SLEEP) return false;
        if (animal.tickCount < nextTryTick) return false;
        nextTryTick = animal.tickCount + 40;
        if (AnimalMemoryStore.get(animal).dangerPos(animal.level().getGameTime()) != null) return false;
        long now = animal.level().getGameTime();
        double radius = TamekindConfig.followTrustedRadius;
        double minTrust = TamekindConfig.followTrustedMinTrust;
        target = null;
        double bestTrust = minTrust;
        for (Player p : animal.level().players()) {
            if (p.distanceToSqr(animal) > radius * radius) continue;
            double t = AnimalMemoryStore.get(animal).trustScore(p.getUUID(), now);
            if (t > bestTrust) {
                bestTrust = t;
                target = p;
            }
        }
        return target != null && animal.distanceToSqr(target) > 64.0;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !animal.getNavigation().isDone()
                && animal.distanceToSqr(target) > 25.0;
    }

    @Override
    public void start() {
        animal.getNavigation().moveTo(target, TamekindConfig.followTrustedSpeed);
    }

    @Override
    public void stop() {
        target = null;
    }
}
