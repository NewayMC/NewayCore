package ru.newaymc.newaycore.register.dimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.worlds.build.WorldBuilder;
import ru.newaymc.newaycore.worlds.build.WorldRegister;
import ru.newaymc.newaycore.worlds.build.WorldTemplate;
import ru.newaymc.newaycore.worlds.providers.WorldDataProvider;

@EventBusSubscriber
public class ModDimensions {
    // Dev dim ( deprecated )
    public static final ResourceKey<DimensionType> DEV_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension_type"));
    public static final ResourceKey<LevelStem> DEV_DIM_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension"));
    public static final ResourceKey<Level> DEV_DIM_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "dev_dimension"));

    // Dev dim
    public static final WorldTemplate DEV_DIM = WorldBuilder.create(NewaycoreMod.MODID, "dev_dimension").build();

    // Loading dimension
    public static final WorldTemplate LOADING_DIM = WorldBuilder.create(NewaycoreMod.MODID, "load").build();

    static {
        WorldRegister.register(DEV_DIM);
        WorldRegister.register(LOADING_DIM);
    }

    @SubscribeEvent
    private static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new WorldDataProvider(output, event.getLookupProvider(), NewaycoreMod.MODID));
    }
}
