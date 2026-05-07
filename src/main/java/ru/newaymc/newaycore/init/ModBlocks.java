package ru.newaymc.newaycore.init;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.block.CoverMarkerAIBlock;
import ru.newaymc.newaycore.block.EmptyBlockBlock;
import ru.newaymc.newaycore.block.OutpostHubBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(NewaycoreMod.MODID);
    public static final DeferredBlock<Block> COVER_MARKER_AI;
    public static final DeferredBlock<Block> EMPTY_BLOCK;
    public static final DeferredBlock<Block> OUTPOST_HUB;

    static {
        COVER_MARKER_AI = REGISTRY.register("cover_marker_ai", CoverMarkerAIBlock::new);
        EMPTY_BLOCK = REGISTRY.register("empty_block", EmptyBlockBlock::new);
        OUTPOST_HUB = REGISTRY.register("outpost_hub", OutpostHubBlock::new);
    }
    // Start of user code block custom blocks
    // End of user code block custom blocks
}