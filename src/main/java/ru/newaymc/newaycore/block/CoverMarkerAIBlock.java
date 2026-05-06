package ru.newaymc.newaycore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;

public class CoverMarkerAIBlock extends Block {
    public CoverMarkerAIBlock() {
        super(Properties.of().sound(SoundType.EMPTY).strength(-1, 3600000).noCollission().pushReaction(PushReaction.IGNORE));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 5;
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
        return PathType.OPEN;
    }
}