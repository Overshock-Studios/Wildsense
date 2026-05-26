package com.wildsense.ai;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class AnimalMemory {
    private Vec3 dangerPos;
    private long dangerUntil;
    private final Map<UUID, TrustEntry> trustedPlayers = new HashMap<>();

    public void rememberDanger(Vec3 pos, long untilTick) {
        this.dangerPos = pos;
        this.dangerUntil = untilTick;
    }

    public Vec3 dangerPos(long gameTime) {
        return gameTime <= dangerUntil ? dangerPos : null;
    }

    public void addTrust(UUID playerId, double amount, long untilTick) {
        TrustEntry current = trustedPlayers.get(playerId);
        double score = current == null ? 0.0 : current.score;
        trustedPlayers.put(playerId, new TrustEntry(Math.min(1.0, score + amount), untilTick));
    }

    public boolean trusts(UUID playerId, long gameTime) {
        return trustScore(playerId, gameTime) > 0.0;
    }

    public double trustScore(UUID playerId, long gameTime) {
        TrustEntry entry = trustedPlayers.get(playerId);
        if (entry == null || gameTime > entry.untilTick) return 0.0;
        return entry.score;
    }

    public void tick(long gameTime) {
        if (gameTime > dangerUntil) {
            dangerPos = null;
        }
        Iterator<Map.Entry<UUID, TrustEntry>> iterator = trustedPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            if (gameTime > iterator.next().getValue().untilTick) {
                iterator.remove();
            }
        }
    }

    private record TrustEntry(double score, long untilTick) {
    }
}
