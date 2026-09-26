package ru.newaymc.newaycore.register;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import ru.newaymc.newaycore.ai.entity.AbstractShooter;
import ru.newaymc.newaycore.ai.utils.TeamRole;
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
                .then(Commands.literal("world")
                        .then(Commands.literal("prepare").then(Commands.argument("worldId", MessageArgument.message()).executes(ModCommand.WorldArgs::prepare)))
                        .then(Commands.literal("load").then(Commands.argument("worldId", MessageArgument.message()).executes(ModCommand.WorldArgs::load)))
                        .then(Commands.literal("teleport").then(Commands.argument("autoLoad", BoolArgumentType.bool())
                                .then(Commands.argument("coordinates", BlockPosArgument.blockPos()).then(Commands.argument("worldId", MessageArgument.message())
                                        .executes(ModCommand.WorldArgs::teleport))))))
                .then(Commands.literal("ai").then(Commands.literal("squad").executes(ModCommand.AiArgs::spawnSquad)))
        );
    }

    private static class WorldArgs {
        private static int prepare(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            String dimensionPath = MessageArgument.getMessage(context, "worldId").getString();

            Optional<WorldTemplate> worldTemplate = WorldRegister.findDimension(dimensionPath);
            if (worldTemplate.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Dimension not found"));
                return 0;
            }
            DimensionLoader.prepareDimension(worldTemplate.get().getDimensionId());

            context.getSource().sendSuccess(() -> Component.literal("Prepare completed"), false);
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

            context.getSource().sendSuccess(() -> Component.literal("Load completed"), false);
            return 1;
        }

        private static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            boolean autoLoad = BoolArgumentType.getBool(context, "autoLoad");
            String dimensionPath = MessageArgument.getMessage(context, "worldId").getString();
            Vec3 pos = BlockPosArgument.getBlockPos(context, "coordinates").getCenter();

            Player player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Player is null"));
                return 0;
            }

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

    private static class AiArgs {

        private static int spawnSquad(CommandContext<CommandSourceStack> context) {
            Vec3 vec3 = context.getSource().getPosition();
            ServerLevel level = context.getSource().getLevel();

            BlockPos pos = new BlockPos((int) vec3.x(), (int) vec3.y() + 1, (int) vec3.z());

            AbstractShooter commander = ModEntities.SHOOTER_AI_ENTITY.get().spawn(level, pos, MobSpawnType.COMMAND);
            if (commander != null) {
                commander.getMemory().setRole(TeamRole.COMMANDER);
                commander.setCustomName(Component.literal("TeamRole.COMMANDER"));
                commander.setCustomNameVisible(true);
                for (int i = 0; i < 4; i++) {
                    AbstractShooter shooter = ModEntities.SHOOTER_AI_ENTITY.get().spawn(level, pos, MobSpawnType.COMMAND);
                    if (shooter == null) {
                        context.getSource().sendFailure(Component.literal("Unexpected error with shooter spawn"));
                        return 0;
                    }

                    shooter.setCustomName(Component.literal("TeamRole.STANDARD"));
                    shooter.setCustomNameVisible(true);
                }
            } else {
                context.getSource().sendFailure(Component.literal("Unexpected error with commander spawn"));
                return 0;
            }
            return 1;
        }
    }
}
