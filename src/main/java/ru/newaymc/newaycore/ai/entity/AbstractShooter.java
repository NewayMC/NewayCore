package ru.newaymc.newaycore.ai.entity;

import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.goals.GunAttack;
import ru.newaymc.newaycore.ai.goals.SmartCover;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.GunSetup;

@Getter
public abstract class AbstractShooter extends Monster {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/AbstractShooter");
    private final Memory memory = new Memory(this);
    public enum State {
        BATTLE,
        SEEK,
        CALM
    }

    protected AbstractShooter(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (isNoAi()) {
            return;
        }

        if (this.getTarget() != null) {
            LOGGER.debug("State: BATTLE");
            memory.setState(State.BATTLE);
            memory.setLastTargetPos(this.getTarget().position());
            memory.setLastSeenTime(System.currentTimeMillis());

        } else if (memory.getState() == State.BATTLE && this.getTarget() == null) {
            memory.setState(State.SEEK);
        }

        if (memory.getState() == State.SEEK) {
            LOGGER.debug("State: SEEK");

            memory.setAllowAttack(false);

            if (this.tickCount % 1200 == 0) {
                memory.setState(State.CALM);
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new GunAttack(this, 75, 1.4f, 0.012f, 3, 5, 10 , 15));
        this.goalSelector.addGoal(2, new SmartCover(this, this.position()));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.1, 40){
            @Override
            public boolean canUse() {
                if (memory.getState() != State.SEEK) {
                    return false;
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers().setUnseenMemoryTicks(600));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    public void equipGun(String gun, String fireMode, int maxAmmo, String scope, String muzzle, String grip) {
        GunSetup.setGun(this, gun, fireMode, maxAmmo, scope, muzzle, grip);
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_) {
        return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_);
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        setNoAi(distanceToClosestPlayer > 64);
        return false;
    }
}
