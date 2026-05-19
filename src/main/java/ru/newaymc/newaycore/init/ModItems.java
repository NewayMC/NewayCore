package ru.newaymc.newaycore.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.gun.item.AR15SniperItem;
import ru.newaymc.newaycore.gun.item.AkmItem;
import ru.newaymc.newaycore.gun.item.M4A1Item;
import ru.newaymc.newaycore.gun.item.MP5Item;

public class ModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(NewaycoreMod.MODID);
    public static final DeferredItem<Item> COVER_MARKER_AI;
    public static final DeferredItem<Item> AKM;
    public static final DeferredItem<Item> M_4_A_1;
    public static final DeferredItem<Item> MP_5;
    public static final DeferredItem<Item> AR_15_SNIPER;
    public static final DeferredItem<Item> EMPTY_BLOCK;
    public static final DeferredItem<Item> OUTPOST_HUB;
    public static final DeferredItem<Item> SHOOTER_AI_ENTITY_SPAWN_EGG;

    static {
        COVER_MARKER_AI = block(ModBlocks.COVER_MARKER_AI);
        AKM = REGISTRY.register("akm", AkmItem::new);
        M_4_A_1 = REGISTRY.register("m_4_a_1", M4A1Item::new);
        MP_5 = REGISTRY.register("mp_5", MP5Item::new);
        AR_15_SNIPER = REGISTRY.register("ar_15_sniper", AR15SniperItem::new);
        EMPTY_BLOCK = block(ModBlocks.EMPTY_BLOCK);
        OUTPOST_HUB = block(ModBlocks.OUTPOST_HUB);
        SHOOTER_AI_ENTITY_SPAWN_EGG = REGISTRY.register("standart_shooter_entity_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.SHOOTER_AI_ENTITY, -13421773, -10066330, new Item.Properties()));
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}