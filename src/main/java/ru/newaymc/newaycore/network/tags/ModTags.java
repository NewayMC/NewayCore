package ru.newaymc.newaycore.network.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import ru.newaymc.newaycore.NewaycoreMod;

public class ModTags {
    public static final TagKey<Block> TERRAIN = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "terrain"));
}
