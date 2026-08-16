package ru.newaymc.newaycore.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import ru.newaymc.newaycore.NewaycoreMod;

public class ModDimensions {
    // Dev dimension
    public static final ResourceKey<DimensionType> DEV_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension_type"));
    public static final ResourceKey<LevelStem> DEV_DIM_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension"));
    public static final ResourceKey<Level> DEV_DIM_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension"));
}
