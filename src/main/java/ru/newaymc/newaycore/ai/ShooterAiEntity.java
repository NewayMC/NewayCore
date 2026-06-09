package ru.newaymc.newaycore.ai;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import ru.newaymc.newaycore.ai.engine.ShooterMain;
import ru.newaymc.newaycore.annotation.AiShooterSetup;
import ru.newaymc.newaycore.gun.entity.GunAmmo;
import ru.newaymc.newaycore.register.ModItems;

public class ShooterAiEntity extends Monster implements RangedAttackMob {
    public static final EntityDataAccessor<String> AI_STATE = SynchedEntityData.defineId(ShooterAiEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> ALLOW_ATTACK = SynchedEntityData.defineId(ShooterAiEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> CAN_FIND_COVER = SynchedEntityData.defineId(ShooterAiEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_BORDER_PATROL = SynchedEntityData.defineId(ShooterAiEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_SIMPLE_FORMATION = SynchedEntityData.defineId(ShooterAiEntity.class, EntityDataSerializers.BOOLEAN);

    public ShooterAiEntity(EntityType<ShooterAiEntity> type, Level world) {
        super(type, world);
        xpReward = 3;
        setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.AKM.get()));
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AI_STATE, "NORMAL");
        builder.define(ALLOW_ATTACK, false);

        builder.define(CAN_FIND_COVER, true);
        builder.define(CAN_BORDER_PATROL, false);
        builder.define(CAN_SIMPLE_FORMATION, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ai_state", this.entityData.get(AI_STATE));
        tag.putBoolean("allow_attack", this.entityData.get(ALLOW_ATTACK));

        tag.putBoolean("can_find_cover", this.entityData.get(CAN_FIND_COVER));
        tag.putBoolean("can_border_patrol", this.entityData.get(CAN_BORDER_PATROL));
        tag.putBoolean("can_simple_formation", this.entityData.get(CAN_SIMPLE_FORMATION));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(AI_STATE, tag.getString("ai_state"));
        this.entityData.set(ALLOW_ATTACK, tag.getBoolean("allow_attack"));

        this.entityData.set(CAN_FIND_COVER, tag.getBoolean("can_find_cover"));
        this.entityData.set(CAN_BORDER_PATROL, tag.getBoolean("can_border_patrol"));
        this.entityData.set(CAN_SIMPLE_FORMATION, tag.getBoolean("can_simple_formation"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, false) {
            @Override
            protected boolean canPerformAttack(LivingEntity entity) {
                return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
            }
        });
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Player.class, true, false) {
            @Override
            public boolean canUse() {
                double x = ShooterAiEntity.this.getX();
                double y = ShooterAiEntity.this.getY();
                double z = ShooterAiEntity.this.getZ();
                Entity entity = ShooterAiEntity.this;
                Level world = ShooterAiEntity.this.level();
                return super.canUse() &&  ShooterMain.BattleAI.getAllowAttack();
            }

            @Override
            public boolean canContinueToUse() {
                double x = ShooterAiEntity.this.getX();
                double y = ShooterAiEntity.this.getY();
                double z = ShooterAiEntity.this.getZ();
                Entity entity = ShooterAiEntity.this;
                Level world = ShooterAiEntity.this.level();
                return super.canContinueToUse() && ShooterMain.BattleAI.getAllowAttack();
            }
        });
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25, 1024, 10f) {
            @Override
            public boolean canContinueToUse() {
                return this.canUse();
            }
        });
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return super.getPassengerRidingPosition(entity).add(0, -0.35F, 0);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

    @Override
    public boolean shouldDropLoot() {
        return false;
    }

    @Override
    @AiShooterSetup
    public void baseTick() {
        super.baseTick();
        ShooterMain.setup(ShooterAiEntity.class);
        ShooterMain.BattleAI.init(this.level(), this.getX(), this.getY(), this.getZ(), this);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.1f);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {

    }

    @Override
    public void performRangedAttack(LivingEntity target, float flval) {
        GunAmmo.shoot(this, target);
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
        builder = builder.add(Attributes.MAX_HEALTH, 150);
        builder = builder.add(Attributes.ARMOR, 10);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 32);
        return builder;
    }
}