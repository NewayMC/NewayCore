package ru.newaymc.newaycore.worlds.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import ru.newaymc.newaycore.worlds.build.WorldRegister;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WorldDataProvider extends DatapackBuiltinEntriesProvider {

    public WorldDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries, createRegistryBuilder(), Set.of(modid));
    }

    private static RegistrySetBuilder createRegistryBuilder() {
        RegistrySetBuilder builder = new RegistrySetBuilder();
        WorldRegister.addToRegistryBuilder(builder);
        return builder;
    }
}
