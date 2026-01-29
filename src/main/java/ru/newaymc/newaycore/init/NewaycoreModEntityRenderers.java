/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package ru.newaymc.newaycore.init;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.newaymc.newaycore.client.renderer.EliteShooterEntityRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NewaycoreModEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NewaycoreModEntities.GUN_AMMO.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(NewaycoreModEntities.ELITE_SHOOTER_ENTITY.get(), EliteShooterEntityRenderer::new);
    }
}