package ru.newaymc.newaycore.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.ai.EliteShooterEntity;
import ru.newaymc.newaycore.entity.GunAmmoEntity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntitiesInit {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NewaycoreMod.MODID);

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> entityTypeBuilder.build(registryname));
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EliteShooterEntity.init();
        });
    }    public static final RegistryObject<EntityType<GunAmmoEntity>> GUN_AMMO = register("gun_ammo",
            EntityType.Builder.<GunAmmoEntity>of(GunAmmoEntity::new, MobCategory.MISC).setCustomClientFactory(GunAmmoEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ELITE_SHOOTER_ENTITY.get(), EliteShooterEntity.createAttributes().build());
    }

    public static final RegistryObject<EntityType<EliteShooterEntity>> ELITE_SHOOTER_ENTITY = register("elite_shooter_entity",
            EntityType.Builder.<EliteShooterEntity>of(EliteShooterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(EliteShooterEntity::new)

                    .sized(0.6f, 1.8f));




}