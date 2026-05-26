package com.wildsense.ai.goal;

import com.wildsense.ai.AiLod;
import com.wildsense.ai.AnimalMemory;
import com.wildsense.ai.AnimalMemoryStore;
import com.wildsense.ai.HerdCoordinator;
import com.wildsense.ai.ThreatScanner;
import com.wildsense.config.WildsenseConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public final class WildPanicGoal extends Goal implements WildsenseGoal {
    private final Animal animal;
    private Vec3 danger;

    public WildPanicGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!WildsenseConfig.enabled || !WildsenseConfig.panicEnabled || AiLod.forAnimal(animal) != AiLod.FULL) return false;
        AnimalMemory memory = AnimalMemoryStore.get(animal);
        memory.tick(animal.level().getGameTime());
        Entity threat = ThreatScanner.nearestThreat(animal, WildsenseConfig.panicRadius);
        if (threat != null) {
            danger = threat.position();
            memory.rememberDanger(danger, animal.level().getGameTime() + WildsenseConfig.memoryTicks);
            return true;
        }
        danger = memory.dangerPos(animal.level().getGameTime());
        return danger != null && animal.distanceToSqr(danger) < WildsenseConfig.panicRadius * WildsenseConfig.panicRadius * 2.0;
    }

    @Override
    public boolean canContinueToUse() {
        return danger != null && !animal.getNavigation().isDone();
    }

    @Override
    public void start() {
        moveAway();
    }

    @Override
    public void tick() {
        if (animal.tickCount % 12 == 0) {
            moveAway();
        }
        if (WildsenseConfig.stampedeEnabled && HerdCoordinator.herdSize(animal) >= WildsenseConfig.minStampedeHerdSize) {
            applyStampedeBump();
        }
    }

    private void moveAway() {
        if (danger == null) return;
        Vec3 current = animal.position();
        Vec3 delta = current.subtract(danger);
        if (delta.lengthSqr() < 0.001) {
            delta = new Vec3(animal.getRandom().nextDouble() - 0.5, 0.0, animal.getRandom().nextDouble() - 0.5);
        }
        Vec3 target = current.add(delta.normalize().scale(12.0));
        BlockPos pos = BlockPos.containing(target.x, target.y, target.z);
        animal.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WildsenseConfig.panicSpeed);
    }

    private void applyStampedeBump() {
        AABB box = animal.getBoundingBox().inflate(0.45);
        Vec3 motion = animal.getDeltaMovement();
        if (motion.horizontalDistanceSqr() < 0.01) return;
        Vec3 push = motion.normalize().scale(WildsenseConfig.stampedeKnockback);
        for (Entity entity : animal.level().getEntities(animal, box, entity -> entity.isAlive() && entity.isPushable())) {
            entity.push(push.x, 0.08, push.z);
        }
    }
}
