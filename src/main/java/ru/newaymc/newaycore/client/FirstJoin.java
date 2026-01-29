package ru.newaymc.newaycore.client;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.newaymc.newaycore.network.NewaycoreModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class FirstJoin {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        execute(event);
    }

    public static void execute() {
        execute(null);
    }

    private static void execute(@Nullable Event event) {
        if (!NewaycoreModVariables.FirstJoin) {
            NewaycoreModVariables.FirstJoin = true;
        }
    }
}