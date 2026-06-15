package ru.newaymc.newaycore;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import ru.newaymc.newaycore.network.tags.TerrainProvider;
import ru.newaymc.newaycore.register.*;
import ru.newaymc.newaycore.network.vars.ModVariables;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mod("newaycore")
public class NewaycoreMod {
    public static final Logger LOGGER = LogUtils.getLogger();
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

    @EventBusSubscriber
    public static class CommonSetup {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {

        }
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {

    }

    @EventBusSubscriber
    public static class GatherEvent {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            PackOutput output = event.getGenerator().getPackOutput();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

            event.getGenerator().addProvider(
                    event.includeServer(),
                    new TerrainProvider(output, lookupProvider, existingFileHelper)
            );
        }
    }

    @EventBusSubscriber(value = {Dist.DEDICATED_SERVER})
    public static class ServerEvents {
        @SubscribeEvent
        public static void init(FMLDedicatedServerSetupEvent event) {
            if (!ModVariables.firstStartup) {
                ModVariables.firstStartup = true;
            }
            ModVariables.serverType = "server";
            NewaycoreMod.LOGGER.info("Loaded as" + ModVariables.serverType);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void init(FMLClientSetupEvent event) {
            ModVariables.serverType = "client";
            NewaycoreMod.LOGGER.info("Loaded as " + ModVariables.serverType);
        }
    }

    @EventBusSubscriber
    public static class PlayerLoggedIn {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            execute(event, event.getEntity().level());
        }

        public static void execute(LevelAccessor world) {
            execute(null, world);
        }

        private static void execute(@Nullable Event event, LevelAccessor world) {
            if (ModVariables.serverType.equals("client")) {
                if (!ModVariables.MapVariables.get(world).firstJoin) {
                    ModVariables.MapVariables.get(world).firstJoin = true;
                }
            }
        }
    }
}