package ru.newaymc.newaycore.worlds;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.files.Utils;
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

    @SubscribeEvent
    public static void onServerStartup(ServerStartedEvent event) {

    }

    public static void saveDimension(ResourceLocation dimension) {
        File mainDir = new File(ZstdFileCompressor.getZstdCompressDir().getPath() + "/" + dimension.getPath());
        if (mainDir.exists()) {
            File regions = new File(mainDir.getPath() + "/regions/");
            File world = new File(mainDir.getPath() + "/world/");

            if (!regions.exists() || !world.exists()) {
                return;
            }

            try {
                Path targetDir = Paths.get(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());
                Files.createDirectory(targetDir);

                ZstdFileCompressor compressor = new ZstdFileCompressor();
                compressor.compressFolder(regions, true, true);

                Path worldPath = world.toPath();
                Path regionPath = regions.toPath();

                long worldSize = Utils.getFolderSize(worldPath);
                long maxSize = 100 * 1024 * 1024;
                if (worldSize > maxSize) {
                    compressor.compressFolderStreaming(world, true, 1024 * 1024, true);
                } else {
                    compressor.compressFolder(world, true, true);
                }

                Files.move(worldPath, targetDir, StandardCopyOption.REPLACE_EXISTING);
                Files.move(regionPath, targetDir, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Compression error: {}", e.toString());
            }
        }
    }

    public static boolean loadFromRegions(ResourceLocation dimension, boolean saveCache) {
        Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimension);
        ServerLevel serverLevel = SERVER.getLevel(worldTemplate.get().getLevelKey());
        File mainDir = new File(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());

        if (!mainDir.exists()) {
            LOGGER.error("Dimension directory not found: {}", dimension);
            return false;
        }

        if (serverLevel == null) {
            LOGGER.error("ServerLevel is null for dimension: {}", dimension);
            return false;
        }

        File regions = new File(mainDir.getPath() + "/regions/");
        File world = new File(mainDir.getPath() + "/world/");

        if (!regions.exists() || !world.exists()) {
            return false;
        }

        try {
            serverLevel.getChunkSource().save(true);
            serverLevel.getChunkSource().close();

            ZstdFileCompressor compressor = new ZstdFileCompressor();
            compressor.decompressFolder(regions, true);
            compressor.decompressFolder(world, true);

            Path savePath = Paths.get(CURRENT_WORLD.getPath() + "/dimensions/" + dimension.getNamespace());
            Path worldPath = world.toPath();

            if (saveCache) {
                Files.copy(worldPath, savePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(worldPath, savePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Decompression error: {}", e.toString());
        }
        return true;
    }
}
