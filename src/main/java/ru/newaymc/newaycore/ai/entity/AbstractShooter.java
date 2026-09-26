package ru.newaymc.newaycore.ai.entity;

import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
import ru.newaymc.newaycore.ai.utils.State;
import ru.newaymc.newaycore.ai.memory.Memory;
import ru.newaymc.newaycore.ai.GunSetup;
import ru.newaymc.newaycore.ai.utils.Order;
import ru.newaymc.newaycore.ai.utils.TeamRole;

import java.util.List;
import java.util.Objects;

public abstract class AbstractShooter extends Monster {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/AbstractShooter");
    private static final boolean debug = true;

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

        findCommander();
        if (memory.getCommander() != null) {
            memory.setCurrentOrder(memory.getCommander().getMemory().getCurrentOrder());
        }

        commander();

        if (this.getTarget() != null) {
            battle();
        } else if (memory.getState() == State.BATTLE &&  this.getTarget() == null) {
            alerted();
        }

        if (this.tickCount % 600 == 0) {
            for (AbstractShooter shooter : memory.getTeammates()) {
                if (shooter.getMemory().getState() == State.BATTLE) {
                    memory.setState(State.ALERTED);
                    break;
                }
            }
        }

        if (memory.getCommander() != null && !memory.getCommander().isAlive()) memory.setCurrentOrder(Order.RETREAT);
    }

    protected void battle() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        //LOGGER.debug("State: BATTLE");

        memory.setState(State.BATTLE);
        memory.setTarget(target);
        memory.setLastTargetPos(target.position());

        if (!memory.getTarget().isAlive()) {
            memory.setState(State.CALM);
        }
    }

    protected void alerted() {
        memory.setState(State.ALERTED);
        //LOGGER.debug("State: ALERTED");

        Vec3 currentPos = this.position();
        if (memory.getLastTargetPos() != null) {
            Vec3 targetPos = memory.getLastTargetPos();
            nav.moveTo(targetPos.x(), targetPos.y(), targetPos.z(), 1.2);
            if (this.distanceToSqr(targetPos) <= 1) {
                memory.setLastTargetPos(null);
                nav.moveTo(currentPos.x(), currentPos.y(), currentPos.z(), 1);

                if (memory.getCommander() == null) {
                    memory.setAllowPatrol(true);
                }
            }
        }

        memory.setAllowAttack(false);

        if (memory.getCurrentOrder() == Order.NONE) {
            if (this.tickCount % 1200 == 0) {
                memory.setState(State.CALM);

                calm();
            }
        }
    }

    protected void calm() {
        //LOGGER.debug("State: CALM");

        memory.setTarget(null);
        memory.setAllowPatrol(false);
    }

    private List<AbstractShooter> findTeammates() {
        AABB searchBox = this.getBoundingBox().inflate(24);
        return this.level().getEntitiesOfClass(AbstractShooter.class, searchBox, entity -> entity != this);
    }

    private void findCommander() {
        if (memory.getRole() == TeamRole.COMMANDER) {
            return;
        }

        if (memory.getCommander() != null) {
            return;
        }

        for (AbstractShooter shooter : memory.getTeammates()) {
            if (shooter.getMemory().getRole() == TeamRole.COMMANDER) {
                memory.setCommander(shooter);
                shooter.getMemory().getSquadmates().add(this);
                break;
            }
        }
    }

    /**
     * [WIP] Main logic method for squadron commander
     */
    protected void commander() {
        if (memory.getRole() != TeamRole.COMMANDER) {
            return;
        }

        AttributeModifier maxHealthM = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(NewaycoreMod.MODID, "commander_health"), this.getMaxHealth() * 1.5f - this.getMaxHealth(), AttributeModifier.Operation.ADD_VALUE);
        if (!this.getAttribute(Attributes.MAX_HEALTH).hasModifier(maxHealthM.id())) {
            this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(maxHealthM);
        }
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
                if (!memory.isAllowPatrol() || memory.getCurrentOrder() != Order.SEEK) {
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
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!debug) {
            return super.mobInteract(player, hand);
        }

        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.literal(memory.toString()), false);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (distanceToClosestPlayer > 64) {
            setNoAi(true);
            if (memory.getCurrentOrder() == Order.RETREAT) {
                this.discard();
            }
        } else {
            setNoAi(false);
        }

        return false;
    }
}
