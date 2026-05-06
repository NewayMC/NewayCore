package ru.newaymc.newaycore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.init.*;
import ru.newaymc.newaycore.network.vars.ModVariables;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod("newaycore")
public class NewaycoreMod {
    public static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.class);
    public static final String MODID = "newaycore";

    public NewaycoreMod(IEventBus modEventBus) {

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerNetworking);
        ModSounds.REGISTRY.register(modEventBus);
        ModBlocks.REGISTRY.register(modEventBus);
        ModBlockEntities.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModEntities.REGISTRY.register(modEventBus);
        ModTabs.REGISTRY.register(modEventBus);
        ModVariables.ATTACHMENT_TYPES.register(modEventBus);

    }

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader,
                                                                 IPayloadHandler<T> handler) {
    }

    public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        if (networkingRegistered)
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
        networkingRegistered = true;
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

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.DEDICATED_SERVER})
    public static class ServerEvents {
        @SubscribeEvent
        public static void init(FMLDedicatedServerSetupEvent event) {
            if (!ModVariables.firstStartup) {
                ModVariables.firstStartup = true;
                NewaycoreMod.LOGGER.info(ModVariables.firstStartup);
            }
            ModVariables.serverType = "server";
            NewaycoreMod.LOGGER.info("Loaded as" + ModVariables.serverType);
        }

    }
}