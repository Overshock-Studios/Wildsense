package com.tamekind.ai.goal;

import com.tamekind.ai.AiLod;
import com.tamekind.ai.AnimalMemoryStore;
import com.tamekind.ai.TamekindAnimalRules;
import com.tamekind.compat.TamekindTags;
import com.tamekind.config.TamekindConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public final class WallowGoal extends Goal implements TamekindGoal {
    private final Animal animal;
    private int nextTryTick;
    private int wallowTicks;

    public WallowGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!TamekindConfig.enabled || animal.getType() != EntityType.PIG) return false;
        if (TamekindAnimalRules.skipMovementGoals(animal)) return false;
        if (AiLod.forAnimal(animal) != AiLod.FULL) return false;
        if (animal.tickCount < nextTryTick) return false;
        nextTryTick = animal.tickCount + 400 + animal.getRandom().nextInt(600);
        if (AnimalMemoryStore.get(animal).dangerPos(animal.level().getGameTime()) != null) return false;
        return isOnWallowableGround();
    }

    @Override
    public boolean canContinueToUse() {
        return wallowTicks > 0 && isOnWallowableGround();
    }

    @Override
    public void start() {
        wallowTicks = 60 + animal.getRandom().nextInt(40);
        animal.getNavigation().stop();
    }

    @Override
    public void tick() {
        wallowTicks--;
        animal.getNavigation().stop();
        animal.setDeltaMovement(animal.getDeltaMovement().multiply(0.4, 1.0, 0.4));
        if (animal.level() instanceof net.minecraft.server.level.ServerLevel server && animal.tickCount % 6 == 0) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,
                    animal.getX(), animal.getY() + 0.2, animal.getZ(),
                    4, 0.3, 0.05, 0.3, 0.02);
        }
    }

    @Override
    public void stop() {
        wallowTicks = 0;
    }

    private boolean isOnWallowableGround() {
        Level level = animal.level();
        BlockPos below = animal.blockPosition().below();
        var state = level.getBlockState(below);
        if (state.is(Blocks.MUD)) return true;
        for (var dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(animal.blockPosition().relative(dir)).is(TamekindTags.WATER_BLOCKS)) return true;
        }
        return false;
    }
}
