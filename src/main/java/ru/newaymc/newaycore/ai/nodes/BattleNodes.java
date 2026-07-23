package ru.newaymc.newaycore.ai.nodes;

import net.minecraft.world.entity.LivingEntity;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Node;
import ru.newaymc.newaycore.ai.utils.Status;

@Deprecated
public class BattleNodes {
    public static class CanSeeEnemyNode extends Node {

        @Override
        public Status tick(Memory memory) {
            //ShooterCore.targetDetection();
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }

            if (memory.isSeeTarget()) {
                memory.setLastTargetPos(memory.getTarget().position());
                memory.setLastSeenTime(System.currentTimeMillis());
                return Status.SUCCESS;
            } else {
                return Status.FAILURE;
            }
        }
    }

    public static class AttackNode extends Node {

        @Override
        public Status tick(Memory memory) {
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }
            LivingEntity target = memory.getTarget();
            long lastSeen = memory.getLastSeenTime();

            if (target == null || System.currentTimeMillis() - lastSeen > 2000) {
                return Status.FAILURE;
            }

            if (memory.isSeeTarget()) {
                memory.setLastTargetPos(memory.getTarget().position());
                memory.setLastSeenTime(System.currentTimeMillis());
            }

            //ShooterCore.gunAttack();
            return Status.RUNNING;
        }
    }
}
