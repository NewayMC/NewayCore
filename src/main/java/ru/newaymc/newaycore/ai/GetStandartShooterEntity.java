package ru.newaymc.newaycore.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import ru.newaymc.newaycore.ai.engine.ShooterAIModule;
import ru.newaymc.newaycore.network.NewaycoreModVariables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GetStandartShooterEntity {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(NewaycoreModVariables.StandartShooterFile));
                StringBuilder jsonstringbuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonstringbuilder.append(line);
                }
                bufferedReader.close();
                NewaycoreModVariables.StandartShooterJsonObj = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                ShooterAIModule.execute(world, x, y, z, entity, NewaycoreModVariables.StandartShooterJsonObj.get("ammunation").getAsDouble(), NewaycoreModVariables.StandartShooterJsonObj.get("damage").getAsDouble(),
                        NewaycoreModVariables.StandartShooterJsonObj.get("inaccurace-accumulation").getAsDouble(), NewaycoreModVariables.StandartShooterJsonObj.get("recoil").getAsDouble(),
                        NewaycoreModVariables.StandartShooterJsonObj.get("recovery-time").getAsDouble(), NewaycoreModVariables.StandartShooterJsonObj.get("shoot").getAsDouble(),
                        NewaycoreModVariables.StandartShooterJsonObj.get("speed").getAsDouble());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}