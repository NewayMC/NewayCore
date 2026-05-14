package ru.newaymc.newaycore.ai;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import ru.newaymc.newaycore.gun.entity.GunAmmo;
import ru.newaymc.newaycore.init.ModItems;

public class EliteShooterEntity extends Monster implements RangedAttackMob {
    public static final EntityDataAccessor<String> DATA_ai_type = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> DATA_damage = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_speed = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_recoil = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_ammunation = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_recovery_time = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_CanBeCommander = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_CanBeInSquad = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.BOOLEAN);

    public EliteShooterEntity(EntityType<EliteShooterEntity> type, Level world) {
        super(type, world);
        xpReward = 4;
        setNoAi(false);
        setCustomName(Component.literal("Elite Shooter Entity"));
        setCustomNameVisible(true);
        setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.M_4_A_1.get()));
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ai_type, "standart");
        builder.define(DATA_damage, 5);
        builder.define(DATA_speed, 3);
        builder.define(DATA_recoil, 1);
        builder.define(DATA_ammunation, 30);
        builder.define(DATA_recovery_time, 10);
        builder.define(DATA_CanBeCommander, true);
        builder.define(DATA_CanBeInSquad, false);
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
                double x = EliteShooterEntity.this.getX();
                double y = EliteShooterEntity.this.getY();
                double z = EliteShooterEntity.this.getZ();
                Entity entity = EliteShooterEntity.this;
                Level world = EliteShooterEntity.this.level();
                return super.canUse() && ShooterMain.BattleAI.getAllowAttack();
            }

            @Override
            public boolean canContinueToUse() {
                double x = EliteShooterEntity.this.getX();
                double y = EliteShooterEntity.this.getY();
                double z = EliteShooterEntity.this.getZ();
                Entity entity = EliteShooterEntity.this;
                Level world = EliteShooterEntity.this.level();
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
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Dataai_type", this.entityData.get(DATA_ai_type));
        compound.putInt("Datadamage", this.entityData.get(DATA_damage));
        compound.putInt("Dataspeed", this.entityData.get(DATA_speed));
        compound.putInt("Datarecoil", this.entityData.get(DATA_recoil));
        compound.putInt("Dataammunation", this.entityData.get(DATA_ammunation));
        compound.putInt("Datarecovery_time", this.entityData.get(DATA_recovery_time));
        compound.putBoolean("DataCanBeCommander", this.entityData.get(DATA_CanBeCommander));
        compound.putBoolean("DataCanBeInSquad", this.entityData.get(DATA_CanBeInSquad));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Dataai_type"))
            this.entityData.set(DATA_ai_type, compound.getString("Dataai_type"));
        if (compound.contains("Datadamage"))
            this.entityData.set(DATA_damage, compound.getInt("Datadamage"));
        if (compound.contains("Dataspeed"))
            this.entityData.set(DATA_speed, compound.getInt("Dataspeed"));
        if (compound.contains("Datarecoil"))
            this.entityData.set(DATA_recoil, compound.getInt("Datarecoil"));
        if (compound.contains("Dataammunation"))
            this.entityData.set(DATA_ammunation, compound.getInt("Dataammunation"));
        if (compound.contains("Datarecovery_time"))
            this.entityData.set(DATA_recovery_time, compound.getInt("Datarecovery_time"));
        if (compound.contains("DataCanBeCommander"))
            this.entityData.set(DATA_CanBeCommander, compound.getBoolean("DataCanBeCommander"));
        if (compound.contains("DataCanBeInSquad"))
            this.entityData.set(DATA_CanBeInSquad, compound.getBoolean("DataCanBeInSquad"));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        GetEliteShooterEntity.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float flval) {
        GunAmmo.shoot(this, target);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.2f);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.5);
        builder = builder.add(Attributes.MAX_HEALTH, 250);
        builder = builder.add(Attributes.ARMOR, 8);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 2);
        builder = builder.add(Attributes.FOLLOW_RANGE, 32);
        builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
        return builder;
    }
}