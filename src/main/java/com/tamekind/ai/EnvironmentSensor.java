package com.tamekind.ai;

import com.tamekind.compat.TamekindTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class EnvironmentSensor {
    private EnvironmentSensor() {
    }

    public static Vec3 nearbyHazard(Animal animal, int radius) {
        Level level = animal.level();
        BlockPos origin = animal.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    var state = level.getBlockState(cursor);
                    if (state.is(TamekindTags.AVOID_BLOCKS) || state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                        double d = origin.distSqr(cursor);
                        if (d < bestDist) {
                            bestDist = d;
                            best = cursor.immutable();
                        }
                    }
                }
            }
        }
        return best == null ? null : Vec3.atCenterOf(best);
    }
}
