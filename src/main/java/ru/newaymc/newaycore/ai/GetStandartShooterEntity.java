package ru.newaymc.newaycore.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import ru.newaymc.newaycore.ai.engine.ShooterMain;

public class GetStandartShooterEntity {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        ShooterMain.BattleAI.init(world, x, y, z, entity, entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_ammunation) : 0,
                entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_damage) : 0,
                entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_inaccurace_accumulation) : 0,
                entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_recoil) : 0,
                entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_recovery_time) : 0,
                entity instanceof StandartShooterEntity _datEntI ? _datEntI.getEntityData().get(StandartShooterEntity.DATA_speed) : 0,
                entity instanceof StandartShooterEntity _datEntS ? _datEntS.getEntityData().get(StandartShooterEntity.DATA_ai_type) : "");
    }
}
