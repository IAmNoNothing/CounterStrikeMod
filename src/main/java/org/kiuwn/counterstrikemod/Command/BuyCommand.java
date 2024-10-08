package org.kiuwn.counterstrikemod.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.kiuwn.counterstrikemod.Counterstrikemod;
import org.kiuwn.counterstrikemod.MatchMaking.Match;

public class BuyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("b")
                .then(Commands.argument("shopPath", StringArgumentType.word())
                    .executes(BuyCommand::buyShopPath))
                .executes(BuyCommand::buy)
        );
    }

    public static int buyShopPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Match match = Counterstrikemod.getInstance().getMatch();
        if (match == null) {
            context.getSource().sendSystemMessage(Component.literal("No match found!"));
            return 0;
        }

        if (!match.isStarted()) {
            context.getSource().sendSystemMessage(Component.literal("Match is not started!"));
            return 0;
        }

        String path = StringArgumentType.getString(context, "shopPath");
        Player player = context.getSource().getPlayerOrException();

        try {
            Integer.parseInt(path);
        } catch (NumberFormatException e) {
            player.sendSystemMessage(Component.literal("Invalid path: " + path));
            return 0;
        }

        match.buy(path, player);

        return 1;
    }

    public static int buy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return 0;
    }
}
