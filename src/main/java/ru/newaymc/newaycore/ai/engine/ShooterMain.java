package ru.newaymc.newaycore.ai.engine;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.annotation.AiShooterSetup;
import ru.newaymc.newaycore.gun.GunSetup;
import ru.newaymc.newaycore.gun.entity.GunAmmo;
import ru.newaymc.newaycore.network.DataSerializer;
import ru.newaymc.newaycore.register.ModEntities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

public class ShooterMain {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AiData data = null;
    public static File file = null;

    private static String aiType;
    private static double ammunition;
    private static double damage;
    private static int recoveryTime;
    private static double speed;
    private static double inaccuracyAccumulation;

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

    public static void setup(LevelAccessor world, double x, double y, double z, Entity entity) {
        file = new File((FMLPaths.GAMEDIR.get().toString() + "/newaycore/data/ai/"), File.separator + entity.getStringUUID() + ".bin");
        for(Method method : entity.getClass().getDeclaredMethods()) {
            AiShooterSetup aiShooterSetup = method.getAnnotation(AiShooterSetup.class);
            if (aiShooterSetup != null) {
                aiType = aiShooterSetup.aiType();
                ammunition = aiShooterSetup.ammunition();
                damage = aiShooterSetup.damage();
                recoveryTime = aiShooterSetup.recoveryTime();
                speed = aiShooterSetup.speed();
                inaccuracyAccumulation = aiShooterSetup.inaccuracyAccumulation();
            }
        }
        if (!file.exists()) {
            data = new AiData(
                    1,
                    true,
                    false,
                    false,
                    false,
                    false);
            DataSerializer.serialization(data, file);
        } else {
            try {
                data = (AiData) DataSerializer.deserialization(file);
            } catch (IOException e) {
                LOGGER.error(e.toString());
            }
            NewaycoreMod.queueServerWork(100, () -> {
                DataSerializer.serialization(data, file);
                LOGGER.debug(data.toString());
            });
        }
        BattleAI.init(world, x, y, z, entity);
    }

    public static class BattleAI {
        private static Entity entity;
        private static final GunSetup.GunUtils mg = null;

