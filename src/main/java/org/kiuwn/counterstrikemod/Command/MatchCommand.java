package org.kiuwn.counterstrikemod.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.kiuwn.counterstrikemod.Counterstrikemod;
import org.kiuwn.counterstrikemod.MatchMaking.MapManager;
import org.kiuwn.counterstrikemod.MatchMaking.Match;
import org.kiuwn.counterstrikemod.MatchMaking.MatchMap;

public class MatchCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("match")
                .then(Commands.literal("create")
                    .then(Commands.argument("mapName", StringArgumentType.string())
                        .executes(MatchCommand::createMatch)))
                .then(Commands.literal("start")
                    .executes(MatchCommand::startMatch))
                .then(Commands.literal("stop")
                    .executes(MatchCommand::stopMatch))
                .then(Commands.literal("restart")
                    .executes(MatchCommand::restartMatch))
        );
    }

    public static int createMatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");

        if (!MapManager.getInstance().getMaps().containsKey(mapName)) {
            player.sendSystemMessage(Component.literal("Map " + mapName + " not found!"));
            return 0;
        }

        MatchMap map = MapManager.getInstance().getMaps().get(mapName);
        Match match = new Match(map);
        Counterstrikemod.getInstance().setMatch(match);

        player.sendSystemMessage(Component.literal("Match " + mapName + " created!"));

        return 1;
    }

    public static int startMatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Match match = Counterstrikemod.getInstance().getMatch();

        if (match == null) {
            player.sendSystemMessage(Component.literal("No match found!"));
            player.sendSystemMessage(Component.literal("Use /match create <mapName> to create one."));
            return 0;
        }

        if (match.isStarted()) {
            player.sendSystemMessage(Component.literal("Match already started!"));
            player.sendSystemMessage(Component.literal("Use /match stop to stop existing match."));
            return 0;
        }

        match.start();
        player.sendSystemMessage(Component.literal("Match on map " + match.getMap().getName() + " has been started!"));

        return 1;
    }

    public static int stopMatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Match match = Counterstrikemod.getInstance().getMatch();

        if (match == null) {
            player.sendSystemMessage(Component.literal("No match found!"));
            return 0;
        }

        if (!match.isStarted()) {
            player.sendSystemMessage(Component.literal("Match not started!"));
            return 0;
        }

        match.stop();
        player.sendSystemMessage(Component.literal("Match on map " + match.getMap().getName() + " has been stopped!"));
        return 1;
    }

    public static int restartMatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Match match = Counterstrikemod.getInstance().getMatch();

        if (match == null) {
            player.sendSystemMessage(Component.literal("No match found!"));
            player.sendSystemMessage(Component.literal("Use /match create <mapName> to create one."));
            return 0;
        }

        if (match.isStarted()) {
            match.stop();
        }

        match.start();

        return 1;
    }
}
