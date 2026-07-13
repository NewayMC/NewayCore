package ru.newaymc.newaycore.ai.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import ru.newaymc.newaycore.ai.ShooterMain;

public interface IShooterSetup extends RangedAttackMob {

    default void aiSetup(Entity entity) {
        ShooterMain.setup(entity);
    }

    default boolean getAllowAttack() {
        return ShooterMain.data.isAllowAttack();
    }

    default void tickingGoals(int ticks) {
    }
}
