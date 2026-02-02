package ru.newaymc.newaycore.factions;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.newaymc.newaycore.NewaycoreMod;

import javax.annotation.Nullable;
import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FactionRegister {
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
            FactionFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Factions/"), File.separator + ("id_" + new java.text.DecimalFormat("##").format(CheckMultiplier) + ".data.json"));
            if (FactionFile.exists()) {
                CheckMultiplier = CheckMultiplier + 1;
            } else if (CheckMultiplier == 0) {
                NewaycoreMod.LOGGER.warn("[NewayCore/Factions] Factions not found");
                break;
            } else {
                NewaycoreMod.LOGGER.info(("[NewayCore/Factions] Loaded " + new java.text.DecimalFormat("##").format(CheckMultiplier) + " faction(s)"));
                break;
            }
        }
    }
}
