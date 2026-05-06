package ru.newaymc.newaycore.network.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class MainCommandLogic {
    public static void executeParam(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null)
            return;
        {
            Entity _ent = entity;
            if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                _ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
                        _ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (commandParameterMessage(arguments, "command")));
            }
        }
    }

    private static String commandParameterMessage(CommandContext<CommandSourceStack> arguments, String parameter) {
        try {
            return MessageArgument.getMessage(arguments, parameter).getString();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }
}
