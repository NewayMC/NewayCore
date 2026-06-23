package ru.newaymc.newaycore.ai.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GoalsExtension {

    public static class BorderPatrolGoal extends Goal {
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

        public BorderPatrolGoal(PathfinderMob mob, double radius, double speed, int stepTicks) {
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

    public static class SimpleFormationGoal extends Goal {
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

        public SimpleFormationGoal(PathfinderMob mob, PathfinderMob commander, int maxUnits, double spacing, double speed) {
            this.mob = mob;
            this.commander = commander;
            this.maxUnits = maxUnits;
            this.spacing = spacing;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ShooterMain.data.isCanSimpleFormation();
        }

        @Override
        public boolean canContinueToUse() {
            if (!ShooterMain.data.isCanSimpleFormation())
                mob.goalSelector.removeGoal(this);
            return ShooterMain.data.isCanSimpleFormation();
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
            ShooterMain.data.setCanSimpleFormation(false);
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

    /**
     * <a href="https://github.com/NewayMC/NewayCore/issues/1">Issue</a>
     */
    @Deprecated
    public static class SmartCover extends Goal {
        private final PathfinderMob mob;
        private static LevelAccessor world;
        private static Vec3 nextPos;
        private static Vec3 pos;

        public SmartCover(PathfinderMob mob, double x, double y, double z, LevelAccessor world) {
            this.mob = mob;
            this.pos = new Vec3(x, y, z);
            this.nextPos = new Vec3(x, y, z);
            this.world = world;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return ShooterMain.data.isCanFindCover();
        }

        @Override
        public boolean canContinueToUse() {
            if (!ShooterMain.data.isCanFindCover())
                mob.goalSelector.removeGoal(this);
            return ShooterMain.data.isCanFindCover();
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            ShooterMain.data.setCanFindCover(false);
            nextPos = null;
        }

        @Override
        public void tick() {
            nextPos = findCover();
            if (nextPos != null) {
                double dist = mob.position().distanceTo(nextPos);
                if (dist <= 0.05) {
                    mob.setPos(nextPos.x, nextPos.y, nextPos.z);
                    stop();
                } else {
                    PathNavigation nav = mob.getNavigation();
                    if (nav.isDone()) {
                        nav.moveTo(nextPos.x, nextPos.y, nextPos.z, 1.2);
                    } else {
                        nav.tick();
                    }
                }
            } else {
                stop();
            }
        }

        private Vec3 findCover() {
            Vec3 coverPos = null;
            double sX = 0;
            double sZ = 0;

            sX = -3;
            for (int index0 = 0; index0 < 16; index0++) {
                sZ = -3;
                for (int index1 = 0; index1 < 16; index1++) {
                    if (!(world.getBlockState(BlockPos.containing(pos.x + sX, pos.y, pos.z + sZ))).is(BlockTags.create(ResourceLocation.fromNamespaceAndPath("newaycore", "terrain")))) {
                        coverPos = directionMath(new Vec3(pos.x + sX, pos.y, pos.z + sZ), mob.getDirection(), 1);
                        break;
                    } else {
                        stop();
                    }
                }
            }
            while (coverPos !=null && !world.getBlockState(BlockPos.containing(coverPos)).canOcclude()) {
                coverPos = directionMath(coverPos, mob.getDirection(), 1);
                break;
            }

            return coverPos;
        }

        private Vec3 directionMath(Vec3 vec3, Direction direction, int i) {
            if (direction == Direction.SOUTH) {
                vec3 = new Vec3(vec3.x, vec3.y, vec3.z - i);

            } else if (direction == Direction.NORTH) {
                vec3 = new Vec3(vec3.x, vec3.y, vec3.z + i);

            } else if (direction == Direction.WEST) {
                vec3 = new Vec3(vec3.x + i, vec3.y, vec3.z);

            } else if (direction == Direction.EAST) {
                vec3 = new Vec3(vec3.x - i, vec3.y, vec3.z);
            }
            return vec3;
        }
    }
}