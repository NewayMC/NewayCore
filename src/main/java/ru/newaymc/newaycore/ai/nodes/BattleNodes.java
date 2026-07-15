package ru.newaymc.newaycore.ai.nodes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Node;
import ru.newaymc.newaycore.ai.utils.Status;
import ru.newaymc.newaycore.gun.GunSetup;

import java.util.function.Supplier;

public class BattleNodes {
    public static class CanSeeEnemyNode extends Node {

        @Override
        public Status tick(Memory memory) {
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
                ShooterCore.LOGGER.debug("Target: {}, see target {}", memory.getTarget(), memory.isSeeTarget());
            }
            PathfinderMob mob = memory.getMob();
            LivingEntity target = memory.getTarget();

            if (target == null) return Status.FAILURE;
            return memory.isSeeTarget() ? Status.SUCCESS : Status.FAILURE;
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

            ShooterCore.allowAttack();
            return Status.RUNNING;
        }
    }

    public static class ReloadNode extends Node {

        @Override
        public Status tick(Memory memory) {
            if (ShooterCore.debug) {
                ShooterCore.LOGGER.debug("Current node: {}", this.getClass().getName());
            }
            PathfinderMob entity = memory.getMob();
            if (((Supplier<Boolean>) (() -> {
                boolean boly = (boolean) GunSetup.GunUtils.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_RELOADING);
                GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_RELOADING, false);
                return boly;
            })).get()) {
                return Status.RUNNING;
            }

            ShooterCore.reload();
            return Status.SUCCESS;
        }
    }
}
