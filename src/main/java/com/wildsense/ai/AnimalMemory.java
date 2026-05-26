package com.wildsense.ai;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class AnimalMemory {
    private Vec3 dangerPos;
    private long dangerUntil;
    private final Map<UUID, Long> trustedPlayers = new HashMap<>();

    public void rememberDanger(Vec3 pos, long untilTick) {
        this.dangerPos = pos;
        this.dangerUntil = untilTick;
    }

    public Vec3 dangerPos(long gameTime) {
        return gameTime <= dangerUntil ? dangerPos : null;
    }

    public void trust(UUID playerId, long untilTick) {
        trustedPlayers.put(playerId, untilTick);
    }

    public boolean trusts(UUID playerId, long gameTime) {
        Long until = trustedPlayers.get(playerId);
        return until != null && gameTime <= until;
    }

    public void tick(long gameTime) {
        if (gameTime > dangerUntil) {
            dangerPos = null;
        }
        Iterator<Map.Entry<UUID, Long>> iterator = trustedPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            if (gameTime > iterator.next().getValue()) {
                iterator.remove();
            }
        }
    }
}
