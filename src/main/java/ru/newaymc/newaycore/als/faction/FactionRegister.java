package ru.newaymc.newaycore.als.faction;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import ru.newaymc.newaycore.NewaycoreMod;

import javax.annotation.Nullable;
import java.io.File;

@Mod.EventBusSubscriber
public class FactionRegister {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
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
                NewaycoreMod.LOGGER.warn("[NewayCore/ALS] Factions not found");
                break;
            } else {
                NewaycoreMod.LOGGER.info(("[NewayCore/Factions] Loaded " + new java.text.DecimalFormat("##").format(CheckMultiplier) + " faction's"));
                break;
            }
        }
    }
}
