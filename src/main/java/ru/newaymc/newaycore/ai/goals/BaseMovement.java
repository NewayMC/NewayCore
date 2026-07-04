package ru.newaymc.newaycore.ai.goals;

import com.google.common.annotations.Beta;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import ru.newaymc.newaycore.ai.ShooterMain;

import java.util.Random;

@Beta
public class BaseMovement extends Goal {

    private final PathfinderMob mob;
    private final double x;
    private final double y;
    private final double z;

    public BaseMovement(PathfinderMob mob, double x, double y, double z) {
        this.mob = mob;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean canUse() {
        return ShooterMain.data.isBaseMovement();
    }

    @Override
    public boolean canContinueToUse() {
        if (!ShooterMain.data.isBaseMovement()) {
            mob.goalSelector.removeGoal(this);
        }
        return ShooterMain.data.isBaseMovement();
    }

    @Override
    public void start() {
        PathNavigation nav = mob.getNavigation();
        int rnd = new Random().nextInt(1, 4);
        int move = new Random().nextInt(3, 5);

        switch (rnd) {
            // SOUTH
            case 1:
                nav.moveTo(x, y, z - move, 1);
                stop();
                // NORTH
            case 2:
                nav.moveTo(x, y, z + move, 1);
                stop();
                // WEST
            case 3:
                nav.moveTo(x + move, y, z, 1);
                stop();
                // EAST
            case 4:
                nav.moveTo(x - move, y, z, 1);
                stop();
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        ShooterMain.data.setBaseMovement(false);
    }
}
