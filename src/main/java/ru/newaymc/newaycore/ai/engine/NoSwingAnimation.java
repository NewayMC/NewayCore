package ru.newaymc.newaycore.ai.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class NoSwingAnimation {
    public static ItemStack lastMainHandItem = ItemStack.EMPTY;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onEventTriggered(RenderPlayerEvent.Pre event) {
        Entity entity = event.getEntity();
        ItemStack item = ItemStack.EMPTY;
        if (entity instanceof LivingEntity living) {
            item = living.getMainHandItem();
        }
        if (item.is(ItemTags.create(new ResourceLocation("forge:guns")))) {
            PlayerModel<?> model = event.getRenderer().getModel();
            model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            model.rightArm.xRot = (float) Math.toRadians(-45);
            model.rightArm.yRot = (float) Math.toRadians(-5);
            model.rightArm.zRot = 0.0F;
            model.leftArm.xRot = (float) Math.toRadians(-30);
            model.leftArm.yRot = (float) Math.toRadians(15);
            model.leftArm.zRot = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player != null) {
            if (event.player.level().isClientSide)
                return;
            ItemStack currentItem = event.player.getMainHandItem();
            if (currentItem.is(ItemTags.create(new ResourceLocation("forge:guns")))) {
                event.player.swinging = false;
                event.player.swingTime = 0;
                event.player.swingingArm = null;
            }
            if (currentItem != lastMainHandItem) {
                if (currentItem.is(ItemTags.create(new ResourceLocation("forge:guns"))) && currentItem != ItemStack.EMPTY) {
                    event.player.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(0);
                    event.player.getAttribute(ForgeMod.ENTITY_REACH.get()).setBaseValue(0);
                    event.player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(1024D);
                } else if (!lastMainHandItem.is(ItemTags.create(new ResourceLocation("forge:guns"))) || currentItem == ItemStack.EMPTY) {
                    event.player.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(4.5D);
                    event.player.getAttribute(ForgeMod.ENTITY_REACH.get()).setBaseValue(3D);
                    event.player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4D);
                }
            }
            lastMainHandItem = event.player.getMainHandItem().copy();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null) {
                if (player.getMainHandItem().is(ItemTags.create(new ResourceLocation("forge:guns")))) {
                    player.swinging = false;
                    player.swingTime = 0;
                    player.swingingArm = null;
                }
            }
        }
    }

    public static void execute() {
        execute(null);
    }

    private static void execute(@Nullable Event event) {
    }
}