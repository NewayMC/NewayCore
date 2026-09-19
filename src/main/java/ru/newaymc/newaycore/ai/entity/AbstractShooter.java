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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.goals.GunAttack;
import ru.newaymc.newaycore.ai.goals.SmartCover;
import ru.newaymc.newaycore.ai.utils.Memory;
import ru.newaymc.newaycore.ai.GunSetup;
import ru.newaymc.newaycore.ai.utils.State;

import java.util.List;
import java.util.Objects;

public abstract class AbstractShooter extends Monster {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/AbstractShooter");
    @Getter
    private final Memory memory = new Memory(this);
    private final PathNavigation nav = this.getNavigation();

    protected AbstractShooter(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (isNoAi()) {
            return;
        }
        memory.setTeammates(findTeammates());

        if (this.getTarget() != null) {
            battle();
        } else if (memory.getState() == State.BATTLE &&  this.getTarget() == null) {
            seek();
        }
    }

    protected void battle() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        LOGGER.debug("State: BATTLE");

        memory.setState(State.BATTLE);
        memory.setTarget(target);
        memory.setLastTargetPos(target.position());
        memory.setLastSeenTime(System.currentTimeMillis());

        if (!memory.getTarget().isAlive()) {
            memory.setState(State.CALM);
        }
    }

    protected void seek() {
        memory.setState(State.SEEK);
        LOGGER.debug("State: SEEK");

        if (memory.getLastTargetPos() != null) {
            Vec3 targetPos = memory.getLastTargetPos();
            nav.moveTo(targetPos.x(), targetPos.y(), targetPos.z(), 1.2);
            if (this.distanceToSqr(targetPos) <= 1) {
                memory.setAllowPatrol(true);
                memory.setLastTargetPos(null);
            }
        }

        memory.setAllowAttack(false);

        if (this.tickCount % 1200 == 0) {
            memory.setState(State.CALM);
            memory.setTarget(null);

            calm();
        }
    }

    protected void calm() {
        LOGGER.debug("State: CALM");
    }

    private List<AbstractShooter> findTeammates() {
        AABB searchBox = this.getBoundingBox().inflate(16);
        return this.level().getEntitiesOfClass(AbstractShooter.class, searchBox, entity -> entity != this);
    }

    @SafeVarargs
    public final void setTargets(Class<? extends LivingEntity>... classes) {
        for (Class<? extends LivingEntity> clazz : classes) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, clazz, true).setUnseenMemoryTicks(500));
        }
    }

    protected GunSetup.GunSettings equipGun() {
        return GunSetup.buildSettings(ResourceLocation.fromNamespaceAndPath("tacz", "ak47"), "auto", 31, null, null, null);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new SmartCover(this, this.position()));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.1, 20){
            @Override
            public boolean canUse() {
                if (!memory.isAllowPatrol()) {
                    return false;
                }
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(4, new HurtByTargetGoal(this).setAlertOthers().setUnseenMemoryTicks(600));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_) {
        GunSetup.setGun(this, equipGun());

        this.goalSelector.addGoal(1, new GunAttack(this, 64, 1.4f, 0.012f, 3, 5, 10 , 15));
        return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_);
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(DamageSource ds) {
        return Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death")));
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt")));
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
