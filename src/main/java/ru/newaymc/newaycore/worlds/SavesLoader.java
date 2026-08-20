package ru.newaymc.newaycore.worlds;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.files.ZstdFileCompressor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Getter
@EventBusSubscriber
// TODO: Testing
public class SavesLoader {
    private static File CURRENT_WORLD;
    private static boolean FIRST_JOIN;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
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

    public static void prepareWorldFiles(PlayerEvent.PlayerLoggedInEvent event, ResourceLocation... dimensionId) {
        for (ResourceLocation dimension : dimensionId) {
            File mainDir = new File(ZstdFileCompressor.getZstdCompressDir().getPath() + dimension.getPath());
            if (mainDir.exists()) {
                File regions = new File(mainDir.getPath() + "/regions/");
                File world = new File(mainDir.getPath() + "/world/");

                long maxSize = 100 * 1024 * 1024;
                ZstdFileCompressor compressor = new ZstdFileCompressor();
                try {
                    compressor.compressFolder(regions, true, true);

                    Path worldPath = world.toPath();
                    Path regionPath = regions.toPath();

                    long worldSize = Files.size(worldPath);
                    if (worldSize > maxSize) {
                        compressor.compressFolderStreaming(world, true, 1024 * 1024, true);
                    }

                    Path targetDir = Paths.get(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());
                    Files.move(worldPath, targetDir, StandardCopyOption.ATOMIC_MOVE);
                    Files.move(regionPath, targetDir, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    NewaycoreMod.LOGGER.error("Compression error: {}", e.toString());
                }
            }
        }
    }

    public static void loadWorldFiles(String modId, ResourceLocation... dimensionId) {
        for (ResourceLocation dimension : dimensionId) {
            File mainDir = new File(NewaycoreMod.MOD_DIR + "/saves/data/" + dimension.getPath());
            if (mainDir.exists()) {
                File regions = new File(mainDir.getPath() + "/regions/");
                File world = new File(mainDir.getPath() + "/world/");

                ZstdFileCompressor compressor = new ZstdFileCompressor();
                try {
                    compressor.decompressFolder(regions, false);
                    compressor.decompressFolder(world, false);
                } catch (IOException e) {
                    NewaycoreMod.LOGGER.error("Decompression error: {}", e.toString());
                }
                File save = new File(CURRENT_WORLD.getPath() + "/dimensions/" + modId + dimension.getPath());

                Path worldPath = world.toPath();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(worldPath)) {
                    for (Path entry : stream) {
                        Path savePath = save.toPath();
                        Files.move(entry, savePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Path regionsPath = regions.toPath();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(regionsPath)) {
                    for (Path entry : stream) {
                        Path savePath = Paths.get(save.getPath() + "/region/");
                        Files.move(entry, savePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
