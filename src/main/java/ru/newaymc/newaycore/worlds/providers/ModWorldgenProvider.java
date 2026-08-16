package ru.newaymc.newaycore.worlds.providers;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.register.ModDimensions;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModWorldgenProvider::bootstrapDimensionTypes)
            .add(Registries.LEVEL_STEM, ModWorldgenProvider::bootstrapLevelStems);

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(NewaycoreMod.MODID));
    }

    private static void bootstrapDimensionTypes(BootstrapContext<DimensionType> ctx) {
        ctx.register(
                ModDimensions.DEV_DIMENSION_TYPE,
                new DimensionType(
                        OptionalLong.of(0),
                        true,
                        false,
                        false,
                        true,
                        1.0,
                        true,
                        false,
                        -64,
                        320,
                        320,
                        BlockTags.INFINIBURN_OVERWORLD,
                        ResourceLocation.parse("minecraft:overworld"),
                        0,
                        new DimensionType.MonsterSettings(false, true, ConstantInt.of(0), 7)
                )
        );
    }

    private static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<net.minecraft.world.level.biome.Biome> biomes = context.lookup(Registries.BIOME);

        context.register(ModDimensions.DEV_DIM_LEVEL_STEM,
                new LevelStem(
                        context.lookup(Registries.DIMENSION_TYPE).getOrThrow(ModDimensions.DEV_DIMENSION_TYPE),
                        new FlatLevelSource(
                                new FlatLevelGeneratorSettings(
                                        Optional.empty(),
                                        biomes.getOrThrow(Biomes.THE_VOID),
                                        List.of()
                                )
                        )
                ));
    }
}
