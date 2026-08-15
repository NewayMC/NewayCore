package ru.newaymc.newaycore;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import ru.newaymc.newaycore.register.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod("newaycore")
public class NewaycoreMod {
    public static final String MODID = "newaycore";
    public static final String MOD_DIR = (FMLPaths.GAMEDIR.get().toString() + "/newaycore/");

    public static HolderLookup.Provider provider;

    public NewaycoreMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
        ModBlocks.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModEntities.REGISTRY.register(modEventBus);
    }

    private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new Tuple<>(action, tick));
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
        workQueue.forEach(work -> {
            work.setB(work.getB() - 1);
            if (work.getB() == 0)
                actions.add(work);
        });
        actions.forEach(e -> e.getA().run());
        workQueue.removeAll(actions);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        provider = event.getServerResources().getRegistryLookup();
    }

    // For some test btw
    @EventBusSubscriber
    public static class PlayerLoggedIn {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
        }

        public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
            execute(null, world, x, y, z, entity);
        }

        private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {

        }
    }
}