package ru.newaymc.newaycore.worlds.chunks;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import ru.newaymc.newaycore.mixins.ChunkMapAccessor;

public class ChunkLoader {

    public static void reloadChunk(ServerLevel level, ChunkPos pos) {
        ServerChunkCache cache = level.getChunkSource();
        long key = pos.toLong();

        ChunkMapAccessor accessor = (ChunkMapAccessor) cache.chunkMap;
        accessor.getUpdatingChunkMap().remove(key);
        accessor.getPendingUnloads().remove(key);
        accessor.getToDrop().remove(key);
        accessor.getChunkTypeCache().remove(key);
        accessor.setModified(true);

        ChunkAccess chunk = cache.getChunk(
                pos.x, pos.z,
                ChunkStatus.FULL,
                true  // requireChunk = true
        );
    }

    public static void reloadRegion(ServerLevel level, int regionX, int regionZ) {
        int baseX = regionX << 5; // * 32
        int baseZ = regionZ << 5;
        for (int dx = 0; dx < 32; dx++) {
            for (int dz = 0; dz < 32; dz++) {
                reloadChunk(level, new ChunkPos(baseX + dx, baseZ + dz));
            }
        }
    }
}