        public static void init(LevelAccessor world, double x, double y, double z, Entity ent) {
            if (ent == null || aiType == null)
                return;
            entity = ent;
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
                })).get() && ((Supplier<Entity>) (() -> {
                    if (entity == null || entity.level() == null)
                        return null;
                    Entity source = entity;
                    Level level = source.level();
                    double maxDistance = 16;
                    double projectileRadius = 2;
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
                    data.setSeeTarget(true);
                    if (entity instanceof Mob _mob) {
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

                    if (data.isAllowAttack()) {
                        if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof LivingEntity && (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).isAlive()) {
                            data.setState(3);
                            GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.SHOULD_SHOOT, true);

                            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getX() + ((Supplier<Double>) (() -> {
                                return (double) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY);
                            })).get() * Mth.nextDouble(RandomSource.create(), -0.25, 0.25)),
                                    ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getY() + (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getBbHeight() * 0.75 + ((Supplier<Double>) (() -> {

                                        return (double) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY);
                                    })).get() * Mth.nextDouble(RandomSource.create(), -0.25, 0.25)), ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getZ() + ((Supplier<Double>) (() -> {

                                return (double) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY);
                            })).get() * Mth.nextDouble(RandomSource.create(), -0.25, 0.25))));

                            reload();
                            gunSounds();
                        } else {
                            GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.SHOULD_SHOOT, false);
                        }
                    }
                } else {
                    data.setState(2);
                    data.setSeeTarget(false);

                    GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.SHOULD_SHOOT, false);
                }

                // Types setup
                if ((aiType).equals("standard")) {
                    standardType();
                } else if ((aiType).equals("sniper")) {
                    sniperType();
                }

                // Cover
                /*if (getState().equals("IN_BATTLE")) {
                    if (EntityData.getBooleanData(entity, ShooterAiEntity.CAN_FIND_COVER)) {
                        if (entity instanceof PathfinderMob mob) mob.goalSelector.addGoal(1, new GoalsExtension.SmartCover(mob, x, y, z, world));
                    }
                }*/
            } else {
                data.setState(1);
            }
        }

        private static void standardType() {
            {
                int recovery_time = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.RECOVERY_TIME);
                boolean has_shooted = (boolean) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.HAS_SHOOTED);
                if (recovery_time > 0)
                    recovery_time--;
                double acc_inaccuracy = (double) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY);
                if (acc_inaccuracy > 0.0) {
                    acc_inaccuracy -= acc_inaccuracy * 0.75;
                    if (acc_inaccuracy < 0.0)
                        acc_inaccuracy = 0.0;
                }
                ItemStack hand = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                int current_ammo = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.AMMO_NUMBER);
                int shooted_ammo = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOOTED_ROUNDS);
                boolean should_shoot = (boolean) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOULD_SHOOT);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.IS_SHOOTING, false);
                if (hand == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY) && mg.isGun((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY))) {
                    if (current_ammo > 0 && recovery_time <= 0 && (should_shoot || shooted_ammo != 0)) {
                        Entity shooter = entity;
                        Level projectileLevel = shooter.level();
                        final float final_acc_inaccuracy = (float) acc_inaccuracy;
                        var shoot = new Object() {
                            public void act(ItemStack s, Entity e, double a) {
                                if (projectileLevel.isClientSide)
                                    return;
                                Projectile projectile = initArrowProjectile(new GunAmmo(ModEntities.GUN_AMMO.get(), projectileLevel), null, (float) damage, true, false, false, AbstractArrow.Pickup.DISALLOWED);
                                projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.25, shooter.getZ());
                                projectile.shoot(shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z, (float) speed, (float) (a + final_acc_inaccuracy));
                                projectileLevel.addFreshEntity(projectile);
                            }
                        };
                        if (shooted_ammo < 1) {
                            shooted_ammo++;
                            shoot.act(hand, shooter, 2.0);
                            mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.IS_SHOOTING, true);
                            if (projectileLevel.isClientSide())
                                shooter.setXRot(shooter.getXRot() - (float) 1);
                            current_ammo--;
                            acc_inaccuracy += inaccuracyAccumulation;
                        } else {
                            recovery_time = recoveryTime;
                            shooted_ammo = 0;
                        }
                    }
                }
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.RECOVERY_TIME, recovery_time);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY, acc_inaccuracy);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.AMMO_NUMBER, current_ammo);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOOTED_ROUNDS, shooted_ammo);
            }
        }

        private static void sniperType() {
            {
                int recovery_time = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.RECOVERY_TIME);
                boolean has_shooted = (boolean) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.HAS_SHOOTED);
                if (recovery_time > 0)
                    recovery_time--;
                double acc_inaccuracy = (double) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY);
                if (acc_inaccuracy > 0.0) {
                    acc_inaccuracy -= acc_inaccuracy * 0.75;
                    if (acc_inaccuracy < 0.0)
                        acc_inaccuracy = 0.0;
                }
                ItemStack hand = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                int current_ammo = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.AMMO_NUMBER);
                int shooted_ammo = (int) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOOTED_ROUNDS);
                boolean should_shoot = (boolean) mg.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOULD_SHOOT);
                if (!should_shoot && has_shooted) {
                    mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.HAS_SHOOTED, false);
                    has_shooted = false;
                }
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.IS_SHOOTING, false);
                if (hand == (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY) && mg.isGun((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY))) {
                    if (current_ammo > 0 && recovery_time <= 0 && should_shoot && !has_shooted) {
                        Entity shooter = entity;
                        Level projectileLevel = shooter.level();
                        final float final_acc_inaccuracy = (float) acc_inaccuracy;
                        var shoot = new Object() {
                            public void act(ItemStack s, Entity e, double a) {
                                if (projectileLevel.isClientSide)
                                    return;
                                Projectile projectile = initArrowProjectile(new GunAmmo(ModEntities.GUN_AMMO.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 1, (byte) 2)), null, (float) damage, true,
                                        false, true, AbstractArrow.Pickup.DISALLOWED);
                                projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.25, shooter.getZ());
                                projectile.shoot(shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z, (float) speed, (float) (a + final_acc_inaccuracy));
                                projectileLevel.addFreshEntity(projectile);
                            }
                        };
                        shoot.act(hand, shooter, 1.0);
                        mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.IS_SHOOTING, true);
                        mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.HAS_SHOOTED, true);
                        if (projectileLevel.isClientSide())
                            shooter.setXRot(shooter.getXRot() - (float) 1);
                        current_ammo--;
                        acc_inaccuracy += inaccuracyAccumulation;
                        recovery_time = recoveryTime;
                    }
                }
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.RECOVERY_TIME, recovery_time);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.ACCUMULATED_INACCURACY, acc_inaccuracy);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.AMMO_NUMBER, current_ammo);
                mg.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), mg.SHOOTED_ROUNDS, shooted_ammo);
            }
        }

        private static void reload() {
            if (((Supplier<Integer>) (() -> {
                return (int) GunSetup.GunUtils.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.AMMO_NUMBER);
            })).get() == 0 && ((Supplier<Integer>) (() -> {
                return (int) GunSetup.GunUtils.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.RECOVERY_TIME);
            })).get() == 0) {
                {
                    GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.AMMO_NUMBER, ammunition);
                    GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_RELOADING, true);
                }
                {
                    GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.RECOVERY_TIME, recoveryTime);
                }
            }
        }

        private static void gunSounds() {
            if (((Supplier<Boolean>) (() -> {
                return (boolean) GunSetup.GunUtils.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_SHOOTING);
            })).get()) {
                entity.playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("newaycore:ak47_fire")), 1, 1);
            }
            if (((Supplier<Boolean>) (() -> {
                boolean boly = (boolean) GunSetup.GunUtils.getValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_RELOADING);
                GunSetup.GunUtils.setValue((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY), GunSetup.GunUtils.IS_RELOADING, false);
                return boly;
            })).get()) {
                entity.playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("newaycore:ak47_reload")), 1, 1);
            }
        }

        public static Boolean getAllowAttack() {
            return data.isAllowAttack();
        }
    }
}