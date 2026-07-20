package ru.newaymc.newaycore.register;

import net.neoforged.bus.api.SubscribeEvent;
import ru.newaymc.newaycore.client.renderer.ShooterAiEntityRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class ModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHOOTER_AI_ENTITY.get(), ShooterAiEntityRenderer::new);
    }
}
