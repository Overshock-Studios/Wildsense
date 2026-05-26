package com.wildsense.ai;

import com.wildsense.config.WildsenseConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public enum AiLod {
    FULL,
    SIMPLE,
    SLEEP;

    public static AiLod forAnimal(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return SLEEP;
        double nearest = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            nearest = Math.min(nearest, player.distanceToSqr(animal));
        }
        int full = WildsenseConfig.fullAiRange;
        if (nearest <= (double) full * full) return FULL;
        int simple = WildsenseConfig.simpleAiRange;
        if (nearest <= (double) simple * simple) return SIMPLE;
        return SLEEP;
    }
}
