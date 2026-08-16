package ru.newaymc.newaycore.worlds;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldRegister {
    private static final List<WorldTemplate> REGISTERED_WORLDS = new ArrayList<>();

    public static WorldTemplate register(WorldTemplate dimension) {
        REGISTERED_WORLDS.add(dimension);
        return dimension;
    }

    public static List<WorldTemplate> getRegisteredDimensions() {
        return List.copyOf(REGISTERED_WORLDS);
    }

    public static void addToRegistryBuilder(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.DIMENSION_TYPE, WorldRegister::bootstrapDimensionTypes);
        registryBuilder.add(Registries.LEVEL_STEM, WorldRegister::bootstrapLevelStems);
    }

    public static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        for (WorldTemplate dimension : REGISTERED_WORLDS) {
            context.register(dimension.getDimensionTypeKey(), createDimensionType(dimension));
        }
    }

    /**
     * Bootstrap all registered level stems
     */
    public static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        for (WorldTemplate dimension : REGISTERED_WORLDS) {
            context.register(dimension.getLevelStemKey(), createLevelStem(context, dimension));
        }
    }

    private static DimensionType createDimensionType(WorldTemplate dim) {
        return new DimensionType(dim.getFixedTime(), dim.isHasSkylight(), dim.isHasCeiling(),
                dim.isUltraWarm(), dim.isNatural(), dim.getCoordinateScale(),
                dim.isBedWorks(), dim.isRespawnAnchorWorks(), dim.getMinY(),
                dim.getHeight(), dim.getHeight(), BlockTags.INFINIBURN_OVERWORLD,
                dim.getEffectsLocation(), dim.getAmbientLight(), new DimensionType.MonsterSettings(dim.isPiglinSafe(), dim.isHasRaids(), ConstantInt.of(dim.getMonsterSpawnLightLevel()), dim.getMonsterSpawnBlockLightLimit())
        );
    }

    private static LevelStem createLevelStem(BootstrapContext<LevelStem> context, WorldTemplate dim) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        return new LevelStem(
                context.lookup(Registries.DIMENSION_TYPE).getOrThrow(dim.getDimensionTypeKey()),
                new FlatLevelSource(
                        new FlatLevelGeneratorSettings(
                                Optional.empty(),
                                biomes.getOrThrow(ResourceKey.create(Registries.BIOME, dim.getBiomeId())), List.of()
                        )
                )
        );
    }
}
