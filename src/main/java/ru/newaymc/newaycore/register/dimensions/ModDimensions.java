package ru.newaymc.newaycore.register.dimensions;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
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
