package ru.newaymc.newaycore.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import ru.newaymc.newaycore.ai.engine.ShooterMain;

public class GetEliteShooterEntity {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        ShooterMain.BattleAI.init(world, x, y, z, entity, entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_ammunation) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_damage) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_inaccurace_accumulation) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_recoil) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_recovery_time) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_shoot) : 0,
                entity instanceof EliteShooterEntity _datEntI ? _datEntI.getEntityData().get(EliteShooterEntity.DATA_speed) : 0,
                entity instanceof EliteShooterEntity _datEntS ? _datEntS.getEntityData().get(EliteShooterEntity.DATA_ai_type) : "");
    }
}