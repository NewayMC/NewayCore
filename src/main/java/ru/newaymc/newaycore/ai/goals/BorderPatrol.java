package ru.newaymc.newaycore.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ru.newaymc.newaycore.ai.ShooterMain;

import java.util.EnumSet;

public class BorderPatrol extends Goal {
    private static final String CENTER_X = "patrol_center_x";
    private static final String CENTER_Y = "patrol_center_y";
    private static final String CENTER_Z = "patrol_center_z";
    private static final String EDGE = "patrol_edge";
    private static final String T = "patrol_t";
    private final PathfinderMob mob;
    private final double radius;
    private final double speed;
    private final int stepTicks;
    private double centerX, centerY, centerZ;
    private int edge = 0;
    private double t = 0;

    public BorderPatrol(PathfinderMob mob, double radius, double speed, int stepTicks) {
        this.mob = mob;
        this.radius = radius;
        this.speed = speed;
        this.stepTicks = Math.max(1, stepTicks);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ShooterMain.data.isCanBorderPatrol();
    }

    @Override
    public boolean canContinueToUse() {
        if (!ShooterMain.data.isCanBorderPatrol())
            mob.goalSelector.removeGoal(this);
        return ShooterMain.data.isCanBorderPatrol();
    }

    @Override
    public void start() {
        CompoundTag data = mob.getPersistentData();
        if (!data.contains(CENTER_X, Tag.TAG_DOUBLE)) {
            data.putDouble(CENTER_X, mob.getX());
            data.putDouble(CENTER_Y, mob.getY());
            data.putDouble(CENTER_Z, mob.getZ());
        }
        centerX = data.getDouble(CENTER_X);
        centerY = data.getDouble(CENTER_Y);
        centerZ = data.getDouble(CENTER_Z);
        edge = data.getInt(EDGE);
        t = data.getDouble(T);
    }

    @Override
    public void tick() {
        t += 1.0 / stepTicks;
        if (t >= 1.0) {
            t = 0.0;
            edge = (edge + 1) & 3;
        }
        double gx = getXForEdge(edge, t);
        double gz = getZForEdge(edge, t);
        double gy = getYOnBorder(gx, gz);
        if (!isSafeTarget(gx, gy, gz)) {
            Vec3 alt = findAlternative(gx, gy, gz);
            if (alt != null) {
                gx = alt.x;
                gy = alt.y;
                gz = alt.z;
            } else {
                mob.getNavigation().stop();
                return;
            }
        }
        if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(gx, gy, gz, speed);
        }
        Vec3 targetDir = new Vec3(gx - mob.getX(), 0, gz - mob.getZ()).normalize();
        if (targetDir.lengthSqr() > 0.0001) {
            float yaw = (float) (Mth.atan2(targetDir.z, targetDir.x) * 180 / Math.PI) - 90f;
            mob.setYRot(yaw);
            mob.setYBodyRot(yaw);
            mob.yHeadRot = yaw;
            mob.yHeadRotO = yaw;
        }
        maybeJump();
        CompoundTag data = mob.getPersistentData();
        data.putInt(EDGE, edge);
        data.putDouble(T, t);
        ShooterMain.data.setCanBorderPatrol(false);
    }

    private boolean isSafeTarget(double x, double y, double z) {
        for (int dy = 1; dy <= 2; dy++) {
            BlockPos below = BlockPos.containing(x, y - dy, z);
            BlockState belowState = mob.level().getBlockState(below);
            if (belowState.getFluidState().isSource())
                return false;
            if (!belowState.getFluidState().isEmpty())
                return false;
            if (!belowState.isSolid())
                return false;
        }
        BlockPos head = BlockPos.containing(x, y, z);
        BlockState headState = mob.level().getBlockState(head);
        if (!headState.getCollisionShape(mob.level(), head).isEmpty())
            return false;
        double dyTotal = y - mob.getY();
        return dyTotal >= -1.5 && dyTotal <= 1.2;
    }

    private Vec3 findAlternative(double x, double y, double z) {
        double[][] offsets = {{0.8, 0}, {-0.8, 0}, {0, 0.8}, {0, -0.8}, {0.6, 0.6}, {-0.6, 0.6}, {0.6, -0.6}, {-0.6, -0.6}};
        for (double[] o : offsets) {
            double ax = x + o[0];
            double az = z + o[1];
            double ay = getSafeY(ax, az);
            BlockPos check = BlockPos.containing(ax, ay - 1, az);
            BlockState bs = mob.level().getBlockState(check);
            if (bs.getFluidState().isSource())
                continue;
            if (!bs.getFluidState().isEmpty())
                continue;
            if (isSafeTarget(ax, ay, az)) {
                return new Vec3(ax, ay, az);
            }
        }
        return null;
    }

    private double getYOnBorder(double x, double z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(Mth.floor(x), (int) centerY, Mth.floor(z));
        for (int dy = 0; dy < 6; dy++) {
            p.setY((int) centerY - dy);
            BlockState state = mob.level().getBlockState(p);
            if (state.getFluidState().isSource())
                continue;
            if (!state.getFluidState().isEmpty())
                continue;
            if (state.isSolid())
                return p.getY() + 1;
        }
        return centerY;
    }

    private double getSafeY(double x, double z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(Mth.floor(x), (int) centerY, Mth.floor(z));
        for (int dy = 0; dy < 6; dy++) {
            p.setY((int) centerY - dy);
            BlockState state = mob.level().getBlockState(p);
            if (state.getFluidState().isSource())
                continue;
            if (!state.getFluidState().isEmpty())
                continue;
            if (state.isSolid())
                return p.getY() + 1;
        }
        return centerY;
    }

    private void maybeJump() {
        if (!mob.onGround())
            return;
        BlockPos targetPos = mob.getNavigation().getTargetPos();
        if (targetPos == null)
            return;
        Vec3 target = Vec3.atCenterOf(targetPos);
        Vec3 dir = new Vec3(target.x - mob.getX(), 0, target.z - mob.getZ()).normalize();
        if (dir.lengthSqr() < 0.0001)
            return;
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * 180 / Math.PI) - 90f;
        mob.setYRot(yaw);
        mob.setYBodyRot(yaw);
        mob.yHeadRot = yaw;
        mob.yHeadRotO = yaw;
        BlockPos front = BlockPos.containing(mob.getX() + dir.x, mob.getY(), mob.getZ() + dir.z);
        BlockPos above = front.above();
        BlockState frontState = mob.level().getBlockState(front);
        BlockState aboveState = mob.level().getBlockState(above);
        if (!frontState.getCollisionShape(mob.level(), front).isEmpty() && aboveState.getCollisionShape(mob.level(), above).isEmpty()) {
            mob.getJumpControl().jump();
        }
    }

    private double getXForEdge(int edge, double t) {
        switch (edge) {
            case 0:
                return centerX + radius * (1 - 2 * t);
            case 1:
                return centerX - radius;
            case 2:
                return centerX - radius * (1 - 2 * t);
            case 3:
                return centerX + radius;
            default:
                return centerX;
        }
    }

    private double getZForEdge(int edge, double t) {
        switch (edge) {
            case 0:
                return centerZ + radius;
            case 1:
                return centerZ + radius * (1 - 2 * t);
            case 2:
                return centerZ - radius;
            case 3:
                return centerZ - radius * (1 - 2 * t);
            default:
                return centerZ;
        }
    }
}
