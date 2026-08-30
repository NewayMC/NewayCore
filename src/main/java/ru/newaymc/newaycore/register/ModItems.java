package ru.newaymc.newaycore.register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.register.ModEntities;

public class ModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(NewaycoreMod.MODID);
    public static final DeferredItem<Item> EMPTY_BLOCK;
    public static final DeferredItem<Item> SHOOTER_AI_ENTITY_SPAWN_EGG;

    static {
        EMPTY_BLOCK = block(ModBlocks.EMPTY_BLOCK);
        SHOOTER_AI_ENTITY_SPAWN_EGG = REGISTRY.register("shooter_ai_entity_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.SHOOTER_AI_ENTITY, -13421773, -10066330, new Item.Properties()));
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}