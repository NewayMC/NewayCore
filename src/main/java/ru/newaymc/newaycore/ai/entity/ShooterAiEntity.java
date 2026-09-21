package ru.newaymc.newaycore.ai.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Test entity
 */
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