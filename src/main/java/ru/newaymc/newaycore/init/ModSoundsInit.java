package ru.newaymc.newaycore.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.newaymc.newaycore.NewaycoreMod;

public class ModSoundsInit {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NewaycoreMod.MODID);
    public static final RegistryObject<SoundEvent> AK47_FIRE = REGISTRY.register("ak47_fire", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("newaycore", "ak47_fire")));
    public static final RegistryObject<SoundEvent> AK47_RELOAD = REGISTRY.register("ak47_reload", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("newaycore", "ak47_reload")));
}