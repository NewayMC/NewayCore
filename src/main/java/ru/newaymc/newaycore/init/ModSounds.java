package ru.newaymc.newaycore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, NewaycoreMod.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> AK47_FIRE = REGISTRY.register("ak47_fire", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_fire")));
    public static final DeferredHolder<SoundEvent, SoundEvent> AK47_RELOAD = REGISTRY.register("ak47_reload", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_reload")));
}