package ru.newaymc.newaycore.worlds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;

import java.util.OptionalLong;

public class WorldBuilder {
    private final ResourceLocation dimensionId;
    private ResourceLocation biomeId = Biomes.THE_VOID.location();
    private OptionalLong fixedTime = OptionalLong.empty();
    private boolean hasSkylight = true;
    private boolean hasCeiling = false;
    private boolean ultraWarm = false;
    private boolean natural = true;
    private double coordinateScale = 1.0;
    private boolean bedWorks = true;
    private boolean respawnAnchorWorks = false;
    private int minY = -64;
    private int height = 320;
    private ResourceLocation effectsLocation = ResourceLocation.withDefaultNamespace("overworld");
    private float ambientLight = 0.0f;
    private boolean piglinSafe = false;
    private boolean hasRaids = true;
    private int monsterSpawnLightLevel = 0;
    private int monsterSpawnBlockLightLimit = 7;

    public WorldBuilder(ResourceLocation dimensionId) {
        this.dimensionId = dimensionId;
    }

    public static WorldBuilder create(String modId, String dimensionName) {
        return new WorldBuilder(ResourceLocation.fromNamespaceAndPath(modId, dimensionName.toLowerCase()));
    }

    public static WorldBuilder create(ResourceLocation dimensionId) {
        return new WorldBuilder(dimensionId);
    }

    public WorldBuilder biome(ResourceLocation biomeId) {
        this.biomeId = biomeId;
        return this;
    }

    public WorldBuilder fixedTime(long time) {
        this.fixedTime = OptionalLong.of(time);
        return this;
    }

    public WorldBuilder noFixedTime() {
        this.fixedTime = OptionalLong.empty();
        return this;
    }

    public WorldBuilder hasSkylight(boolean hasSkylight) {
        this.hasSkylight = hasSkylight;
        return this;
    }

    public WorldBuilder hasCeiling(boolean hasCeiling) {
        this.hasCeiling = hasCeiling;
        return this;
    }

    public WorldBuilder ultraWarm(boolean ultraWarm) {
        this.ultraWarm = ultraWarm;
        return this;
    }

    public WorldBuilder natural(boolean natural) {
        this.natural = natural;
        return this;
    }

    public WorldBuilder coordinateScale(double scale) {
        this.coordinateScale = scale;
        return this;
    }

    public WorldBuilder bedWorks(boolean bedWorks) {
        this.bedWorks = bedWorks;
        return this;
    }

    public WorldBuilder respawnAnchorWorks(boolean works) {
        this.respawnAnchorWorks = works;
        return this;
    }

    public WorldBuilder height(int minY, int height) {
        this.minY = minY;
        this.height = height;
        return this;
    }

    public WorldBuilder effects(ResourceLocation effectsLocation) {
        this.effectsLocation = effectsLocation;
        return this;
    }

    public WorldBuilder ambientLight(float ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    public WorldBuilder monsterSettings(boolean piglinSafe, boolean hasRaids, int monsterSpawnLightLevel, int monsterSpawnBlockLightLimit) {
        this.piglinSafe = piglinSafe;
        this.hasRaids = hasRaids;
        this.monsterSpawnLightLevel = monsterSpawnLightLevel;
        this.monsterSpawnBlockLightLimit = monsterSpawnBlockLightLimit;
        return this;
    }

    public WorldTemplate build() {
        return new WorldTemplate(dimensionId,
                biomeId,
                fixedTime,
                hasSkylight,
                hasCeiling,
                ultraWarm,
                natural,
                coordinateScale,
                bedWorks,
                respawnAnchorWorks,
                minY,
                height,
                effectsLocation,
                ambientLight,
                piglinSafe,
                hasRaids,
                monsterSpawnLightLevel,
                monsterSpawnBlockLightLimit
        );
    }
}
