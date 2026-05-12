package ru.newaymc.newaycore.network;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import ru.newaymc.newaycore.als.faction.FactionLoader;
import ru.newaymc.newaycore.als.outpost.OutpostLoader;

@EventBusSubscriber
public class MainCommand {
        @SubscribeEvent
        public static void registerCommand(RegisterCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("newaycore").requires(s -> s.hasPermission(4)).then(Commands.literal("als").then(Commands.literal("reload").executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();

                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();

                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);

                Direction direction = Direction.DOWN;
                if (entity != null)
                    direction = entity.getDirection();

                FactionLoader.execute();
                return 0;
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();

                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();

                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);

                Direction direction = Direction.DOWN;
                if (entity != null)
                    direction = entity.getDirection();

                OutpostLoader.execute(world);
                return 0;
            }))).then(Commands.literal("execute").then(Commands.argument("command", MessageArgument.message()).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();

                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();

                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);

                Direction direction = Direction.DOWN;
                if (entity != null)
                    direction = entity.getDirection();

                MainCommand.Logic.executeParam(arguments, entity);
                return 0;
            }))));
        }

    public class Logic {
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
}
