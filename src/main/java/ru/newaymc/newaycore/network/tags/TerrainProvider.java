package ru.newaymc.newaycore.network.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import ru.newaymc.newaycore.NewaycoreMod;

import java.util.concurrent.CompletableFuture;

public class TerrainProvider extends BlockTagsProvider {
    public TerrainProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, NewaycoreMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.TERRAIN).add(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.MUD, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.WATER)
                .addTag(BlockTags.UNDERWATER_BONEMEALS);
    }
}
