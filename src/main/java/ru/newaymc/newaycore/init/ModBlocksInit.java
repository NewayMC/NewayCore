package ru.newaymc.newaycore.init;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.block.CoverMarkerAI;
import ru.newaymc.newaycore.block.EmptyBlock;
import ru.newaymc.newaycore.block.ObjectMarkerAI;
import ru.newaymc.newaycore.block.OutpostHub;

public class ModBlocksInit {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, NewaycoreMod.MODID);
    public static final RegistryObject<Block> COVER_MARKER_AI;
    public static final RegistryObject<Block> OBJECT_MARKER_AI;
    public static final RegistryObject<Block> EMPTY_BLOCK;
    public static final RegistryObject<Block> OUTPOST_HUB;

    static {
        COVER_MARKER_AI = REGISTRY.register("cover_marker_ai", CoverMarkerAI::new);
        OBJECT_MARKER_AI = REGISTRY.register("object_marker_ai", ObjectMarkerAI::new);
        EMPTY_BLOCK = REGISTRY.register("empty_block", EmptyBlock::new);
        OUTPOST_HUB = REGISTRY.register("outpost_hub", OutpostHub::new);
    }
}