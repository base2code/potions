package de.base2code.potions.commands;

import de.base2code.potions.Potions;
import de.base2code.potions.utils.UUIDFetcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PotionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission("potion.blacklist")) {
            commandSender.sendMessage(Potions.getInstance().getMessage("no-permission"));
            return false;
        }

        if (args.length != 2) {
            sendHelp(commandSender);
            return false;
        }

        if (!args[0].equalsIgnoreCase("blacklist")) {
            sendHelp(commandSender);
            return false;
        }

        // Get player uuid to add / remove from blacklist
        String playerName = args[1];
        Player player = Potions.getInstance().getServer().getPlayer(playerName);

        // Run async so that the main thread is not blocked (UUIDFetcher)
        CompletableFuture.runAsync(() -> {
            UUID uuid = null;
            if (player != null) {
                uuid = player.getUniqueId();
            } else {
                uuid = UUIDFetcher.getUUID(playerName);
            }

            if (uuid == null) {
                commandSender.sendMessage(Potions.getInstance().getMessage("not-found"));
                return;
            }

            if (Potions.getInstance().getBlacklistManager().containsUser(uuid)) {
                Potions.getInstance().getBlacklistManager().removeUser(uuid);
                commandSender.sendMessage(Potions.getInstance().getMessage("commands.potion.blacklist.remove").replace("%player%", playerName));
            } else {
                Potions.getInstance().getBlacklistManager().addUser(uuid);
                commandSender.sendMessage(Potions.getInstance().getMessage("commands.potion.blacklist.add").replace("%player%", playerName));
            }
        });
        return true;
    }

    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage(Potions.getInstance().getMessage("commands.potion.help"));
    }
}
