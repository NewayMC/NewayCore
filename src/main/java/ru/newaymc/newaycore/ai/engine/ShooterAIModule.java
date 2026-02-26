package ru.newaymc.newaycore.ai.engine;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.EliteShooterEntity;
import ru.newaymc.newaycore.als.outpost.OutpostRegister;
import ru.newaymc.newaycore.entity.GunAmmoEntity;
import ru.newaymc.newaycore.init.ModBlocksInit;
import ru.newaymc.newaycore.init.ModEntitiesInit;
import ru.newaymc.newaycore.network.vars.ModVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.Comparator;

public class ShooterAIModule {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity GetShooterEntity, boolean CanBeCommander, boolean CanBeInSquad, double ammunation, double damage, double inaccurace_accumulation, double recoil, double recovery_time, double shoot, double speed, String ai_type) {
        if (GetShooterEntity == null)
            return;
        double sx = 0;
        double sy = 0;
        double sz = 0;
        double rnd = 0;
        boolean found = false;
        Entity cmd = null;
        File TargetOutpost = new File("");
        if (!world.getEntitiesOfClass(Player.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(128 / 2d), e -> true).isEmpty()) {
            // Entity detection start
            if (((Supplier<Boolean>) (() -> {
                if (GetShooterEntity == null || (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)
                    return false;
                Level level = GetShooterEntity.level();
                if (level == null)
                    return false;
                Vec3 start = GetShooterEntity.getEyePosition(1f);
                Vec3 end = (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getEyePosition(1f);
                ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, GetShooterEntity);
                BlockHitResult hit = level.clip(context);
                if (hit.getType() == HitResult.Type.MISS)
                    return true;
                return hit.getLocation().distanceToSqr(start) >= end.distanceToSqr(start);
            })).get()) {
                if (((Supplier<Entity>) (() -> {
                    if (GetShooterEntity == null || GetShooterEntity.level() == null)
                        return null;
                    Entity source = GetShooterEntity;
                    Level level = source.level();
                    double maxDistance = 15;
                    double projectileRadius = 3;
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
                    // GetShooterEntity.getPersistentData().putBoolean("forceStopBorderPatrol", true);
                    GetShooterEntity.lookAt(EntityAnchorArgument.Anchor.EYES,
                            new Vec3(((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getX()),
                                    ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getY() + (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getBbHeight() * 0.75),
                                    ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getZ())));
                    NewaycoreMod.queueServerWork(60, () -> {
                        ModVariables.MapVariables.get(world).AIstate = true;
                        ModVariables.MapVariables.get(world).markSyncDirty();
                    });
                }
            }
            // Entity detection end
            // Allow attack start
            if (ModVariables.MapVariables.get(world).AIstate) {
                if (GetShooterEntity instanceof Mob _mob) {
                    if (!(_mob.getTarget() instanceof LivingEntity) || !_mob.getTarget().isAlive()) {
                        try {
                            GoalSelector _targetSelector = _mob.targetSelector;
                            NearestAttackableTargetGoal<LivingEntity> _goal = new NearestAttackableTargetGoal<>(_mob, LivingEntity.class, 10, true, false,
                                    e -> e.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("player"))) && !e.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("no_entities"))));
                            _targetSelector.addGoal(1, _goal);
                        } catch (Exception ignored) {
                        }
                    }
                }
                if ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof LivingEntity) {
                    GetShooterEntity.lookAt(EntityAnchorArgument.Anchor.EYES,
                            new Vec3(((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getX()),
                                    ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getY() + (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getBbHeight() * 0.75),
                                    ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getZ())));
                }
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsMousePressed",
                        ((GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof LivingEntity && (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).isAlive()));
            }
            // Allow attack end
            //  Standart AI type setup start
            ItemStack mainHandStack = (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
            boolean isGunInHand = (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY) == (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);

            if ((ai_type).equals("standart"))
                if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("Delay") < 1) {
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsShooting", false);
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("Delay", 0);
                } else {
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("Delay",
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("Delay") + 1);
                }
            if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") > 0)
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("RecoveryTime",
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") - 1);
            if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") > 0) {
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy",
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") - (inaccurace_accumulation * 0.75));
                if ((int) (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") < 0)
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy", 0);
            }

            if (mainHandStack.getItem() == (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()) {
                if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("MagazineAmmoNumber") > 0
                        && (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") <= 0
                        && ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("IsMousePressed")
                        || (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("ShootedAmmo") != 0)) {
                    Entity _shootFrom = GetShooterEntity;
                    Level projectileLevel = _shootFrom.level();
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("Inaccuracy", 2.0D);
                    if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("ShootedAmmo") < shoot) {
                        if (isGunInHand) {
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("ShootedAmmo",
                                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("ShootedAmmo") + 1);
                            if (!projectileLevel.isClientSide()) {
                                Projectile _entityToSpawn = initArrowProjectile(new GunAmmoEntity(ModEntitiesInit.GUN_AMMO.get(), projectileLevel), null, (float) damage, true, false, false, AbstractArrow.Pickup.DISALLOWED);
                                _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.3, _shootFrom.getZ());
                                _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, (float) speed,
                                        (float) ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("Inaccuracy")
                                                + (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy")));
                                projectileLevel.addFreshEntity(_entityToSpawn);
                            }
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsShooting", true);
                            if (projectileLevel.isClientSide())
                                _shootFrom.setXRot(_shootFrom.getXRot() - (float) recoil);
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("MagazineAmmoNumber",
                                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("MagazineAmmoNumber") - 1);
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy",
                                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") + inaccurace_accumulation);
                        }
                    } else {
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("RecoveryTime", recovery_time);
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShootedAmmo", 0);
                    }
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("MaxInaccuracy", inaccurace_accumulation);
                }
            } else if ((ai_type).equals("sniper")) {
                if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("Delay") < 1) {
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsShooting", false);
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("Delay", 0);
                } else {
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("Delay",
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("Delay") + 1);
                }

                if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") > 0)
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("RecoveryTime",
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") - 1);
                if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") > 0) {
                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy",
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") - (inaccurace_accumulation * 0.75));
                    if ((int) (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") < 0)
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy", 0);
                }

                if (mainHandStack.getItem() == (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()) {
                    if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("MagazineAmmoNumber") > 0
                            && (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") <= 0
                            && (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("IsMousePressed")) {
                        Entity _shootFrom = GetShooterEntity;
                        Level projectileLevel = _shootFrom.level();
                        if (isGunInHand) {
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("Inaccuracy", 1.0D);
                            if (!projectileLevel.isClientSide()) {
                                Projectile _entityToSpawn = initArrowProjectile(createArrowWeaponItemStack(new GunAmmoEntity(ModEntitiesInit.GUN_AMMO.get(), 0, 0, 0, projectileLevel), 1, (byte) 2), null, (float) damage, true, false, true,
                                        AbstractArrow.Pickup.DISALLOWED);
                                _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.3, _shootFrom.getZ());
                                _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, (float) speed,
                                        (float) ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("Inaccuracy")
                                                + (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy")));
                                projectileLevel.addFreshEntity(_entityToSpawn);
                            }
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsShooting", true);
                            if (projectileLevel.isClientSide())
                                _shootFrom.setXRot(_shootFrom.getXRot() - (float) recoil);
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("MagazineAmmoNumber",
                                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("MagazineAmmoNumber") - 1);
                            (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("ShotInaccuracy",
                                    (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble("ShotInaccuracy") + inaccurace_accumulation);
                        }
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble("MaxInaccuracy", inaccurace_accumulation);
                        (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("RecoveryTime", (int) recovery_time);
                    }
                }

            }
            // Standart type setup end
            // Reloading start
            if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("MagazineAmmoNumber") == 0
                    && (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getInt("RecoveryTime") == 0) {
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("MagazineAmmoNumber", (int) ammunation);
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsReloading", true);
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putInt("RecoveryTime", (int) recovery_time);
            }
            // Reloading end
            // Gun sounds start
            if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("IsShooting")) {
                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(null, BlockPos.containing(GetShooterEntity.getX(), GetShooterEntity.getY(), GetShooterEntity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_fire")), SoundSource.NEUTRAL, 1, 1);
                    } else {
                        _level.playLocalSound((GetShooterEntity.getX()), (GetShooterEntity.getY()), (GetShooterEntity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_fire")), SoundSource.NEUTRAL, 1, 1, false);
                    }
                }
            }
            if (((Supplier<Boolean>) (() -> {
                boolean boly_ = (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getBoolean("IsReloading");
                (GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putBoolean("IsReloading", false);
                return boly_;
            })).get()) {
                if (world instanceof Level _level) {
                    if (!_level.isClientSide()) {
                        _level.playSound(null, BlockPos.containing(GetShooterEntity.getX(), GetShooterEntity.getY(), GetShooterEntity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_reload")), SoundSource.NEUTRAL, 1,
                                1);
                    } else {
                        _level.playLocalSound((GetShooterEntity.getX()), (GetShooterEntity.getY()), (GetShooterEntity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("newaycore", "ak47_reload")), SoundSource.NEUTRAL, 1, 1, false);
                    }
                }
            }
            // Gun sounds end
            // Find cover if low health start
            if ((GetShooterEntity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) < 50) {
                sx = -3;
                for (int index0 = 0; index0 < 16; index0++) {
                    sy = -3;
                    for (int index1 = 0; index1 < 16; index1++) {
                        sz = -3;
                        for (int index2 = 0; index2 < 16; index2++) {
                            if ((world.getBlockState(BlockPos.containing(x + sx, y + sy, z + sz))).getBlock() == ModBlocksInit.COVER_MARKER_AI.get()) {
                                if (GetShooterEntity instanceof Mob _entity)
                                    _entity.getNavigation().moveTo((x + sx), (y + sy), (z + sz), 2);
                                break;
                            }
                            sz = sz + 1;
                        }
                        sy = sy + 1;
                    }
                    sx = sx + 1;
                }
            }
            // Find cover if low heath end
            // Targets ( patrol or going to object point ) start
            if (ModVariables.MapVariables.get(world).AIstate) {
                if (!((Supplier<Boolean>) (() -> {
                    if (GetShooterEntity == null || (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)
                        return false;
                    Level level = GetShooterEntity.level();
                    if (level == null)
                        return false;
                    Vec3 start = GetShooterEntity.getEyePosition(1f);
                    Vec3 end = (GetShooterEntity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getEyePosition(1f);
                    ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, GetShooterEntity);
                    BlockHitResult hit = level.clip(context);
                    if (hit.getType() == HitResult.Type.MISS)
                        return true;
                    return hit.getLocation().distanceToSqr(start) >= end.distanceToSqr(start);
                })).get()) {
                    sx = -3;
                    for (int index3 = 0; index3 < 32; index3++) {
                        sy = -3;
                        for (int index4 = 0; index4 < 32; index4++) {
                            sz = -3;
                            for (int index5 = 0; index5 < 32; index5++) {
                                if ((world.getBlockState(BlockPos.containing(x + sx, y + sy, z + sz))).getBlock() == ModBlocksInit.OBJECT_MARKER_AI.get()) {
                                    if (GetShooterEntity instanceof Mob _entity)
                                        _entity.getNavigation().moveTo((x + sx), (y + sy), (z + sz), 1);
                                }
                                sz = sz + 1;
                            }
                            sy = sy + 1;
                        }
                        sx = sx + 1;
                    }
                }
            } else {
                sx = -3;
                for (int index6 = 0; index6 < 32; index6++) {
                    sy = -3;
                    for (int index7 = 0; index7 < 32; index7++) {
                        sz = -3;
                        for (int index8 = 0; index8 < 32; index8++) {
                            if ((world.getBlockState(BlockPos.containing(x + sx, y + sy, z + sz))).getBlock() == ModBlocksInit.OBJECT_MARKER_AI.get()) {
                                if (GetShooterEntity instanceof Mob _entity)
                                    _entity.getNavigation().moveTo((x + sx), (y + sy), (z + sz), 1);
                                found = true;
                                break;
                            } else {
                                found = false;
                            }
                            sz = sz + 1;
                        }
                        sy = sy + 1;
                    }
                    sx = sx + 1;
                }
                if (!found) {
                    GetShooterEntity.getPersistentData().putBoolean("borderPatrol", true);
                    if (!GetShooterEntity.getPersistentData().getBoolean("borderPatrolCall")) {
                        GetShooterEntity.getPersistentData().putBoolean("borderPatrolCall", true);
                        GetShooterEntity.getPersistentData().putBoolean("borderPatrol", false);
                        GetShooterEntity.getPersistentData().putBoolean("borderPatrolCanBeStopped", false);
                        if (GetShooterEntity instanceof PathfinderMob mob) {
                            mob.goalSelector.addGoal(1, new GoalsExtension.BorderPatrolGoal(mob, 16, 1, 60));
                        }
                    }
                } else {
                    GetShooterEntity.getPersistentData().putBoolean("forceStopBorderPatrol", true);
                }
            }
            // Targets ( patrol or going to object point ) end
        }
        // Temporarily disabled (awaiting testing)
        if (ModVariables.MapVariables.get(world).ALSToggle) {
            if (CanBeInSquad && !(GetShooterEntity instanceof EliteShooterEntity)) {
                if (!world.getEntitiesOfClass(EliteShooterEntity.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(8 / 2d), e -> true).isEmpty()
                        && ((findEntityInWorldRange(world, EliteShooterEntity.class, x, y, z, 8)) instanceof EliteShooterEntity _datEntL77 && _datEntL77.getEntityData().get(EliteShooterEntity.DATA_CanBeCommander))) {
                    cmd = findEntityInWorldRange(world, EliteShooterEntity.class, x, y, z, 8);
                    GetShooterEntity.getPersistentData().putBoolean("forceStopSimpleFormation", false);
                    GetShooterEntity.getPersistentData().putBoolean("simpleFormationCanBeStopped", true);
                    if (GetShooterEntity instanceof PathfinderMob mob && (cmd != null && cmd instanceof PathfinderMob commander)) {
                        mob.goalSelector.addGoal(2, new GoalsExtension.SimpleFormationGoal(mob, commander, 6, 0.7 * 3.0D, 1));
                    }
                }
            }
            if (CanBeCommander && !CanBeInSquad) {
                rnd = Mth.nextInt(RandomSource.create(), 0, (int) (OutpostRegister.execute(world) - 1));
                TargetOutpost = new File((FMLPaths.GAMEDIR.get().toString() + "/NewayMC/Outposts/"), File.separator + ("outpost_id_" + rnd + ".json"));
                if (TargetOutpost.exists()) {
                    {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(TargetOutpost));
                            StringBuilder jsonstringbuilder = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                jsonstringbuilder.append(line);
                            }
                            bufferedReader.close();
                            ModVariables.OutpostJsonObj = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                            if (GetShooterEntity instanceof Mob _entity)
                                _entity.getNavigation().moveTo(ModVariables.OutpostJsonObj.get("coordinate-x").getAsDouble(), ModVariables.OutpostJsonObj.get("coordinate-y").getAsDouble(),
                                        ModVariables.OutpostJsonObj.get("coordinate-z").getAsDouble(), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static AbstractArrow initArrowProjectile(AbstractArrow entityToSpawn, Entity shooter, float damage, boolean silent, boolean fire, boolean particles, AbstractArrow.Pickup pickup) {
        entityToSpawn.setOwner(shooter);
        entityToSpawn.setBaseDamage(damage);
        if (silent)
            entityToSpawn.setSilent(true);
        if (fire)
            entityToSpawn.setSecondsOnFire(100);
        if (particles)
            entityToSpawn.setCritArrow(true);
        entityToSpawn.pickup = pickup;
        return entityToSpawn;
    }

    private static AbstractArrow createArrowWeaponItemStack(AbstractArrow entityToSpawn, int knockback, byte piercing) {
        if (knockback > 0)
            entityToSpawn.setKnockback(knockback);
        if (piercing > 0)
            entityToSpawn.setPierceLevel(piercing);
        return entityToSpawn;
    }

    private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
        return world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).findFirst().orElse(null);
    }
}