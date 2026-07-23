package ru.newaymc.newaycore.ai.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.goals.GunAttack;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.gun.GunSetup;

public abstract class AbstractShooter extends Monster {
    private static Memory memory;

    protected AbstractShooter(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        buildAi(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this){
            @Override
            public boolean canUse() {
                if (this.mob instanceof AbstractShooter) {
                    return false;
                }
                return super.canUse();
            }
        }.setAlertOthers());
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    @SafeVarargs
    public final void setTargets(Class<? extends LivingEntity>... classes) {
        for (Class<? extends LivingEntity> clazz : classes) {
            this.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, clazz, true).setUnseenMemoryTicks(500));
        }
    }

    public void syncMemory(Memory memory) {
        memory.setTicks(this.tickCount);
    }

    public Memory getMemory() {
        return memory;
    }

    public void buildAi(PathfinderMob mob) {
        memory = new Memory(mob);
        syncMemory(memory);

        ShooterCore.setup(memory);
    }

    public void equipGun(String gun, String fireMode, int maxAmmo, String scope, String muzzle, String grip) {
        GunSetup.setGun(this, gun, fireMode, maxAmmo, scope, muzzle, grip);
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_) {
        equipGun("ak47", "SEMI", 31, null, null, null);

        this.goalSelector.addGoal(1, new GunAttack(this, 90, 1.4f, 0.012f, 3, 5, 10 , 15));
        return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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
}
