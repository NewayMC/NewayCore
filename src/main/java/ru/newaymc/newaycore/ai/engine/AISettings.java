package ru.newaymc.newaycore.ai.engine;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.newaymc.newaycore.network.NewaycoreModVariables;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AISettings {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        execute();
    }

    public static void execute() {
        execute(null);
    }

    private static void execute(@Nullable Event event) {
        NewaycoreModVariables.StandartShooterFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/NewayCore/AI_Engine/Entities"), File.separator + "StandartShooter.data.json");
        if (!NewaycoreModVariables.StandartShooterFile.exists()) {
            try {
                NewaycoreModVariables.StandartShooterFile.getParentFile().mkdirs();
                NewaycoreModVariables.StandartShooterFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("shoot", 3);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("damage", 3);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("speed", 4);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("inaccurace-accumulation", 3);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("recoil", 3);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("ammunation", 30);
        NewaycoreModVariables.StandartShooterJsonObj.addProperty("recovery-time", 40);
        {
            com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try {
                FileWriter fileWriter = new FileWriter(NewaycoreModVariables.StandartShooterFile);
                fileWriter.write(mainGSONBuilderVariable.toJson(NewaycoreModVariables.StandartShooterJsonObj));
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        NewaycoreModVariables.EliteShooterFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/NewayCore/AI_Engine/Entities"), File.separator + "EliteShooter.data.json");
        if (!NewaycoreModVariables.EliteShooterFile.exists()) {
            try {
                NewaycoreModVariables.EliteShooterFile.getParentFile().mkdirs();
                NewaycoreModVariables.EliteShooterFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("shoot", 5);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("damage", 4);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("speed", 4);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("inaccurace-accumulation", 2);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("recoil", 1);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("ammunation", 30);
        NewaycoreModVariables.EliteShooterJsonObj.addProperty("recovery-time", 30);
        {
            com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try {
                FileWriter fileWriter = new FileWriter(NewaycoreModVariables.EliteShooterFile);
                fileWriter.write(mainGSONBuilderVariable.toJson(NewaycoreModVariables.EliteShooterJsonObj));
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}