package ru.newaymc.newaycore.worlds.build;

import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.OptionalLong;

@Getter
public class WorldTemplate {
    private final ResourceLocation dimensionId;
    private final ResourceLocation biomeId;
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final int minY;
    private final int height;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final boolean piglinSafe;
    private final boolean hasRaids;
    private final int monsterSpawnLightLevel;
    private final int monsterSpawnBlockLightLimit;

    private final ResourceKey<LevelStem> levelStemKey;
    private final ResourceKey<Level> levelKey;
    private final ResourceKey<DimensionType> dimensionTypeKey;

    public WorldTemplate(ResourceLocation dimensionId, ResourceLocation biomeId, OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling,
                         boolean ultraWarm, boolean natural, double coordinateScale, boolean bedWorks, boolean respawnAnchorWorks, int minY, int height,
                         ResourceLocation effectsLocation, float ambientLight, boolean piglinSafe, boolean hasRaids, int monsterSpawnLightLevel, int monsterSpawnBlockLightLimit) {
        this.dimensionId = dimensionId;
        this.biomeId = biomeId;
        this.fixedTime = fixedTime;
        this.hasSkylight = hasSkylight;
        this.hasCeiling = hasCeiling;
        this.ultraWarm = ultraWarm;
        this.natural = natural;
        this.coordinateScale = coordinateScale;
        this.bedWorks = bedWorks;
        this.respawnAnchorWorks = respawnAnchorWorks;
        this.minY = minY;
        this.height = height;
        this.effectsLocation = effectsLocation;
        this.ambientLight = ambientLight;
        this.piglinSafe = piglinSafe;
        this.hasRaids = hasRaids;
        this.monsterSpawnLightLevel = monsterSpawnLightLevel;
        this.monsterSpawnBlockLightLimit = monsterSpawnBlockLightLimit;

        this.levelStemKey = ResourceKey.create(Registries.LEVEL_STEM, dimensionId);
        this.levelKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        this.dimensionTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(dimensionId.getNamespace(), dimensionId.getPath() + "_type"));
    }
}
