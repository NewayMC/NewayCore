package ru.newaymc.newaycore.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.entity.AbstractShooter;
import ru.newaymc.newaycore.ai.utils.Cover;
import ru.newaymc.newaycore.ai.memory.Memory;
import ru.newaymc.newaycore.ai.utils.State;
import ru.newaymc.newaycore.ai.utils.Order;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class SmartCover extends Goal {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/SmartCover");
    private static final boolean debug = false;

    private static List<Cover> covers = new ArrayList<>();

    private static final double MAX_SEARCH_RADIUS = 16;
    private static final double MIN_ENEMY_DISTANCE = 5;
    private static final double SAFETY_MODIFIER = 0.4;
    private static final double DISTANCE_MODIFIER = 0.5;

    private static LevelAccessor world;
    private static double x;
    private static double y;
    private static double z;
    private static AbstractShooter shooter;
    private static Vec3 targetPos;

    private static final TagKey<Block> TERRAIN = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "terrain"));

    public SmartCover(AbstractShooter mob, Vec3 pos) {
        world = mob.level();
        x = pos.x();
        y = pos.y();
        z = pos.z();
        shooter = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (shooter.getMemory().getState() == State.CALM) {
            return false;
        }

        return shooter.getHealth() <= 50 || shooter.getMemory().getCurrentOrder() == Order.DEFEND;
    }

    @Override
    public boolean canContinueToUse() {
        if (shooter.getMemory().getState() != State.CALM && shooter.getHealth() <= 50 || shooter.getMemory().getCurrentOrder() == Order.DEFEND) {
            return true;
        } else {
            stop();
            return false;
        }
    }

    @Override
    public void tick() {
        targetPos = shooter.getMemory().getLastTargetPos();
        Cover bestCover = findBestCover();

        if (bestCover != null) {
            shooter.getMemory().setCoverStatus(true);
            PathNavigation nav = shooter.getNavigation();
            double dist = bestCover.getDistance();
            if (dist <= 0.05) {
                shooter.setPos(bestCover.getVec3());
                shooter.getMemory().setCurrentCover(bestCover);
                shooter.getMemory().setAllowAttack(true);
            } else {
                nav.moveTo(bestCover.getVec3().x(), bestCover.getVec3().y(), bestCover.getVec3().z(), 1.1);
            }
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        shooter.getMemory().getBlockedCovers().remove(shooter);

        shooter.getMemory().setAllowAttack(true);
        shooter.getMemory().setCurrentCover(null);
        shooter.getMemory().setCoverStatus(false);
    }

    private static void debug() {
        if (debug) {
            if (!covers.isEmpty()) {
                LOGGER.debug("Size: {} ,Covers: {}", covers.size(), covers);
            } else {
                LOGGER.debug("Null");
            }
        }
    }

    private static List<Cover> findPossibleCovers() {
        List<Cover> possibleCovers = new ArrayList<>();
        Vec3 coverPos;

        double searchDiameter = MAX_SEARCH_RADIUS + MAX_SEARCH_RADIUS + 1;
        double sX;
        double sZ;

        sX = -MAX_SEARCH_RADIUS;
        for (int index0 = 0; index0 < searchDiameter; index0++) {
            sZ = -MAX_SEARCH_RADIUS;
            for (int index1 = 0; index1 < searchDiameter; index1++) {
                if (!(world.getBlockState(BlockPos.containing(x + sX, y, z + sZ))).is(TERRAIN)) {
                    Direction direction = shooter.getDirection();
                    coverPos = foundDirection(new Vec3(x + sX, y, z + sZ), direction);
                    while (!world.getBlockState(BlockPos.containing(coverPos)).canOcclude()) {
                        coverPos = foundDirection(new Vec3(x + sX, y, z + sZ), direction);
                    }
                    possibleCovers.add(new Cover(coverPos, shooter.distanceToSqr(coverPos)));
                }
                sZ = sZ + 1;
            }
            sX = sX + 1;
        }
        debug();

        return possibleCovers;
    }

    private static Cover findBestCover() {
        covers = findPossibleCovers();
        if (covers.isEmpty()) {
            return null;
        }
        double bestScore = -Double.MAX_VALUE;
        Cover bestCover = null;

        checkCoverBlock();
        for (Cover cover : covers) {
            double dist = cover.getDistance();
            double safety = evaluateSafety(cover);
            double coverDistToTarget = cover.getVec3().distanceTo(targetPos);

            double total = (DISTANCE_MODIFIER * dist) + (SAFETY_MODIFIER * safety);

            if (shooter.getMemory().getBlockedCovers().containsValue(cover)) {
                total *= 0.0;
            }

            if (coverDistToTarget <= MIN_ENEMY_DISTANCE) {
                total *= 0.5;
            }

            if (shooter.getHealth() <= 30) {
                total *= 1.2;
            }

            cover.setScore(total);

            if (total > bestScore) {
                bestScore = total;
                bestCover = cover;
            }
        }
        return bestCover;
    }

    private static double evaluateSafety(Cover cover) {
        double distToTarget = cover.getVec3().distanceTo(targetPos);
        double distFactor = Math.min(1.0, distToTarget / 10.0);

        if (distToTarget < 3) {
            return 0;
        }
        return 0.7 + 0.3 * distFactor;
    }

    private static Vec3 foundDirection(Vec3 vec3, Direction direction) {
        if (direction == Direction.SOUTH) {
            vec3 = new Vec3(vec3.x, vec3.y, vec3.z - 1);

        } else if (direction == Direction.NORTH) {
            vec3 = new Vec3(vec3.x, vec3.y, vec3.z + 1);

        } else if (direction == Direction.WEST) {
            vec3 = new Vec3(vec3.x + 1, vec3.y, vec3.z);

        } else if (direction == Direction.EAST) {
            vec3 = new Vec3(vec3.x - 1, vec3.y, vec3.z);
        }
        return vec3;
    }

    private static void checkCoverBlock() {
        Map<AbstractShooter, Cover> blocked = shooter.getMemory().getBlockedCovers();
        for (AbstractShooter teammate : shooter.getMemory().getTeammates()) {
            Memory memory = teammate.getMemory();
            if (memory.isCoverStatus()) {
                blocked.putIfAbsent(teammate, memory.getCurrentCover());
            }
        }
    }
}
