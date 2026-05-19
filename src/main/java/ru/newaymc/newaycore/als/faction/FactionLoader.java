package ru.newaymc.newaycore.als.faction;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import ru.newaymc.newaycore.ai.engine.AlsController;

import javax.annotation.Nullable;
import java.io.File;

@EventBusSubscriber
public class FactionLoader {

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        execute();
    }

    public static void execute() {
        execute(null);
    }

    private static void execute(@Nullable Event event) {
        File FactionFile = new File("");
        double CheckMultiplier = 0;
        while (true) {
            FactionFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Factions/"), File.separator + ("faction_id_" + new java.text.DecimalFormat("##").format(CheckMultiplier) + ".json"));
            if (FactionFile.exists()) {
                CheckMultiplier = CheckMultiplier + 1;
            } else if (CheckMultiplier == 0) {
                AlsController.LOGGER.warn("Factions not found");
                break;
            } else {
                AlsController.LOGGER.info(("Loaded " + new java.text.DecimalFormat("##").format(CheckMultiplier) + " faction's"));
                break;
            }
        }
    }
}
