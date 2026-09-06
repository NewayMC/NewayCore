package ru.newaymc.newaycore.register;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import ru.newaymc.newaycore.ai.entity.renderer.ShooterAiEntityRenderer;

@EventBusSubscriber(Dist.CLIENT)
public class ModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHOOTER_AI_ENTITY.get(), ShooterAiEntityRenderer::new);
    }
}
