package ru.newaymc.newaycore;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.files.ZstdFileCompressor;
import ru.newaymc.newaycore.register.*;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Mod("newaycore")
public class NewaycoreMod {
    public static final String MODID = "newaycore";
    public static final String MOD_DIR = (FMLPaths.GAMEDIR.get().toString() + "/newaycore/");

    public static HolderLookup.Provider provider;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public NewaycoreMod(IEventBus modEventBus) {
        prepareModDirectories();

        NeoForge.EVENT_BUS.register(this);
        ModBlocks.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);

        modEventBus.addListener(this::gatherData);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        provider = event.getServerResources().getRegistryLookup();
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
    }

    private void prepareModDirectories() {
        ZstdFileCompressor.prepareDirectory(MOD_DIR);
        new File(MOD_DIR + "/saves/data/").mkdirs();
    }

    @EventBusSubscriber
    public static class ModEvents {

        // For some test btw
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        }
    }
}