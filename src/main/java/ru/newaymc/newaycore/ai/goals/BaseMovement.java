package ru.newaymc.newaycore.ai.goals;

import com.google.common.annotations.Beta;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import ru.newaymc.newaycore.ai.ShooterMain;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Beta
public class BaseMovement extends Goal {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long cd;

    private final PathfinderMob mob;
    private final double x;
    private final double y;
    private final double z;

    public BaseMovement(PathfinderMob mob, double x, double y, double z, int cooldown) {
        this.mob = mob;
        this.x = x;
        this.y = y;
        this.z = z;
        this.cd = cooldown;
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
    public void stop() {
        scheduler.shutdown();
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        PathNavigation nav = mob.getNavigation();
        int rnd = new Random().nextInt(1, 4);
        int move = new Random().nextInt(3, 5);

        scheduler.scheduleAtFixedRate(() -> {
            switch (rnd) {
                // SOUTH
                case 1:
                    nav.moveTo(x, y, z - move, 1.2);
                    // NORTH
                case 2:
                    nav.moveTo(x, y, z + move, 1.2);
                    // WEST
                case 3:
                    nav.moveTo(x + move, y, z, 1.2);
                    // EAST
                case 4:
                    nav.moveTo(x - move, y, z, 1.2);
            }
        }, 0, cd, TimeUnit.SECONDS);
    }
}
