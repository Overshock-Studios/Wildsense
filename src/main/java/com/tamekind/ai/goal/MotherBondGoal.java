package com.tamekind.ai.goal;

import com.tamekind.ai.AiLod;
import com.tamekind.ai.AnimalMemoryStore;
import com.tamekind.ai.TamekindAnimalRules;
import com.tamekind.config.TamekindConfig;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

public final class MotherBondGoal extends Goal implements TamekindGoal {
    private final Animal adult;
    private Animal calf;
    private int nextTryTick;
    private int lookTicks;

    public MotherBondGoal(Animal adult) {
        this.adult = adult;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!TamekindConfig.enabled || adult.isBaby()) return false;
        if (TamekindAnimalRules.skipMovementGoals(adult)) return false;
        if (AiLod.forAnimal(adult) != AiLod.FULL) return false;
        if (adult.tickCount < nextTryTick) return false;
        nextTryTick = adult.tickCount + 200 + adult.getRandom().nextInt(200);
        if (AnimalMemoryStore.get(adult).dangerPos(adult.level().getGameTime()) != null) return false;
        if (adult.isInLove()) return false;
        AABB box = adult.getBoundingBox().inflate(10.0);
        calf = adult.level().getEntitiesOfClass(Animal.class, box,
                        o -> o != adult && o.isAlive() && o.isBaby() && o.getType() == adult.getType()).stream()
                .min(Comparator.comparingDouble(adult::distanceToSqr))
                .orElse(null);
        return calf != null && adult.distanceToSqr(calf) > 9.0;
    }

    @Override
    public boolean canContinueToUse() {
        return calf != null && calf.isAlive() && calf.isBaby()
                && (lookTicks > 0 || (!adult.getNavigation().isDone() && adult.distanceToSqr(calf) > 4.0));
    }

    @Override
    public void start() {
        lookTicks = 40 + adult.getRandom().nextInt(40);
        adult.getNavigation().moveTo(calf, 0.85);
    }

    @Override
    public void tick() {
        if (calf == null) return;
        adult.getLookControl().setLookAt(calf, 30.0F, 30.0F);
        if (adult.distanceToSqr(calf) <= 4.0) {
            adult.getNavigation().stop();
            lookTicks--;
        }
    }

    @Override
    public void stop() {
        calf = null;
        lookTicks = 0;
    }
}
