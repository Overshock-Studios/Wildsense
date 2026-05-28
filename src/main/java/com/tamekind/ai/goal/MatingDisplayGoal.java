package com.tamekind.ai.goal;

import com.tamekind.ai.AiLod;
import com.tamekind.ai.AnimalMemoryStore;
import com.tamekind.ai.TamekindAnimalRules;
import com.tamekind.compat.TamekindTags;
import com.tamekind.config.TamekindConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

public final class MatingDisplayGoal extends Goal implements TamekindGoal {
    private final Animal animal;
    private Animal rival;
    private int nextTryTick;
    private int displayTicks;

    public MatingDisplayGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!TamekindConfig.enabled || animal.isBaby() || animal.getAge() != 0) return false;
        if (TamekindAnimalRules.skipMovementGoals(animal)) return false;
        if (AiLod.forAnimal(animal) != AiLod.FULL) return false;
        if (animal.tickCount < nextTryTick) return false;
        nextTryTick = animal.tickCount + 300 + animal.getRandom().nextInt(300);
        if (!BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(animal.getType()).is(TamekindTags.MATING_DISPLAYS)) return false;
        if (AnimalMemoryStore.get(animal).dangerPos(animal.level().getGameTime()) != null) return false;
        AABB box = animal.getBoundingBox().inflate(4.0);
        rival = animal.level().getEntitiesOfClass(Animal.class, box,
                        o -> o != animal && o.isAlive() && !o.isBaby() && o.getAge() == 0 && o.getType() == animal.getType()).stream()
                .min(Comparator.comparingDouble(animal::distanceToSqr))
                .orElse(null);
        return rival != null;
    }

    @Override
    public boolean canContinueToUse() {
        return rival != null && rival.isAlive() && displayTicks > 0;
    }

    @Override
    public void start() {
        displayTicks = 30 + animal.getRandom().nextInt(20);
        animal.getNavigation().moveTo(rival, 1.1);
    }

    @Override
    public void tick() {
        if (rival == null) return;
        animal.getLookControl().setLookAt(rival, 30.0F, 30.0F);
        displayTicks--;
        if (displayTicks % 10 == 0) {
            animal.getNavigation().moveTo(rival, 1.1);
        }
    }

    @Override
    public void stop() {
        rival = null;
        displayTicks = 0;
    }
}
