package ru.newaymc.newaycore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.EliteShooterEntity;
import ru.newaymc.newaycore.ai.StandartShooterEntity;
import ru.newaymc.newaycore.gun.entity.GunAmmo;

@EventBusSubscriber
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, NewaycoreMod.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<GunAmmo>> GUN_AMMO = register("gun_ammo",
            EntityType.Builder.<GunAmmo>of(GunAmmo::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
    public static final DeferredHolder<EntityType<?>, EntityType<EliteShooterEntity>> ELITE_SHOOTER_ENTITY = register("elite_shooter_entity",
            EntityType.Builder.<EliteShooterEntity>of(EliteShooterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

                    .ridingOffset(-0.6f).sized(0.6f, 1.8f));
    public static final DeferredHolder<EntityType<?>, EntityType<StandartShooterEntity>> STANDART_SHOOTER_ENTITY = register("standart_shooter_entity",
            EntityType.Builder.<StandartShooterEntity>of(StandartShooterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

                    .ridingOffset(-0.6f).sized(0.6f, 1.8f));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
    }

    @SubscribeEvent
    public static void init(RegisterSpawnPlacementsEvent event) {
        EliteShooterEntity.init(event);
        StandartShooterEntity.init(event);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ELITE_SHOOTER_ENTITY.get(), EliteShooterEntity.createAttributes().build());
        event.put(STANDART_SHOOTER_ENTITY.get(), StandartShooterEntity.createAttributes().build());
    }
}