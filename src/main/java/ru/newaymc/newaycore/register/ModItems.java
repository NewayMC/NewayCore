package ru.newaymc.newaycore.register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;

public class ModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(NewaycoreMod.MODID);
    public static final DeferredItem<Item> EMPTY_BLOCK;

    static {
        EMPTY_BLOCK = block(ModBlocks.EMPTY_BLOCK);
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}