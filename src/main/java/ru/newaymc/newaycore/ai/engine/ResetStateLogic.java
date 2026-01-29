package ru.newaymc.newaycore.ai.engine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import ru.newaymc.newaycore.network.NewaycoreModVariables;

public class ResetStateLogic {
    public static void execute(LevelAccessor world) {
        NewaycoreModVariables.MapVariables.get(world).AIstate = false;
        NewaycoreModVariables.MapVariables.get(world).markSyncDirty();
        if (world instanceof ServerLevel _level) {
            _level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("\u0421\u043E\u0441\u0442\u043E\u044F\u043D\u0438\u0435 \u0418\u0418 \u0431\u044B\u043B\u043E \u0441\u0431\u0440\u043E\u0448\u0435\u043D\u043E."), false);
        }
    }
}