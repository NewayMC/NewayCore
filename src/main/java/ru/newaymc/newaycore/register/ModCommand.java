package ru.newaymc.newaycore.register;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import ru.newaymc.newaycore.worlds.DimensionLoader;
import ru.newaymc.newaycore.worlds.build.WorldRegister;
import ru.newaymc.newaycore.worlds.build.WorldTemplate;

import java.util.Optional;

@EventBusSubscriber
public class ModCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("newaycore").requires(s -> s.hasPermission(4))
                .then(Commands.literal("prepare").then(Commands.argument("worldId", MessageArgument.message()).executes(ModCommand::prepare)))
                .then(Commands.literal("load").then(Commands.argument("worldId", MessageArgument.message()).executes(ModCommand::load)))
                .then(Commands.literal("teleport").then(Commands.argument("autoLoad", BoolArgumentType.bool())
                        .then(Commands.argument("coordinates", BlockPosArgument.blockPos()).then(Commands.argument("worldId", MessageArgument.message())
                                .executes(ModCommand::teleport)))))
        );
    }

    private static int prepare(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String dimensionPath = MessageArgument.getMessage(context, "worldId").getString();

        Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimensionPath);
        if (worldTemplate.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Dimension not found"));
            return 0;
        }
        DimensionLoader.prepareDimension(worldTemplate.get().getDimensionId());

        context.getSource().sendSuccess(() -> Component.literal("Preparing."), false);
        return 1;
    }

    private static int load(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String dimensionPath = MessageArgument.getMessage(context, "worldId").getString();

        Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimensionPath);
        if (worldTemplate.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Dimension not found"));
            return 0;
        }
        DimensionLoader.loadDimension(worldTemplate.get().getDimensionId());

        context.getSource().sendSuccess(() -> Component.literal("Loading."), false);
        return 1;
    }

    private static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean autoLoad = BoolArgumentType.getBool(context, "autoLoad");
        String dimensionPath = MessageArgument.getMessage(context, "worldId").getString();
        Vec3 pos = BlockPosArgument.getBlockPos(context, "coordinates").getCenter();
        Player player = context.getSource().getPlayer();

        Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimensionPath);
        if (worldTemplate.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Dimension not found"));
            return 0;
        }
        DimensionLoader.teleportToWorld(player, pos, worldTemplate.get().getDimensionId(), false);

        context.getSource().sendSuccess(() -> Component.literal("Teleportation."), autoLoad);
        return 1;
    }
}
