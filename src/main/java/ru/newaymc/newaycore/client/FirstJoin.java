package ru.newaymc.newaycore.client;

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.newaymc.newaycore.network.vars.ModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class FirstJoin {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        execute(event, event.getEntity().level());
    }

    public static void execute(LevelAccessor world) {
        execute(null, world);
    }


    private static void execute(@Nullable Event event, LevelAccessor world) {
        if (!ModVariables.MapVariables.get(world).FirstJoin) {
            ModVariables.MapVariables.get(world).FirstJoin = true;
        }
    }
}