package ru.newaymc.newaycore.ai.utils;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.objects.Memory;

public interface IShooterSetup extends RangedAttackMob {

    default void buildAi(PathfinderMob mob) {
        Memory memory = new Memory(mob);
        ShooterCore.setup(memory);
    }

    default void tickUpdate(int ticks) {
        ShooterCore.memory.setTicks(ticks);
    }

    default void tickingGoals() {

    }
}
