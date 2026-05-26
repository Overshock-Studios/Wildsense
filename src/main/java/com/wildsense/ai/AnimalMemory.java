package com.wildsense.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class AnimalMemory {
    private static final String DANGER = "Danger";
    private static final String TRUST = "Trust";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    private static final String UNTIL = "Until";
    private static final String SCORE = "Score";

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

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        if (dangerPos != null) {
            CompoundTag danger = new CompoundTag();
            danger.putDouble(X, dangerPos.x);
            danger.putDouble(Y, dangerPos.y);
            danger.putDouble(Z, dangerPos.z);
            danger.putLong(UNTIL, dangerUntil);
            root.put(DANGER, danger);
        }
        if (!trustedPlayers.isEmpty()) {
            CompoundTag trust = new CompoundTag();
            for (Map.Entry<UUID, TrustEntry> entry : trustedPlayers.entrySet()) {
                CompoundTag saved = new CompoundTag();
                saved.putDouble(SCORE, entry.getValue().score);
                saved.putLong(UNTIL, entry.getValue().untilTick);
                trust.put(entry.getKey().toString(), saved);
            }
            root.put(TRUST, trust);
        }
        return root;
    }

    public void load(CompoundTag root, long gameTime) {
        dangerPos = null;
        dangerUntil = 0L;
        trustedPlayers.clear();

        CompoundTag danger = root.getCompoundOrEmpty(DANGER);
        long savedDangerUntil = danger.getLongOr(UNTIL, 0L);
        if (savedDangerUntil > gameTime) {
            dangerPos = new Vec3(
                    danger.getDoubleOr(X, 0.0),
                    danger.getDoubleOr(Y, 0.0),
                    danger.getDoubleOr(Z, 0.0));
            dangerUntil = savedDangerUntil;
        }

        CompoundTag trust = root.getCompoundOrEmpty(TRUST);
        for (String key : trust.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);
                CompoundTag saved = trust.getCompoundOrEmpty(key);
                double score = saved.getDoubleOr(SCORE, 0.0);
                long until = saved.getLongOr(UNTIL, 0L);
                if (score > 0.0 && until > gameTime) {
                    trustedPlayers.put(uuid, new TrustEntry(Math.min(1.0, score), until));
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed third-party or old data.
            }
        }
    }

    private record TrustEntry(double score, long untilTick) {
    }
}
