package ru.newaymc.newaycore.mixins;

import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {

    @Accessor("updatingChunkMap")
    it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<net.minecraft.server.level.ChunkHolder> getUpdatingChunkMap();

    @Accessor("chunkTypeCache")
    it.unimi.dsi.fastutil.longs.Long2ByteMap getChunkTypeCache();

    @Accessor("pendingUnloads")
    it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<net.minecraft.server.level.ChunkHolder> getPendingUnloads();

    @Accessor("toDrop")
    it.unimi.dsi.fastutil.longs.LongSet getToDrop();

    @Accessor("modified")
    void setModified(boolean modified);
}
