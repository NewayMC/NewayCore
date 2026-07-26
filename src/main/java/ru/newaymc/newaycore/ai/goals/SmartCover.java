package ru.newaymc.newaycore.ai.goals;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.entity.AbstractShooter;
import ru.newaymc.newaycore.ai.objects.Cover;
import ru.newaymc.newaycore.register.ModTags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class SmartCover extends Goal {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/SmartCover");
    private static final boolean debug = false;

    @Getter
    private static Cover bestCover;
    private static List<Cover> covers = new ArrayList<>();

    private static final double MAX_SEARCH_RADIUS = 17;
    private static final double MIN_ENEMY_DISTANCE = 5;
    private static final double SAFETY_MODIFIER = 0.4;
    private static final double DISTANCE_MODIFIER = 0.5;

    private static LevelAccessor world;
    private static double x;
    private static double y;
    private static double z;
    private static AbstractShooter shooter;

    private static Vec3 targetPos;

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
        return shooter.getHealth() <= 30;
    }

    @Override
    public boolean canContinueToUse() {
        if (shooter.getHealth() > 30) {
            shooter.goalSelector.removeGoal(this);
        }
        return shooter.getHealth() <= 30;
    }

    @Override
    public void tick() {
        if (shooter.getTarget() != null) {
            targetPos = shooter.getTarget().position();
        } else {
            shooter.getMemory().setCoverStatus(false);
            return;
        }
        Cover bestCover = findBestCover();

        if (bestCover != null) {
            PathNavigation nav = shooter.getNavigation();
            double dist = bestCover.getDistance();
            if (dist <= 0.1) {
                shooter.setPos(bestCover.getVec3());
            } else {
                nav.moveTo(bestCover.getVec3().x(), bestCover.getVec3().y(), bestCover.getVec3().z(), 1.1);
                shooter.getMemory().setCurrentCover(bestCover);
                shooter.getMemory().setCoverStatus(true);
            }
        } else {
            shooter.getMemory().setCoverStatus(false);
        }
    }

    @Override
    public void stop() {
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
        double sX;
        double sZ;

        sX = -3;
        for (int index0 = 0; index0 < MAX_SEARCH_RADIUS; index0++) {
            sZ = -3;
            for (int index1 = 0; index1 < MAX_SEARCH_RADIUS; index1++) {
                if (!(world.getBlockState(BlockPos.containing(x + sX - 5, y, z + sZ - 5))).is(ModTags.TERRAIN)) {
                    Direction direction = shooter.getDirection();
                    coverPos = foundDirection(new Vec3(x + sX - 5, y, z + sZ - 5), direction);
                    if (!world.getBlockState(BlockPos.containing(coverPos)).canOcclude()) {
                        possibleCovers.add(new Cover(coverPos, shooter.position().distanceTo(coverPos)));
                    } else {
                        coverPos = foundDirection(new Vec3(x + sX - 5, y, z + sZ - 5), direction);
                    }
                }
                sZ = sZ + 1;
            }
            sX = sX + 1;
        }
        covers = covers.stream().distinct().collect(Collectors.toList());
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

        for (Cover cover : covers) {
            double dist = cover.getDistance();
            double safety = evaluateSafety(cover);
            double coverDistToTarget = cover.getVec3().distanceTo(targetPos);

            double total = (DISTANCE_MODIFIER * dist) + (SAFETY_MODIFIER * safety);

            if (coverDistToTarget <= MIN_ENEMY_DISTANCE) {
                total *= 0.5;
            }

            if ((shooter instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (shooter instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.3) {
                total *= 1.3;
            }

            cover.setScore(total);

            if (total > bestScore) {
                bestScore = total;
                bestCover = cover;
            }
        }
        covers.sort((c1, c2) -> Double.compare(c2.getScore(), c1.getScore()));
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
}
