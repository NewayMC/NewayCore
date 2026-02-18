package ru.newaymc.newaycore.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import ru.newaymc.newaycore.entity.GunAmmoEntity;
import ru.newaymc.newaycore.init.ModEntitiesInit;
import ru.newaymc.newaycore.init.ModItemsInit;

public class EliteShooterEntity extends Monster implements RangedAttackMob {

    public static final EntityDataAccessor<Boolean> DATA_als_state = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> DATA_ai_type = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> DATA_shoot = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_damage = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_speed = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_inaccurace_accumulation = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_recoil = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_ammunation = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_recovery_time = SynchedEntityData.defineId(EliteShooterEntity.class, EntityDataSerializers.INT);

    public EliteShooterEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntitiesInit.ELITE_SHOOTER_ENTITY.get(), world);
    }

    public EliteShooterEntity(EntityType<EliteShooterEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 0;
        setNoAi(false);
        setCustomName(Component.literal("Elite Shooter Entity"));
        setCustomNameVisible(true);
        setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItemsInit.M_4_A_1.get()));
    }

    public static void init() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.2);
        builder = builder.add(Attributes.MAX_HEALTH, 500);
        builder = builder.add(Attributes.ARMOR, 5);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 32);
        builder = builder.add(Attributes.ATTACK_KNOCKBACK, 0.5);
        return builder;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_als_state, true);
        this.entityData.define(DATA_ai_type, "standart");
        this.entityData.define(DATA_shoot, 5);
        this.entityData.define(DATA_damage, 4);
        this.entityData.define(DATA_speed, 4);
        this.entityData.define(DATA_inaccurace_accumulation, 2);
        this.entityData.define(DATA_recoil, 1);
        this.entityData.define(DATA_ammunation, 30);
        this.entityData.define(DATA_recovery_time, 30);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false) {
            @Override
            protected double getAttackReachSqr(LivingEntity entity) {
                return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
            }
        });

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Player.class, true, false));

        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 1024, 10f) {
            @Override
            public boolean canContinueToUse() {
                return this.canUse();
            }
        });
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Dataals_state", this.entityData.get(DATA_als_state));
        compound.putString("Dataai_type", this.entityData.get(DATA_ai_type));
        compound.putInt("Datashoot", this.entityData.get(DATA_shoot));
        compound.putInt("Datadamage", this.entityData.get(DATA_damage));
        compound.putInt("Dataspeed", this.entityData.get(DATA_speed));
        compound.putInt("Datainaccurace_accumulation", this.entityData.get(DATA_inaccurace_accumulation));
        compound.putInt("Datarecoil", this.entityData.get(DATA_recoil));
        compound.putInt("Dataammunation", this.entityData.get(DATA_ammunation));
        compound.putInt("Datarecovery_time", this.entityData.get(DATA_recovery_time));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Dataals_state"))
            this.entityData.set(DATA_als_state, compound.getBoolean("Dataals_state"));
        if (compound.contains("Dataai_type"))
            this.entityData.set(DATA_ai_type, compound.getString("Dataai_type"));
        if (compound.contains("Datashoot"))
            this.entityData.set(DATA_shoot, compound.getInt("Datashoot"));
        if (compound.contains("Datadamage"))
            this.entityData.set(DATA_damage, compound.getInt("Datadamage"));
        if (compound.contains("Dataspeed"))
            this.entityData.set(DATA_speed, compound.getInt("Dataspeed"));
        if (compound.contains("Datainaccurace_accumulation"))
            this.entityData.set(DATA_inaccurace_accumulation, compound.getInt("Datainaccurace_accumulation"));
        if (compound.contains("Datarecoil"))
            this.entityData.set(DATA_recoil, compound.getInt("Datarecoil"));
        if (compound.contains("Dataammunation"))
            this.entityData.set(DATA_ammunation, compound.getInt("Dataammunation"));
        if (compound.contains("Datarecovery_time"))
            this.entityData.set(DATA_recovery_time, compound.getInt("Datarecovery_time"));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        GetEliteShooterEntity.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float flval) {
        GunAmmoEntity.shoot(this, target);
    }
}