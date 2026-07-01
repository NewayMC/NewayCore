package ru.newaymc.newaycore.ai.entity;

import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import ru.newaymc.newaycore.ai.ShooterMain;
import ru.newaymc.newaycore.annotation.AiShooterSetup;
import ru.newaymc.newaycore.gun.entity.GunAmmo;
import ru.newaymc.newaycore.register.ModItems;

public class ShooterAiEntity extends Monster implements RangedAttackMob {
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
                return super.canUse() &&  ShooterMain.Ai.getAllowAttack();
            }

            @Override
            public boolean canContinueToUse() {
                double x = ShooterAiEntity.this.getX();
                double y = ShooterAiEntity.this.getY();
                double z = ShooterAiEntity.this.getZ();
                Entity entity = ShooterAiEntity.this;
                Level world = ShooterAiEntity.this.level();
                return super.canContinueToUse() && ShooterMain.Ai.getAllowAttack();
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
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

//    @Override
//    public void die(DamageSource ds) {
//        super.die(ds);
//        File file = ShooterMain.file;
//        if (file.exists()) {
//            try {
//                boolean delete = file.delete();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    @Override
    public boolean shouldDropLoot() {
        return false;
    }

    @Override
    @AiShooterSetup
    public void baseTick() {
        super.baseTick();
        ShooterMain.setup(this.level(), this.getX(), this.getY(), this.getZ(), this);
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