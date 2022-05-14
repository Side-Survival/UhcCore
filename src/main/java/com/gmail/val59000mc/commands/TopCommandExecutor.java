package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommandExecutor implements CommandExecutor{

    private final PlayerManager playerManager;

    public TopCommandExecutor(PlayerManager playerManager){
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)){
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        player.sendMessage(Lang.COMMAND_TOP_HEADER);
        for (String s : GameManager.getGameManager().getPointHandler().getAll()) {
            player.sendMessage(s);
        }
        player.sendMessage("");

        return true;
    }

}