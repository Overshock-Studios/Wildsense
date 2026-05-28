package com.tamekind.ai;

import com.tamekind.TamekindMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public final class BiomeComfort {
    private static final Map<Identifier, TagKey<Biome>> CACHE = new HashMap<>();

    private BiomeComfort() {
    }

    public static boolean isInComfortBiome(Animal animal) {
        Identifier preyId = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        TagKey<Biome> key = CACHE.computeIfAbsent(preyId, id ->
                TagKey.create(Registries.BIOME,
                        Identifier.fromNamespaceAndPath(TamekindMod.MOD_ID,
                                "comfortable_in/" + id.getNamespace() + "/" + id.getPath())));
        return animal.level().getBiome(animal.blockPosition()).is(key);
    }
}
