package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.listeners.PlayerChatListener;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StaffChatCmd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("uhc-core.commands.staffchat")) {
            return false;
        }

        UUID uuid = p.getUniqueId();

        if (PlayerChatListener.staffChatToggled.contains(uuid)) {
            PlayerChatListener.staffChatToggled.remove(uuid);
            sender.sendMessage(RandomUtils.color("&7Staff chat &cDISABLED&7!"));
        } else {
            PlayerChatListener.staffChatToggled.add(uuid);
            sender.sendMessage(RandomUtils.color("&7Staff chat &aENABLED&7!"));
        }

        return true;
    }
}
