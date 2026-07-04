package ru.newaymc.newaycore.network.vars;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ru.newaymc.newaycore.NewaycoreMod;

import java.io.File;

@EventBusSubscriber
public class ModVariables {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NewaycoreMod.MODID);
    public static String serverType = "";
    public static boolean firstStartup = false;

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        NewaycoreMod.addNetworkMessage(SavedDataSyncMessage.TYPE, SavedDataSyncMessage.STREAM_CODEC, SavedDataSyncMessage::handleData);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SavedData mapdata = MapVariables.get(event.getEntity().level());
            SavedData worlddata = WorldVariables.get(event.getEntity().level());
            if (mapdata != null)
                PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(0, mapdata));
            if (worlddata != null)
                PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SavedData worlddata = WorldVariables.get(event.getEntity().level());
            if (worlddata != null)
                PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(1, worlddata));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldVariables worldVariables = WorldVariables.get(level);
            if (worldVariables._syncDirty) {
                PacketDistributor.sendToPlayersInDimension(level, new SavedDataSyncMessage(1, worldVariables));
                worldVariables._syncDirty = false;
            }
            MapVariables mapVariables = MapVariables.get(level);
            if (mapVariables._syncDirty) {
                PacketDistributor.sendToAllPlayers(new SavedDataSyncMessage(0, mapVariables));
                mapVariables._syncDirty = false;
            }
        }
    }

    public static class WorldVariables extends SavedData {
        public static final String DATA_NAME = "newaycore_worldvars";
        static WorldVariables clientSide = new WorldVariables();
        boolean _syncDirty = false;

        public static WorldVariables load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
            WorldVariables data = new WorldVariables();
            data.read(tag, lookupProvider);
            return data;
        }

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(new Factory<>(WorldVariables::new, WorldVariables::load), DATA_NAME);
            } else {
                return clientSide;
            }
        }

        public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
            return nbt;
        }

        public void markSyncDirty() {
            this.setDirty();
            this._syncDirty = true;
        }
    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "newaycore_mapvars";
        static MapVariables clientSide = new MapVariables();
        public boolean firstJoin = false;
        boolean _syncDirty = false;

        public static MapVariables load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
            MapVariables data = new MapVariables();
            data.read(tag, lookupProvider);
            return data;
        }

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new Factory<>(MapVariables::new, MapVariables::load), DATA_NAME);
            } else {
                return clientSide;
            }
        }

        public void read(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
            firstJoin = nbt.getBoolean("firstJoin");
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
            nbt.putBoolean("firstJoin", firstJoin);
            return nbt;
        }

        public void markSyncDirty() {
            this.setDirty();
            _syncDirty = true;
        }
    }

    public record SavedDataSyncMessage(int dataType, SavedData data) implements CustomPacketPayload {
        public static final Type<SavedDataSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "saved_data_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, SavedDataSyncMessage message) -> {
            buffer.writeInt(message.dataType);
            if (message.data != null)
                buffer.writeNbt(message.data.save(new CompoundTag(), buffer.registryAccess()));
        }, (RegistryFriendlyByteBuf buffer) -> {
            int dataType = buffer.readInt();
            CompoundTag nbt = buffer.readNbt();
            SavedData data = null;
            if (nbt != null) {
                data = dataType == 0 ? new MapVariables() : new WorldVariables();
                if (data instanceof MapVariables mapVariables)
                    mapVariables.read(nbt, buffer.registryAccess());
                else if (data instanceof WorldVariables worldVariables)
                    worldVariables.read(nbt, buffer.registryAccess());
            }
            return new SavedDataSyncMessage(dataType, data);
        });

        public static void handleData(final SavedDataSyncMessage message, final IPayloadContext context) {
            if (context.flow() == PacketFlow.CLIENTBOUND && message.data != null) {
                context.enqueueWork(() -> {
                    if (message.dataType == 0)
                        MapVariables.clientSide.read(message.data.save(new CompoundTag(), context.player().registryAccess()), context.player().registryAccess());
                    else
                        WorldVariables.clientSide.read(message.data.save(new CompoundTag(), context.player().registryAccess()), context.player().registryAccess());
                }).exceptionally(e -> {
                    context.connection().disconnect(Component.literal(e.getMessage()));
                    return null;
                });
            }
        }

        @Override
        public Type<SavedDataSyncMessage> type() {
            return TYPE;
        }
    }
}