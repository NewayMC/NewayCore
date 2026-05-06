package ru.newaymc.newaycore.als.faction;

import javafx.event.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@EventBusSubscriber
public class NeutralFactionCreate {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        execute();
    }

    public static void execute() {
        execute(null);
    }

    private static void execute(@Nullable Event event) {
        File NeutralFactionFile = new File("");
        com.google.gson.JsonObject NeutralFactionJsonObj = new com.google.gson.JsonObject();
        com.google.gson.JsonArray NeutralFactionAllyArray = new com.google.gson.JsonArray();
        com.google.gson.JsonArray NeutralFactionEnemyArray = new com.google.gson.JsonArray();
        NeutralFactionFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Factions/"), File.separator + "faction_id_0.json");
        if (!NeutralFactionFile.exists()) {
            try {
                NeutralFactionFile.getParentFile().mkdirs();
                NeutralFactionFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        NeutralFactionAllyArray.add("none");
        NeutralFactionEnemyArray.add("none");
        NeutralFactionJsonObj.addProperty("id", 0);
        NeutralFactionJsonObj.addProperty("name", "Neutral");
        NeutralFactionJsonObj.addProperty("default-relationships", (-1));
        NeutralFactionJsonObj.add("allies", NeutralFactionAllyArray);
        NeutralFactionJsonObj.add("enemies", NeutralFactionEnemyArray);
        {
            com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try {
                FileWriter fileWriter = new FileWriter(NeutralFactionFile);
                fileWriter.write(mainGSONBuilderVariable.toJson(NeutralFactionJsonObj));
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}