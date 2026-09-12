package ru.newaymc.newaycore.worlds;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.files.ZstdFileCompressor;
import ru.newaymc.newaycore.worlds.build.WorldRegister;
import ru.newaymc.newaycore.worlds.build.WorldTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Getter
@EventBusSubscriber
public class DimensionLoader {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/DimensionLoader");

    private static File CURRENT_WORLD;
    private static MinecraftServer SERVER;
    private static boolean FIRST_JOIN;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        SERVER = event.getEntity().getServer();
        if (!event.getEntity().level().isClientSide) {
            CURRENT_WORLD = event.getEntity().getServer().getWorldPath(LevelResource.ROOT).toFile().getParentFile();
            File worldFile = new File(NewaycoreMod.MOD_DIR + "/saves/data/", File.separator + CURRENT_WORLD.getName());
            if (!worldFile.exists()) {
                try {
                    worldFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                FIRST_JOIN = true;
            }
        }
    }

    public static void teleportToWorld(Player player,Vec3 pos, ResourceLocation target, boolean autoLoad ) {
        Level level = player.level();
        if (!level.isClientSide) {

            if (WorldRegister.getDimensionOrNull(target.getPath()) == null) {
                return;
            }

            ServerPlayer serverPlayer = (ServerPlayer) player;
            ServerLevel load = player.getServer().getLevel(WorldRegister.findDimension(NewaycoreMod.MODID, "load").get().getLevelKey());
            ServerLevel targetLevel = player.getServer().getLevel(WorldRegister.findDimension(target).get().getLevelKey());

            if (autoLoad) {
                serverPlayer.teleportTo(load, pos.x(), pos.y(), pos.z(), serverPlayer.getXRot(), serverPlayer.getYRot());
                if (loadDimension(target)) {
                    serverPlayer.teleportTo(targetLevel, pos.x(), pos.y(), pos.z(), player.getXRot(), player.getYRot());
                }
            } else {
                serverPlayer.teleportTo(targetLevel, pos.x(), pos.y(), pos.z(), player.getXRot(), player.getYRot());
            }
        }
    }

    /**
     * Test compression result:
     * <br>
     * From 914 mb to 680 mb ( .mca files )
     * @param dimension
     */
    public static void prepareDimension(ResourceLocation dimension) {
        LOGGER.info("Preparing dimension {} ", dimension);
        File mainDir = new File(ZstdFileCompressor.getZstdCompressDir().getPath() + "/" + dimension.getPath());
        if (mainDir.exists()) {
            try {
                Path targetDir = Paths.get(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());
                Files.createDirectory(targetDir);

                ZstdFileCompressor compressor = new ZstdFileCompressor();

                Path mainDirPath = mainDir.toPath();
                compressor.compressFolderStreaming(mainDir, true, 1024 * 1024, true);

                Files.move(mainDirPath, targetDir, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Preparation error: {}", e.toString());
            }
        } else {
            LOGGER.warn("Directory not found");
        }
    }

    public static boolean loadDimension(ResourceLocation dimension) {
        Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimension);
        ServerLevel serverLevel = SERVER.getLevel(worldTemplate.get().getLevelKey());
        File mainDir = new File(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());

        if (!mainDir.exists()) {
            LOGGER.warn("Dimension directory not found: {}", dimension);
            return false;
        }

        File regionDir = new File(mainDir.getPath() + "/regions");
        if (regionDir.exists()) {
            LOGGER.error("Dimension {} already loaded", dimension);
            return false;
        }

        if (serverLevel == null) {
            LOGGER.warn("ServerLevel is null for dimension: {}", dimension);
            return false;
        }

        try {
            ZstdFileCompressor compressor = new ZstdFileCompressor();
            compressor.decompressFolder(mainDir, true);

            File save = new File(CURRENT_WORLD.getPath() + "/dimensions/" + dimension.getNamespace() + "/" + dimension.getPath());

            LOGGER.debug("Copying directory");
            FileUtils.copyDirectory(mainDir, save);
            FileUtils.deleteDirectory(mainDir);

            LOGGER.info("Dimension {} successfully loaded", dimension.toString());
        } catch (IOException e) {
            LOGGER.error("Loading error: {}", e.toString());
        }
        return true;
    }
}
