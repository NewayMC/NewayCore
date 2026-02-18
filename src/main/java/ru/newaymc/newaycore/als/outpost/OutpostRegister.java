package ru.newaymc.newaycore.als.outpost;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.init.ModBlocksInit;
import ru.newaymc.newaycore.network.vars.ModVariables;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Mod.EventBusSubscriber
public class OutpostRegister {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        execute(event, event.getEntity().level());
    }

    public static void execute(LevelAccessor world) {
        execute(null, world);
    }

    private static void execute(@Nullable Event event, LevelAccessor world) {
        double BlockX = 0;
        double BlockY = 0;
        double BlockZ = 0;
        double CheckMultiplier = 0;
        while (true) {
            ModVariables.OutpostFile = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Outposts/"), File.separator + ("outpost_id_" + new java.text.DecimalFormat("##").format(CheckMultiplier) + ".json"));
            if (ModVariables.OutpostFile.exists()) {
                CheckMultiplier = CheckMultiplier + 1;
                {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(ModVariables.OutpostFile));
                        StringBuilder jsonstringbuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonstringbuilder.append(line);
                        }
                        bufferedReader.close();
                        ModVariables.OutpostJsonObj = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                        BlockX = ModVariables.OutpostJsonObj.get("coordinate-x").getAsDouble();
                        BlockY = ModVariables.OutpostJsonObj.get("coordinate-y").getAsDouble();
                        BlockZ = ModVariables.OutpostJsonObj.get("coordinate-z").getAsDouble();
                        if ((world.getBlockState(BlockPos.containing(BlockX, BlockY, BlockZ))).getBlock() == ModBlocksInit.OUTPOST_HUB.get()) {
                            if (!world.isClientSide()) {
                                BlockPos _bp = BlockPos.containing(BlockX, BlockY, BlockZ);
                                BlockEntity _blockEntity = world.getBlockEntity(_bp);
                                BlockState _bs = world.getBlockState(_bp);
                                if (_blockEntity != null) {
                                    _blockEntity.getPersistentData().putString("outpost-id", ModVariables.OutpostJsonObj.get("id").getAsString());
                                    _blockEntity.getPersistentData().putString("faction", ModVariables.OutpostJsonObj.get("faction").getAsString());
                                }
                                if (world instanceof Level _level)
                                    _level.sendBlockUpdated(_bp, _bs, _bs, 3);
                            }
                            NewaycoreMod.LOGGER.info(("[NewayCore/ALS] Loaded " + ModVariables.OutpostJsonObj.get("id").getAsString()));
                        } else {
                            NewaycoreMod.LOGGER.warn(("[NewayCore/ALS] Creating " + ModVariables.OutpostJsonObj.get("id").getAsString()));
                            ModVariables.MapVariables.get(world).NextOutpostID = ModVariables.MapVariables.get(world).NextOutpostID - 1;
                            ModVariables.MapVariables.get(world).markSyncDirty();
                            world.setBlock(BlockPos.containing(BlockX, BlockY, BlockZ), ModBlocksInit.OUTPOST_HUB.get().defaultBlockState(), 3);
                            if (!world.isClientSide()) {
                                BlockPos _bp = BlockPos.containing(BlockX, BlockY, BlockZ);
                                BlockEntity _blockEntity = world.getBlockEntity(_bp);
                                BlockState _bs = world.getBlockState(_bp);
                                if (_blockEntity != null) {
                                    _blockEntity.getPersistentData().putString("outpost-id", ModVariables.OutpostJsonObj.get("id").getAsString());
                                    _blockEntity.getPersistentData().putString("faction", ModVariables.OutpostJsonObj.get("faction").getAsString());
                                }
                                if (world instanceof Level _level)
                                    _level.sendBlockUpdated(_bp, _bs, _bs, 3);
                            }
                            NewaycoreMod.LOGGER.info(("[NewayCore/ALS] Loaded " + ModVariables.OutpostJsonObj.get("id").getAsString()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (CheckMultiplier == 0) {
                NewaycoreMod.LOGGER.warn("[NewayCore/ALS] Outposts not found");
                break;
            } else {
                NewaycoreMod.LOGGER.info(("[NewayCore/ALS] Loaded " + new java.text.DecimalFormat("##").format(CheckMultiplier) + " outpost's"));
                break;
            }
        }
    }
}
