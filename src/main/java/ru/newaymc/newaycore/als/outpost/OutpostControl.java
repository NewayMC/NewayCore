package ru.newaymc.newaycore.als.outpost;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

@Deprecated(forRemoval = true)
public class OutpostControl {
    public static void execute(LevelAccessor world, double x, double y, double z) {
//        boolean UnderSecurity = false;
//        boolean HaveSquad = false;
//        Entity OutpostSecurity = null;
//        Entity cmd = null;
//        double rnd = 0;
//        if (world.getEntitiesOfClass(ShooterAiEntity.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(16 / 2d), e -> true).isEmpty() && world.dayTime() == 1000) {
//            UnderSecurity = false;
//        }
//        if (!UnderSecurity) {
//            for (int index0 = 0; index0 < (int) getBlockNBTNumber(world, BlockPos.containing(x, y, z), "security-count"); index0++) {
//                OutpostSecurity = world instanceof ServerLevel _level3 ? ModEntities.STANDART_SHOOTER_ENTITY.get().spawn(_level3, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED) : null;
//                if (OutpostSecurity instanceof ShooterAiEntity _datEntSetL)
//                    _datEntSetL.getEntityData().set(ShooterAiEntity.DATA_CanBeInSquad, false);
//            }
//            UnderSecurity = true;
//        }
//        if (!HaveSquad && world.dayTime() == 1000) {
//            cmd = world instanceof ServerLevel _level6 ? ModEntities.ELITE_SHOOTER_ENTITY.get().spawn(_level6, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED) : null;
//            for (int index1 = 0; index1 < 4; index1++) {
//                if (world instanceof ServerLevel _level) {
//                    Entity entityToSpawn = ModEntities.STANDART_SHOOTER_ENTITY.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
//                    if (entityToSpawn != null) {
//                    }
//                }
//            }
//        }
    }

    private static double getBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null)
            return blockEntity.getPersistentData().getDouble(tag);
        return -1;
    }
}
