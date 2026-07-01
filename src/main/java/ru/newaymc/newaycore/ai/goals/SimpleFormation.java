package ru.newaymc.newaycore.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;
import ru.newaymc.newaycore.ai.ShooterMain;

import java.util.EnumSet;
import java.util.List;

public class SimpleFormation extends Goal {
    private final PathfinderMob mob;
    private final PathfinderMob commander;
    private final int maxUnits;
    private final double spacing;
    private final double speed;
    private boolean initialized = false;
    private int slotIndex = -1;
    private Vec3 targetPos;
    private boolean arrived = false;
    private Vec3 commanderInitPos;
    private float commanderInitYaw;

    public SimpleFormation(PathfinderMob mob, PathfinderMob commander, int maxUnits, double spacing, double speed) {
        this.mob = mob;
        this.commander = commander;
        this.maxUnits = maxUnits;
        this.spacing = spacing;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ShooterMain.data.isSimpleFormation();
    }

    @Override
    public boolean canContinueToUse() {
        if (!ShooterMain.data.isSimpleFormation())
            mob.goalSelector.removeGoal(this);
        return ShooterMain.data.isSimpleFormation();
    }

    @Override
    public void start() {
        if (!initialized) {
            int idx = commander.getPersistentData().getInt("formationCount");
            commander.getPersistentData().putInt("formationCount", idx + 1);
            slotIndex = idx;
            mob.getPersistentData().putInt("formationSlot", slotIndex);
            commanderInitPos = commander.position();
            commanderInitYaw = commander.getYRot();
            targetPos = calculateSlotPosition(slotIndex);
            initialized = true;
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        commanderInitPos = null;
        commanderInitYaw = 0;
    }

    @Override
    public void tick() {
        ShooterMain.data.setSimpleFormation(false);
        targetPos = findFreeSlot(targetPos);
        double dist = mob.position().distanceTo(targetPos);
        if (dist <= 0.05) {
            mob.setPos(targetPos.x, targetPos.y, targetPos.z);
            arrived = true;
            alignWithCommanderLook();
        } else {
            arrived = false;
            PathNavigation nav = mob.getNavigation();
            if (nav.isDone()) {
                nav.moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
            } else {
                nav.tick();
            }
            rotateTowardsCommanderGradual();
        }
        mob.getPersistentData().putBoolean("simpleFormationArrived", arrived);
    }

    private Vec3 calculateSlotPosition(int index) {
        int unitsPerRow = Math.min(maxUnits, 10);
        int row = index / unitsPerRow;
        int col = index % unitsPerRow;
        double dx = (col - (unitsPerRow - 1) * 0.5) * spacing;
        double dz = 2.0 + row * spacing;
        double yawRad = Math.toRadians(commanderInitYaw);
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        Vec3 offset = forward.scale(dz).add(right.scale(dx));
        Vec3 pos = commanderInitPos.add(offset);
        return new Vec3(Math.floor(pos.x) + 0.5, getSafeY(pos.x, pos.z), Math.floor(pos.z) + 0.5);
    }

    private Vec3 findFreeSlot(Vec3 target) {
        BlockPos base = BlockPos.containing(target);
        BlockPos[] offsets = new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, -1), new BlockPos(2, 0, 0), new BlockPos(-2, 0, 0), new BlockPos(0, 0, 2), new BlockPos(0, 0, -2)};
        List<PathfinderMob> allies = mob.level().getEntitiesOfClass(PathfinderMob.class, mob.getBoundingBox().inflate(50), e -> e != mob && e.getClass() == mob.getClass() && e.getPersistentData().getBoolean("simpleFormation"));
        for (BlockPos off : offsets) {
            BlockPos check = base.offset(off);
            boolean free = true;
            for (PathfinderMob ally : allies) {
                if (!ally.isDeadOrDying() && BlockPos.containing(ally.position()).equals(check)) {
                    free = false;
                    break;
                }
            }
            if (free) {
                return new Vec3(check.getX() + 0.5, getSafeY(check.getX(), check.getZ()), check.getZ() + 0.5);
            }
        }
        return target;
    }

    private void rotateTowardsCommanderGradual() {
        float yawDiff = (commander.getYRot() - mob.getYRot() + 540) % 360 - 180;
        mob.setYRot(mob.getYRot() + yawDiff * 0.2F);
        mob.yBodyRot = mob.getYRot();
        mob.yHeadRot += (commander.yHeadRot - mob.yHeadRot) * 0.2F;
        mob.setXRot(mob.getXRot() + (commander.getXRot() - mob.getXRot()) * 0.2F);
    }

    private void alignWithCommanderLook() {
        mob.setYRot(commander.getYRot());
        mob.setXRot(commander.getXRot());
        mob.yBodyRot = commander.yBodyRot;
        mob.yHeadRot = commander.yHeadRot;
    }

    private double getSafeY(double x, double z) {
        BlockPos base = BlockPos.containing(x, mob.getY(), z);
        for (int dy = -2; dy <= 2; dy++) {
            BlockPos p = base.offset(0, dy, 0);
            if (!mob.level().getBlockState(p).isSolid() && mob.level().getBlockState(p.below()).isSolid())
                return p.getY();
        }
        return mob.getY();
    }
}
