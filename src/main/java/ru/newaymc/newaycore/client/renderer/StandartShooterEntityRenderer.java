package ru.newaymc.newaycore.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import ru.newaymc.newaycore.ai.StandartShooterEntity;

public class StandartShooterEntityRenderer extends HumanoidMobRenderer<StandartShooterEntity, HumanoidModel<StandartShooterEntity>> {
    public StandartShooterEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<StandartShooterEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(StandartShooterEntity entity) {
        return ResourceLocation.parse("newaycore:textures/entities/standart.png");
    }
}