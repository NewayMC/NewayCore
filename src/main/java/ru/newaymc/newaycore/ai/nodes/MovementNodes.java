package ru.newaymc.newaycore.ai.nodes;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.goals.BorderPatrol;
import ru.newaymc.newaycore.ai.goals.SmartCover;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Interruptible;
import ru.newaymc.newaycore.ai.utils.Node;
import ru.newaymc.newaycore.ai.utils.Status;

import java.util.Random;

public class MovementNodes {
    public static class MoveToCoverNode extends Node {
        private boolean pathFound = false;

        @Override
        public Status tick(Memory memory) {
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }
            PathfinderMob mob = memory.getMob();
            PathNavigation nav = mob.getNavigation();
            if (!pathFound) {
                SmartCover.init(mob, mob.position(), memory.getTarget());
                if (SmartCover.getStatus()) {
                    pathFound = true;
                } else {
                    return Status.FAILURE;
                }
                memory.setCoverStatus(SmartCover.getStatus());

                double dist = SmartCover.getBestCover().getDistance();
                if (dist <= 0.05) {
                    pathFound = false;
                    mob.setPos(SmartCover.getBestCover().getVec3());
                    memory.setCurrentCover(SmartCover.getBestCover());
                    return Status.SUCCESS;
                } else {
                    memory.setCurrentCover(null);
                }
            }

            Vec3 coverPos = SmartCover.getBestCover().getVec3();
            nav.moveTo(coverPos.x(), coverPos.y(), coverPos.z(), 1);
            return Status.RUNNING;
        }
    }

    public static class PatrolNode extends Node implements Interruptible {
        private static PathfinderMob mob;
        private static final Goal goal = new BorderPatrol(mob, 16, 1, 60);

        @Override
        public Status tick(Memory memory) {
            mob = memory.getMob();
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }
            if (!ShooterCore.memory.isBorderPatrol()) {
                return Status.SUCCESS;
            }

            mob.goalSelector.addGoal(1, goal);
            return Status.RUNNING;
        }

        @Override
        public void onInterrupt(Memory memory) {
            mob.goalSelector.removeGoal(goal);
        }
    }

    /*public static class StrayNode extends Node {

        @Override
        public Status tick(Memory memory) {
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }
            PathfinderMob mob = memory.getMob();
            PathNavigation nav = mob.getNavigation();
            if (!memory.isCoverStatus() && memory.getTicks() % 40 == 0) {
                int rnd = new Random().nextInt(1, 4);
                int move = new Random().nextInt(3, 5);

                switch (rnd) {
                    case 1:
                        nav.moveTo(mob.getX(), mob.getY(), mob.getZ() - move, 1);
                        break;
                    case 2:
                        nav.moveTo(mob.getX(), mob.getY(), mob.getZ() + move, 1);
                        break;
                    case 3:
                        nav.moveTo(mob.getX() + move, mob.getY(), mob.getZ(), 1);
                        break;
                    case 4:
                        nav.moveTo(mob.getX() - move, mob.getY(), mob.getZ() + move, 1);
                        break;
                }
            } else {
                 return Status.FAILURE;
            }
            return Status.RUNNING;
        }
    }*/
}
