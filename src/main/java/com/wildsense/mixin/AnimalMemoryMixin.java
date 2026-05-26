package com.wildsense.mixin;

import com.wildsense.ai.AnimalMemoryStore;
import com.wildsense.config.WildsenseConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMemoryMixin {
    private static final String WILDSENSE_MEMORY = "WildsenseMemory";

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void wildsense$saveMemory(CompoundTag tag, CallbackInfo ci) {
        if (!WildsenseConfig.enabled) return;
        Animal animal = (Animal) (Object) this;
        CompoundTag memory = AnimalMemoryStore.get(animal).save();
        if (!memory.isEmpty()) {
            tag.put(WILDSENSE_MEMORY, memory);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void wildsense$loadMemory(CompoundTag tag, CallbackInfo ci) {
        if (!WildsenseConfig.enabled) return;
        Animal animal = (Animal) (Object) this;
        AnimalMemoryStore.get(animal).load(
                tag.getCompoundOrEmpty(WILDSENSE_MEMORY),
                animal.level().getGameTime());
    }
}
