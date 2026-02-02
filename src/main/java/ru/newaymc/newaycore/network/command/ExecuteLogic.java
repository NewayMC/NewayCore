package ru.newaymc.newaycore.network.command;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;

// Logic for execute param in MainCommand
public class ExecuteLogic {
    public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
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