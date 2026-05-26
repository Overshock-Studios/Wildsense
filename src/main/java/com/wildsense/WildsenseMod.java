package com.wildsense;

import com.wildsense.ai.PassiveGoalInjector;
import com.wildsense.config.WildsenseConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WildsenseMod implements ModInitializer {
    public static final String MOD_ID = "wildsense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        WildsenseConfig.load(FabricLoader.getInstance().getConfigDir().resolve("wildsense.properties"));
        PassiveGoalInjector.register();
        LOGGER.info("Wildsense initialized.");
    }
}
