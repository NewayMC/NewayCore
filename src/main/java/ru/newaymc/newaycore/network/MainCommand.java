package ru.newaymc.newaycore.network;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.newaymc.newaycore.factions.FactionRegister;
import ru.newaymc.newaycore.network.command.ExecuteLogic;

// Now, it only needs for some tests
@Mod.EventBusSubscriber
public class MainCommand {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("newaycore")

                .then(Commands.literal("reload").executes(arguments -> {
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

                    FactionRegister.execute();
                    return 0;


                })).then(Commands.literal("execute").then(Commands.argument("command", MessageArgument.message()).executes(arguments -> {
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

                    ExecuteLogic.execute(arguments, entity);
                    return 0;
                }))));
    }

}
