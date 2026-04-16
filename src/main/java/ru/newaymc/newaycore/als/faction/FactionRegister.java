package ru.newaymc.newaycore.als.faction;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FactionRegister {
    public static final Logger LOGGER = LogManager.getLogger(FactionRegister.class);

    @SubscribeEvent
    public static void onPlayerLoggedIn(FMLCommonSetupEvent event) {
        execute(event);
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
                FactionRegister.LOGGER.warn("Factions not found");
                break;
            } else {
                FactionRegister.LOGGER.info(("Loaded " + new java.text.DecimalFormat("##").format(CheckMultiplier) + " faction's"));
                break;
            }
        }
    }
}
