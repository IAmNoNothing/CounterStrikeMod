package org.kiuwn.counterstrikemod.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.kiuwn.counterstrikemod.MatchMaking.MapManager;
import org.kiuwn.counterstrikemod.MatchMaking.MatchMap;
import org.kiuwn.counterstrikemod.MatchMaking.MatchMode;
import org.kiuwn.counterstrikemod.Counterstrikemod;

import java.util.HashMap;

public class MapCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("map")
                .then(Commands.literal("create")
                    .then(Commands.argument("mapName", StringArgumentType.string())
                        .then(Commands.argument("mode", StringArgumentType.word())
                            .executes(MapCommand::createMap))))
                .then(Commands.literal("list")
                    .executes(MapCommand::listMap))
                .then(Commands.literal("delete")
                    .then(Commands.argument("mapName", StringArgumentType.string())
                        .executes(MapCommand::deleteMap)))
                .then(Commands.literal("add")
                    .then(Commands.literal("spawn")
                        .then(Commands.argument("mapName", StringArgumentType.string())
                            .then(Commands.argument("teamName", StringArgumentType.string())
                                .executes(MapCommand::addSpawnPosition)))))
                .then(Commands.literal("remove")
                    .then(Commands.literal("spawn")
                        .then(Commands.argument("mapName", StringArgumentType.string())
                            .then(Commands.argument("teamName", StringArgumentType.string())
                                .then(Commands.argument("index", IntegerArgumentType.integer())
                                        .executes(MapCommand::removeSpawnPosition))))))
                .then(Commands.literal("get")
                    .then(Commands.argument("mapName", StringArgumentType.string())
                        .then(Commands.literal("spawns")
                            .executes(MapCommand::getSpawnPositions))))
                    .then(Commands.literal("savepath")
                        .executes(MapCommand::savePath))
                .then(Commands.literal("save")
                    .executes(MapCommand::saveMap))

        );
    }

    public static int createMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");
        try {
            MatchMode mode = MatchMode.valueOf(StringArgumentType.getString(context, "mode"));
            if (Counterstrikemod.getInstance().getMapManager().getMaps().containsKey(mapName)) {
                player.sendSystemMessage(Component.literal("Map " + mapName + " already exists"));
                return 0;
            }
            Counterstrikemod.getInstance().getMapManager().addMap(mapName, mode);
            player.sendSystemMessage(Component.literal("Created map " + mapName));
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.literal("Invalid mode: " + StringArgumentType.getString(context, "mode")));
            return 0;
        }
        return 1;
    }

    public static int listMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("List of maps:"));
        HashMap<String, MatchMap> maps = Counterstrikemod.getInstance().getMapManager().getMaps();
        String[] mapNames = maps.keySet().toArray(new String[0]);

        if (mapNames.length > 0) {
            for (String mapName : mapNames) {
                player.sendSystemMessage(Component.literal("- " + mapName + "(" + maps.get(mapName).getMode() + ")"));
            }
        } else {
            player.sendSystemMessage(Component.literal("No maps found"));
        }

        return 1;
    }

    public static int deleteMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");

        Counterstrikemod.getInstance().getMapManager().removeMap(mapName);
        player.sendSystemMessage(Component.literal("Deleted map " + mapName));
        return 1;
    }

    public static int addSpawnPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");
        String teamName = StringArgumentType.getString(context, "teamName");
        Vec3 position = player.position();
        MatchMap map = Counterstrikemod.getInstance().getMapManager().getMaps().get(mapName);

        if (map == null) {
            player.sendSystemMessage(Component.literal("Map " + mapName + " doesn't exist"));
            return 0;
        }

        map.addSpawnPosition(teamName, position);
        player.sendSystemMessage(Component.literal("Added spawn position for team " + teamName + " to map " + mapName));

        return 1;
    }

    public static int removeSpawnPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");
        String teamName = StringArgumentType.getString(context, "teamName");
        int index = IntegerArgumentType.getInteger(context, "index");
        MatchMap map = Counterstrikemod.getInstance().getMapManager().getMaps().get(mapName);

        if (map == null) {
            player.sendSystemMessage(Component.literal("Map " + mapName + " doesn't exist"));
            return 0;
        }

        map.removeSpawnPosition(teamName, index);
        player.sendSystemMessage(Component.literal("Removed spawn position for team " + teamName + " from map " + mapName));

        return 1;
    }

    public static int getSpawnPositions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String mapName = StringArgumentType.getString(context, "mapName");
        MatchMap map = Counterstrikemod.getInstance().getMapManager().getMaps().get(mapName);

        if (map == null) {
            player.sendSystemMessage(Component.literal("Map " + mapName + " doesn't exist"));
            return 0;
        }

        player.sendSystemMessage(Component.literal("Spawn positions for map " + mapName + ":"));
        for (String teamName : map.getSpawnPositions().keySet()) {
            player.sendSystemMessage(Component.literal("For team: " + teamName));
            for (Vec3 position : map.getSpawnPositions().get(teamName)) {
                player.sendSystemMessage(Component.literal("    - " + position));
            }
        }

        return 1;
    }

    public static int savePath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        String path = Counterstrikemod.getInstance().getMapManager().getFile().getAbsolutePath();
        player.sendSystemMessage(Component.literal("Save path: " + path));
        return 1;
    }

    public static int saveMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        MapManager.getInstance().save();
        player.sendSystemMessage(Component.literal("Maps saved."));
        return 1;
    }
}
