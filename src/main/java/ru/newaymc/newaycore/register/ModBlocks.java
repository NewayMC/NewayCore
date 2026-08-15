package ru.newaymc.newaycore.register;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.block.EmptyBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(NewaycoreMod.MODID);
    public static final DeferredBlock<Block> EMPTY_BLOCK;

    static {
        EMPTY_BLOCK = REGISTRY.register("empty_block", EmptyBlock::new);
    }
}