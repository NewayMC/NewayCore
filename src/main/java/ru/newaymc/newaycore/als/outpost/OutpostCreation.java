package ru.newaymc.newaycore.als.outpost;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.newaymc.newaycore.network.vars.ModVariables;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutpostCreation {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        ModVariables.OutpostFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Outposts/"),
                File.separator + ("outpost_id_" + new java.text.DecimalFormat("##").format(ModVariables.MapVariables.get(world).NextOutpostID) + ".json"));
        if (!ModVariables.OutpostFile.exists()) {
            try {
                ModVariables.OutpostFile.getParentFile().mkdirs();
                ModVariables.OutpostFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        ModVariables.OutpostJsonObj.addProperty("id", ("outpost_id_" + new java.text.DecimalFormat("##").format(ModVariables.MapVariables.get(world).NextOutpostID)));
        ModVariables.OutpostJsonObj.addProperty("faction", "none");
        ModVariables.OutpostJsonObj.addProperty("coordinate-x", x);
        ModVariables.OutpostJsonObj.addProperty("coordinate-y", y);
        ModVariables.OutpostJsonObj.addProperty("coordinate-z", z);
        {
            com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try {
                FileWriter fileWriter = new FileWriter(ModVariables.OutpostFile);
                fileWriter.write(mainGSONBuilderVariable.toJson(ModVariables.OutpostJsonObj));
                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        ModVariables.MapVariables.get(world).NextOutpostID = ModVariables.MapVariables.get(world).NextOutpostID + 1;
        ModVariables.MapVariables.get(world).markSyncDirty();
    }

}
