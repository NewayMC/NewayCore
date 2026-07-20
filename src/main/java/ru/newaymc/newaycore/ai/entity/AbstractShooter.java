package ru.newaymc.newaycore.ai.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.AiShooterSetup;
import ru.newaymc.newaycore.ai.utils.IShooterSetup;
import ru.newaymc.newaycore.gun.GunSetup;

public abstract class AbstractShooter extends Monster implements IShooterSetup {
    private static GunSetup.GunSettings gunSettings;

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
    public void performRangedAttack(LivingEntity livingEntity, float v) {

    }

    @Override
    public void baseTick() {
        super.baseTick();
        buildAi(this);
        if (ShooterCore.memory != null) {
            tickUpdate(this.tickCount);
            tickingGoals();
        }
    }

    @Override
    @AiShooterSetup
    public void buildAi(PathfinderMob mob) {
        Memory memory = new Memory(mob);
        ShooterCore.setup(memory);
    }

    @Override
    public void equipGun() {
        GunSetup.setGun(this, "ak47", "SEMI", 30, null, null, null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_) {
        equipGun();
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
