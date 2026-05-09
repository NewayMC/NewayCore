package ru.newaymc.newaycore.ai.engine;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import ru.newaymc.newaycore.entity.GunAmmoEntity;
import ru.newaymc.newaycore.init.ModEntities;

import java.util.List;
import java.util.function.Supplier;

public class ShooterMain {
    public static class BattleAI {
        public static String aiState = "";
        public static Boolean canFindCover = false;
        public static Boolean allowAttack = true;

        public static void init(LevelAccessor world, double x, double y, double z, Entity entity, double ammunation, double damage, double inaccuraceAccumulation, double recoil, double recoveryTime, double shoot, double speed, String aiType) {
            if (entity == null || aiType == null)
                return;

            if (!world.getEntitiesOfClass(Player.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(128 / 2d), e -> true).isEmpty()) {
                // Entity detection
                if (((Supplier<Boolean>) (() -> {
                    if (entity == null || (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)
                        return false;
                    Level level = entity.level();
                    if (level == null)
                        return false;
                    Vec3 start = entity.getEyePosition(1f);
                    Vec3 end = (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getEyePosition(1f);
                    ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                    BlockHitResult hit = level.clip(context);
                    if (hit.getType() == HitResult.Type.MISS)
                        return true;
                    return hit.getLocation().distanceToSqr(start) >= end.distanceToSqr(start);
                })).get()) {
                    if (((Supplier<Entity>) (() -> {
                        if (entity == null || entity.level() == null)
                            return null;
                        Entity source = entity;
                        Level level = source.level();
                        double maxDistance = (double) 16;
                        double projectileRadius = (double) 3;
                        Vec3 start = source.getEyePosition(1f);
                        Vec3 look = source.getViewVector(1f).normalize();
                        Entity nearest = null;
                        double nearestForward = Double.POSITIVE_INFINITY;
                        List<Entity> candidates = level.getEntities(source, source.getBoundingBox().inflate(maxDistance));
                        for (Entity candidate : candidates) {
                            if (candidate == source)
                                continue;
                            if (!candidate.isPickable())
                                continue;
                            AABB box = candidate.getBoundingBox();
                            Vec3 boxCenter = new Vec3((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0);
                            Vec3 toCenter = boxCenter.subtract(start);
                            double forwardProj = toCenter.dot(look);
                            if (forwardProj <= 0)
                                continue;
                            if (forwardProj > maxDistance)
                                continue;
                            Vec3 pointOnLine = start.add(look.scale(forwardProj));
                            double cx = Math.max(box.minX, Math.min(box.maxX, pointOnLine.x));
                            double cy = Math.max(box.minY, Math.min(box.maxY, pointOnLine.y));
                            double cz = Math.max(box.minZ, Math.min(box.maxZ, pointOnLine.z));
                            Vec3 closestPointOnBox = new Vec3(cx, cy, cz);
                            double lateralDist = closestPointOnBox.subtract(pointOnLine).length();
                            if (lateralDist <= projectileRadius) {
                                double forwardForCandidate = forwardProj;
                                if (forwardForCandidate < nearestForward) {
                                    nearestForward = forwardForCandidate;
                                    nearest = candidate;
                                }
                            } else {
                                double tMin = Math.max(0.0, forwardProj - 1.0);
                                double tMax = Math.min(maxDistance, forwardProj + 1.0);
                                boolean intersects = false;
                                int samples = 5;
                                for (int i = 0; i <= samples; i++) {
                                    double t = tMin + (tMax - tMin) * i / (double) samples;
                                    Vec3 p = start.add(look.scale(t));
                                    double px = Math.max(box.minX, Math.min(box.maxX, p.x));
                                    double py = Math.max(box.minY, Math.min(box.maxY, p.y));
                                    double pz = Math.max(box.minZ, Math.min(box.maxZ, p.z));
                                    Vec3 cp = new Vec3(px, py, pz);
                                    double lat = cp.subtract(p).length();
                                    if (lat <= projectileRadius) {
                                        intersects = true;
                                        if (t < nearestForward) {
                                            nearestForward = t;
                                            nearest = candidate;
                                        }
                                        break;
                                    }
                                }
                                if (intersects)
                                    continue;
                            }
                        }
                        return nearest;
                    })).get() instanceof Player) {
                        if (allowAttack) {
                            if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof LivingEntity && (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).isAlive()) {
                                aiState = "inBattle";
                                if (entity instanceof Mob _mob) {
                                    if (!(_mob.getTarget() instanceof LivingEntity) || !((LivingEntity) _mob.getTarget()).isAlive()) {
                                        try {
                                            GoalSelector _targetSelector = _mob.targetSelector;
                                            NearestAttackableTargetGoal<LivingEntity> _goal = new NearestAttackableTargetGoal<>(_mob, LivingEntity.class, 10, true, false,
                                                    e -> e.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("player")))
                                                            && !e.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("no_entities"))));
                                            _targetSelector.addGoal((int) 1, _goal);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                                entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                                        new Vec3(((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getX()),
                                                ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getY() + (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getBbHeight() * 0.75),
                                                ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getZ())));
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsMousePressed", true);
                            } else {
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsMousePressed", false);
                            }
                        }
                    }
                } else {
                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsMousePressed", false);

                    aiState = "normal";
                }
                if ((aiState).equals("alerted")) {
                    entity.getPersistentData().putBoolean("borderPatrol", true);
                    if (!entity.getPersistentData().getBoolean("borderPatrolCall")) {
                        entity.getPersistentData().putBoolean("borderPatrolCall", true);
                        entity.getPersistentData().putBoolean("borderPatrol", false);
                        entity.getPersistentData().putBoolean("borderPatrolCanBeStopped", false);
                        if (entity instanceof PathfinderMob mob) {
                            mob.goalSelector.addGoal(3, new GoalsExtension.BorderPatrolGoal(mob, (double) 8, (double) 1, (int) 100));
                        }
                    }
                }
                // Types setup
                if ((aiType).equals("standart")) {
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Delay") < 1) {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsShooting", false);
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("Delay", 0);
                    } else {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("Delay",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Delay") + 1);
                    }
                    boolean isGunInHand = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY) == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") > 0)
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("RecoveryTime",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") - 1);
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") > 0) {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") - (inaccuraceAccumulation * 0.75));
                        if ((int) (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") < 0)
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy", 0);
                    }
                    ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
                    if (mainHandStack.getItem() == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()) {
                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("MagazineAmmoNumber") > 0
                                && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") <= 0
                                && ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("IsMousePressed")
                                || (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("ShootedAmmo") != 0)) {
                            Entity _shootFrom = entity;
                            Level projectileLevel = _shootFrom.level();
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("Inaccuracy", 2.0D);
                            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("ShootedAmmo") < shoot) {
                                if (isGunInHand) {
                                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("ShootedAmmo",
                                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("ShootedAmmo") + 1);
                                    if (!projectileLevel.isClientSide()) {
                                        Projectile _entityToSpawn = initArrowProjectile(new GunAmmoEntity(ModEntities.GUN_AMMO.get(), projectileLevel), null, (float) damage, true, false, false, AbstractArrow.Pickup.DISALLOWED);
                                        _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.3, _shootFrom.getZ());
                                        _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, (float) speed,
                                                (float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("Inaccuracy")
                                                        + (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy")));
                                        projectileLevel.addFreshEntity(_entityToSpawn);
                                    }
                                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsShooting", true);
                                    if (projectileLevel.isClientSide())
                                        _shootFrom.setXRot(_shootFrom.getXRot() - (float) recoil);
                                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("MagazineAmmoNumber",
                                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("MagazineAmmoNumber") - 1);
                                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy",
                                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy")
                                                    + (double) inaccuraceAccumulation);
                                }
                            } else {
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("RecoveryTime", recoveryTime);
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShootedAmmo", 0);
                            }
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("MaxInaccuracy", (double) inaccuraceAccumulation);
                        }
                    }
                } else if ((aiType).equals("sniper")) {
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Delay") < 1) {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsShooting", false);
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("Delay", 0);
                    } else {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("Delay",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Delay") + 1);
                    }
                    boolean isGunInHand = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY) == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") > 0)
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("RecoveryTime",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") - 1);
                    if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") > 0) {
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy",
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") - (inaccuraceAccumulation * 0.75));
                        if ((int) (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy") < 0)
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy", 0);
                    }
                    ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
                    if (mainHandStack.getItem() == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()) {
                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("MagazineAmmoNumber") > 0
                                && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") <= 0
                                && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("IsMousePressed")) {
                            Entity _shootFrom = entity;
                            Level projectileLevel = _shootFrom.level();
                            if (isGunInHand) {
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("Inaccuracy", 1.0D);
                                if (!projectileLevel.isClientSide()) {
                                    Projectile _entityToSpawn = initArrowProjectile(new GunAmmoEntity(ModEntities.GUN_AMMO.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 1, (byte) 2)), null, (float) damage, true,
                                            false, true, AbstractArrow.Pickup.DISALLOWED);
                                    _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.3, _shootFrom.getZ());
                                    _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, (float) speed,
                                            (float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("Inaccuracy")
                                                    + (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy")));
                                    projectileLevel.addFreshEntity(_entityToSpawn);
                                }
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("IsShooting", true);
                                if (projectileLevel.isClientSide())
                                    _shootFrom.setXRot(_shootFrom.getXRot() - (float) recoil);
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("MagazineAmmoNumber",
                                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("MagazineAmmoNumber") - 1);
                                (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("ShotInaccuracy",
                                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("ShotInaccuracy")
                                                + (double) inaccuraceAccumulation);
                            }
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putDouble("MaxInaccuracy", (double) inaccuraceAccumulation);
                            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("RecoveryTime", (int) recoveryTime);
                        }
                    }
                }

                // Reload
                if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("MagazineAmmoNumber") == 0
                        && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("RecoveryTime") == 0) {
                    CompoundTag tag = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    tag.putInt("MagazineAmmoNumber", (int) ammunation);
                    tag.putBoolean("IsReloading", true);
                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("RecoveryTime", (int) recoveryTime);
                    entity.playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("newaycore:ak47_reload")), 1, 1 );
                }
                // Sounds
                if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("IsShooting")) {
                    entity.playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("newaycore:ak47_fire")), 1, 1);
                }

                // Cover system
                if ((aiState).equals("inBattle")) {
                    if (entity instanceof PathfinderMob mob) {
                        canFindCover = true;
                        mob.goalSelector.addGoal(1, new GoalsExtension.FindCover(mob, x, y, z, world));
                    }
                }
            } else {
                aiState = "normal";
            }
        }

        public static Boolean getAllowAttack() {
            return allowAttack;
        }

        private static AbstractArrow initArrowProjectile(AbstractArrow entityToSpawn, Entity shooter, float damage, boolean silent, boolean fire, boolean particles, AbstractArrow.Pickup pickup) {
            entityToSpawn.setOwner(shooter);
            entityToSpawn.setBaseDamage(damage);
            if (silent)
                entityToSpawn.setSilent(true);
            if (fire)
                entityToSpawn.igniteForSeconds(100);
            if (particles)
                entityToSpawn.setCritArrow(true);
            entityToSpawn.pickup = pickup;
            return entityToSpawn;
        }

        private static ItemStack createArrowWeaponItemStack(Level level, int knockback, byte piercing) {
            ItemStack weapon = new ItemStack(Items.ARROW);
            if (knockback > 0)
                weapon.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), knockback);
            if (piercing > 0)
                weapon.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING), piercing);
            return weapon;
        }
    }
}