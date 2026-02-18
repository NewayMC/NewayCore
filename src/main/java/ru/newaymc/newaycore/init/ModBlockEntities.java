package ru.newaymc.newaycore.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.block.entity.OutpostHubBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NewaycoreMod.MODID);
    public static final RegistryObject<BlockEntityType<OutpostHubBlockEntity>> OUTPOST_HUB = register("outpost_hub", ModBlocksInit.OUTPOST_HUB, OutpostHubBlockEntity::new);

    // Start of user code block custom block entities
    // End of user code block custom block entities
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
        return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

}
