package ru.newaymc.newaycore.init;

import net.neoforged.bus.api.SubscribeEvent;
import ru.newaymc.newaycore.client.renderer.StandartShooterEntityRenderer;
import ru.newaymc.newaycore.client.renderer.EliteShooterEntityRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class ModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GUN_AMMO.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.ELITE_SHOOTER_ENTITY.get(), EliteShooterEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.STANDART_SHOOTER_ENTITY.get(), StandartShooterEntityRenderer::new);
    }
}
