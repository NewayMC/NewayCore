/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package ru.newaymc.newaycore.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import ru.newaymc.newaycore.NewaycoreMod;
import ru.newaymc.newaycore.network.ResetStateMessage;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NewaycoreModKeyMappings {
    public static final KeyMapping RESET_STATE = new KeyMapping("key.newaycore.reset_state", GLFW.GLFW_KEY_Z, "key.categories.misc") {
        private boolean isDownOld = false;

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (isDownOld != isDown && isDown) {
                NewaycoreMod.PACKET_HANDLER.sendToServer(new ResetStateMessage(0, 0));
                ResetStateMessage.pressAction(Minecraft.getInstance().player, 0, 0);
            }
            isDownOld = isDown;
        }
    };

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RESET_STATE);
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (Minecraft.getInstance().screen == null) {
                RESET_STATE.consumeClick();
            }
        }
    }
}