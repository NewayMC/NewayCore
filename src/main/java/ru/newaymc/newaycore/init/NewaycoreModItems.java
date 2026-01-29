/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package ru.newaymc.newaycore.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.item.AR15SniperItem;
import ru.newaymc.newaycore.item.AkmItem;
import ru.newaymc.newaycore.item.M4A1Item;
import ru.newaymc.newaycore.item.MP5Item;

public class NewaycoreModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, NewaycoreMod.MODID);
    public static final RegistryObject<Item> ELITE_SHOOTER_ENTITY_SPAWN_EGG;
    public static final RegistryObject<Item> COVER_MARKER_AI;
    public static final RegistryObject<Item> OBJECT_MARKER_AI;
    public static final RegistryObject<Item> AKM;
    public static final RegistryObject<Item> M_4_A_1;
    public static final RegistryObject<Item> MP_5;
    public static final RegistryObject<Item> AR_15_SNIPER;
    public static final RegistryObject<Item> EMPTY_BLOCK;

    static {
        ELITE_SHOOTER_ENTITY_SPAWN_EGG = REGISTRY.register("elite_shooter_entity_spawn_egg", () -> new ForgeSpawnEggItem(NewaycoreModEntities.ELITE_SHOOTER_ENTITY, -13421773, -10066330, new Item.Properties()));
        COVER_MARKER_AI = block(NewaycoreModBlocks.COVER_MARKER_AI);
        OBJECT_MARKER_AI = block(NewaycoreModBlocks.OBJECT_MARKER_AI);
        AKM = REGISTRY.register("akm", AkmItem::new);
        M_4_A_1 = REGISTRY.register("m_4_a_1", M4A1Item::new);
        MP_5 = REGISTRY.register("mp_5", MP5Item::new);
        AR_15_SNIPER = REGISTRY.register("ar_15_sniper", AR15SniperItem::new);
        EMPTY_BLOCK = block(NewaycoreModBlocks.EMPTY_BLOCK);
    }

    // Start of user code block custom items
    // End of user code block custom items
    private static RegistryObject<Item> block(RegistryObject<Block> block) {
        return block(block, new Item.Properties());
    }

    private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}