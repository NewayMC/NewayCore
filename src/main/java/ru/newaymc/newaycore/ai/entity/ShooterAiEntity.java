package ru.newaymc.newaycore.ai.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.goals.BorderPatrol;
import ru.newaymc.newaycore.ai.utils.AiShooterSetup;
import ru.newaymc.newaycore.ai.utils.IShooterSetup;
import ru.newaymc.newaycore.gun.entity.GunAmmo;
import ru.newaymc.newaycore.register.ModItems;

import java.util.Random;


public class ShooterAiEntity extends Monster implements IShooterSetup {
    private final PathNavigation nav = this.getNavigation();

    public ShooterAiEntity(EntityType<? extends ShooterAiEntity>  type, Level world) {
        super(type, world);
        xpReward = 3;
        setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.AKM.get()));
        refreshDimensions();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new BorderPatrol(this, 16, 1, 100));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.3, 1024, 10f) {
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
    public void baseTick() {
        super.baseTick();
        buildAi(this);
        if (ShooterCore.memory != null) {
            tickUpdate(this.tickCount);
            tickingGoals();
        }
    }

    @Override
    @AiShooterSetup(speed = 9)
    public void buildAi(PathfinderMob mob) {
        IShooterSetup.super.buildAi(mob);
    }

    @Override
    public void tickingGoals() {
        // Stray
        /*if (ShooterCore.memory.getTarget() != null && this.tickCount % 40 == 0) {
            int rnd = new Random().nextInt(1, 4);
            int move = new Random().nextInt(3, 5);

            switch (rnd) {
                case 1:
                    nav.moveTo(this.getX(), this.getY(), this.getZ() - move, 1);
                    break;
                case 2:
                    nav.moveTo(this.getX(), this.getY(), this.getZ() + move, 1);
                    break;
                case 3:
                    nav.moveTo(this.getX() + move, this.getY(), this.getZ(), 1);
                    break;
                case 4:
                    nav.moveTo(this.getX() - move, this.getY(), this.getZ() + move, 1);
                    break;
            }
        }*/
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
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 100);
        builder = builder.add(Attributes.ARMOR, 10);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 32);
        return builder;
    }
}