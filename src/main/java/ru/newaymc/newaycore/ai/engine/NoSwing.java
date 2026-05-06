package ru.newaymc.newaycore.ai.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "newaycore", bus = EventBusSubscriber.Bus.GAME)
public class NoSwing {

    public static ItemStack lastMainHandItem = ItemStack.EMPTY;

    private static final ResourceLocation GUN_TAG = ResourceLocation.parse("neoforge:guns");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onEventTriggered(RenderPlayerEvent.Pre event) {
        var entity = event.getEntity();
        ItemStack item = ItemStack.EMPTY;

        if (entity instanceof LivingEntity living)
            item = living.getMainHandItem();

        if (item.is(ItemTags.create(GUN_TAG))) {
            PlayerModel<?> model = (PlayerModel<?>) event.getRenderer().getModel();

            model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;

            model.rightArm.xRot = (float) Math.toRadians(-45);
            model.rightArm.yRot = (float) Math.toRadians(-5);
            model.leftArm.xRot = (float) Math.toRadians(-30);
            model.leftArm.yRot = (float) Math.toRadians(15);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        ItemStack currentItem = player.getMainHandItem();

        if (currentItem.is(ItemTags.create(GUN_TAG))) {
            player.swinging = false;
            player.swingTime = 0;
            player.swingingArm = null;
        }

        if (!ItemStack.isSameItemSameComponents(currentItem, lastMainHandItem)) {

            if (currentItem.is(ItemTags.create(GUN_TAG))) {
                player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(0);
                player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(0);
                player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(1024D);
            } else {
                player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5D);
                player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(3D);
                player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4D);
            }
        }

        lastMainHandItem = currentItem.copy();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && player.getMainHandItem().is(ItemTags.create(GUN_TAG))) {
            player.swinging = false;
            player.swingTime = 0;
            player.swingingArm = null;
        }
    }
}
