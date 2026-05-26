package com.wildsense.config;

import com.wildsense.WildsenseMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class WildsenseConfig {
    public static boolean enabled = true;
    public static boolean herdEnabled = true;
    public static boolean alertEnabled = true;
    public static boolean panicEnabled = true;
    public static boolean habitatEnabled = true;
    public static boolean trustEnabled = true;
    public static boolean stampedeEnabled = true;

    public static int fullAiRange = 48;
    public static int simpleAiRange = 96;
    public static int alertRadius = 18;
    public static int panicRadius = 8;
    public static int herdSearchRadius = 16;
    public static int shelterSearchRadius = 10;
    public static int memoryTicks = 20 * 30;
    public static int trustTicks = 20 * 60 * 20;
    public static int minStampedeHerdSize = 5;

    public static double panicSpeed = 1.35;
    public static double herdFollowSpeed = 1.05;
    public static double shelterSpeed = 1.0;
    public static double stampedeKnockback = 0.35;

    private WildsenseConfig() {
    }

    public static void load(Path path) {
        Properties properties = defaults();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException e) {
                WildsenseMod.LOGGER.warn("Failed to read {}, using defaults", path, e);
            }
        } else {
            try {
                Files.createDirectories(path.getParent());
                try (OutputStream out = Files.newOutputStream(path)) {
                    properties.store(out, "Wildsense configuration");
                }
            } catch (IOException e) {
                WildsenseMod.LOGGER.warn("Failed to create {}", path, e);
            }
        }

        enabled = bool(properties, "enabled", enabled);
        herdEnabled = bool(properties, "herdEnabled", herdEnabled);
        alertEnabled = bool(properties, "alertEnabled", alertEnabled);
        panicEnabled = bool(properties, "panicEnabled", panicEnabled);
        habitatEnabled = bool(properties, "habitatEnabled", habitatEnabled);
        trustEnabled = bool(properties, "trustEnabled", trustEnabled);
        stampedeEnabled = bool(properties, "stampedeEnabled", stampedeEnabled);

        fullAiRange = integer(properties, "fullAiRange", fullAiRange);
        simpleAiRange = integer(properties, "simpleAiRange", simpleAiRange);
        alertRadius = integer(properties, "alertRadius", alertRadius);
        panicRadius = integer(properties, "panicRadius", panicRadius);
        herdSearchRadius = integer(properties, "herdSearchRadius", herdSearchRadius);
        shelterSearchRadius = integer(properties, "shelterSearchRadius", shelterSearchRadius);
        memoryTicks = integer(properties, "memoryTicks", memoryTicks);
        trustTicks = integer(properties, "trustTicks", trustTicks);
        minStampedeHerdSize = integer(properties, "minStampedeHerdSize", minStampedeHerdSize);

        panicSpeed = decimal(properties, "panicSpeed", panicSpeed);
        herdFollowSpeed = decimal(properties, "herdFollowSpeed", herdFollowSpeed);
        shelterSpeed = decimal(properties, "shelterSpeed", shelterSpeed);
        stampedeKnockback = decimal(properties, "stampedeKnockback", stampedeKnockback);
    }

    private static Properties defaults() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("herdEnabled", Boolean.toString(herdEnabled));
        properties.setProperty("alertEnabled", Boolean.toString(alertEnabled));
        properties.setProperty("panicEnabled", Boolean.toString(panicEnabled));
        properties.setProperty("habitatEnabled", Boolean.toString(habitatEnabled));
        properties.setProperty("trustEnabled", Boolean.toString(trustEnabled));
        properties.setProperty("stampedeEnabled", Boolean.toString(stampedeEnabled));
        properties.setProperty("fullAiRange", Integer.toString(fullAiRange));
        properties.setProperty("simpleAiRange", Integer.toString(simpleAiRange));
        properties.setProperty("alertRadius", Integer.toString(alertRadius));
        properties.setProperty("panicRadius", Integer.toString(panicRadius));
        properties.setProperty("herdSearchRadius", Integer.toString(herdSearchRadius));
        properties.setProperty("shelterSearchRadius", Integer.toString(shelterSearchRadius));
        properties.setProperty("memoryTicks", Integer.toString(memoryTicks));
        properties.setProperty("trustTicks", Integer.toString(trustTicks));
        properties.setProperty("minStampedeHerdSize", Integer.toString(minStampedeHerdSize));
        properties.setProperty("panicSpeed", Double.toString(panicSpeed));
        properties.setProperty("herdFollowSpeed", Double.toString(herdFollowSpeed));
        properties.setProperty("shelterSpeed", Double.toString(shelterSpeed));
        properties.setProperty("stampedeKnockback", Double.toString(stampedeKnockback));
        return properties;
    }

    private static boolean bool(Properties properties, String key, boolean fallback) {
        return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(fallback)));
    }

    private static int integer(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double decimal(Properties properties, String key, double fallback) {
        try {
            return Double.parseDouble(properties.getProperty(key, Double.toString(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
