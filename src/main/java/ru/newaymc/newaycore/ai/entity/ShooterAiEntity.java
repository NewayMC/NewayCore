package ru.newaymc.newaycore.ai.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;


public class ShooterAiEntity extends AbstractShooter {

    public ShooterAiEntity(EntityType<? extends ShooterAiEntity>  type, Level world) {
        super(type, world);
        xpReward = 3;
        setPersistenceRequired();
        refreshDimensions();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        setTargets(Player.class);
    }

    @Override
    public void equipGun(String gun, String fireMode, int maxAmmo, String scope, String muzzle, String grip) {
        super.equipGun("ak47", "auto", 31, null, null, null);
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

//        this.goalSelector.addGoal(2, new BorderPatrol(this, 16, 1, 100));
// Stray
//        if (ShooterCore.memory.getTarget() != null && this.tickCount % 40 == 0) {
//            int rnd = new Random().nextInt(1, 4);
//            int move = new Random().nextInt(3, 5);
//
//            switch (rnd) {
//                case 1:
//                    nav.moveTo(this.getX(), this.getY(), this.getZ() - move, 1);
//                    break;
//                case 2:
//                    nav.moveTo(this.getX(), this.getY(), this.getZ() + move, 1);
//                    break;
//                case 3:
//                    nav.moveTo(this.getX() + move, this.getY(), this.getZ(), 1);
//                    break;
//                case 4:
//                    nav.moveTo(this.getX() - move, this.getY(), this.getZ() + move, 1);
//                    break;
//            }
//        }


    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.1f);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {

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