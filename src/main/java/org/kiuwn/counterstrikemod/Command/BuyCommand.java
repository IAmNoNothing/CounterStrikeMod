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
import org.kiuwn.counterstrikemod.Gameplay.ShopItem;
import org.kiuwn.counterstrikemod.MatchMaking.Match;

import java.util.ArrayList;

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

        ArrayList<ShopItem> items = match.getShop().getItemsWithPath(path);
        if (items.isEmpty()) {
            player.sendSystemMessage(Component.literal("Item " + path + " not found!"));
            return 0;
        }

        if (items.size() == 1) {
            match.buy(path, player);
        } else {
            BuyCommand.printItems(items, player);
        }

        return 1;
    }

    public static int buy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Match match = Counterstrikemod.getInstance().getMatch();
        Player player = context.getSource().getPlayerOrException();

        if (match == null) {
            player.sendSystemMessage(Component.literal("No match found!"));
            return 0;
        }

        if (!match.isStarted()) {
            player.sendSystemMessage(Component.literal("Match is not started!"));
            return 0;
        }

        ArrayList<ShopItem> items = match.getShop().getItemsWithPath("");
        if (items.isEmpty()) {
            player.sendSystemMessage(Component.literal("Shop is empty!"));
            return 1;
        }

        BuyCommand.printItems(items, player);

        return 1;
    }

    private static void printItems(ArrayList<ShopItem> items, Player player) {
        player.sendSystemMessage(Component.literal("Items:"));
        for (ShopItem item : items) {
            String message = "%s: %s - %s".formatted(
                item.path(), item.name(), item.price()
            );

            player.sendSystemMessage(Component.literal(message));
        }
    }
}
