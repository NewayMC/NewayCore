package ru.newaymc.newaycore.ai.engine;

import com.mojang.logging.LogUtils;
import lombok.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import ru.newaymc.newaycore.register.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SmartCover {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean debug = false;

    private static List<Cover> covers = new ArrayList<>();
    private static Cover bestCover;

    private static LevelAccessor world;
    private static double x;
    private static double y;
    private static double z;
    private static PathfinderMob entity;

    private static Vec3 targetPos;

    public static void init(LevelAccessor _world, Vec3 pos, PathfinderMob _entity, Entity _target) {
        if (ShooterMain.data.isCanFindCover()) {
            world = _world;
            x = pos.x();
            y = pos.y();
            z = pos.z();
            entity = _entity;
            targetPos = _target.position();

            CoverEvaluator.findPossibleCovers();
            bestCover = CoverEvaluator.findBestCover();

            if (bestCover != null) {
                double dist = bestCover.getDistance();
                if (dist <= 0.05) {
                    entity.setPos(bestCover.getVec3());
                } else {
                    PathNavigation nav = entity.getNavigation();
                    if (nav.isDone()) {
                        nav.moveTo(bestCover.getVec3().x(), bestCover.getVec3().y(), bestCover.getVec3().z(), 1.2);
                    } else {
                        nav.tick();
                    }
                }
            }
        }
    }

    public static void exit() {
        entity.getNavigation().stop();
        clearCovers();
        ShooterMain.data.setCanFindCover(false);
    }
    /**
     * For test covers search algorithm
     */
    private static void debugMarker() {
        if (debug) {
            for (Cover cover : covers) {
                Vec3 vec3 = cover.vec3;
                world.setBlock(BlockPos.containing(vec3.x(), vec3.y(), vec3.z()), Blocks.LIME_STAINED_GLASS.defaultBlockState(), 3);
                world.setBlock(BlockPos.containing(bestCover.getVec3()), Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), 3);
            }
            if (!covers.isEmpty()) {
                LOGGER.debug("Size: {}", covers.size());
                LOGGER.debug("List: {}", covers.toString());
            } else {
                LOGGER.debug("unique = null");
            }
        }
    }

    private static void clearCovers() {
        covers.clear();
    }

    private static class CoverEvaluator {
        private static final double MAX_SEARCH_RADIUS = 17;
        private static final double MIN_ENEMY_DISTANCE = 5;

        private static final double SAFETY_MODIFIER = 0.4;
        private static final double DISTANCE_MODIFIER = 0.5;

        public static void findPossibleCovers() {
            Vec3 coverPos;
            double sX;
            double sZ;

            sX = -3;
            for (int index0 = 0; index0 < MAX_SEARCH_RADIUS; index0++) {
                sZ = -3;
                for (int index1 = 0; index1 < MAX_SEARCH_RADIUS; index1++) {
                    if (!(world.getBlockState(BlockPos.containing(x + sX - 5, y, z + sZ - 5))).is(ModTags.TERRAIN)) {
                        Direction direction = entity.getDirection();
                        coverPos = foundDirection(new Vec3(x + sX - 5, y, z + sZ - 5), direction);
                        if (!world.getBlockState(BlockPos.containing(coverPos)).canOcclude()) {
                            covers.add(new Cover(coverPos, entity.position().distanceTo(coverPos)));
                        } else {
                            coverPos = foundDirection(new Vec3(x + sX - 5, y, z + sZ - 5), direction);
                        }
                    }
                    sZ = sZ + 1;
                }
                sX = sX + 1;
            }
            covers = covers.stream().distinct().collect(Collectors.toList());
            debugMarker();
            clearCovers();
        }

        public static Cover findBestCover() {
            if (covers == null || !covers.isEmpty()) {
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

                if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) * 0.3) {
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

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Cover {
        private Vec3 vec3;
        private double score;
        private double distance;

        public Cover(Vec3 vec3, double distance) {
            this.vec3 = vec3;
            this.distance = distance;
            this.score = 0;
        }
    }
}
