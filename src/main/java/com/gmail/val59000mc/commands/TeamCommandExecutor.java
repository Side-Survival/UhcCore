package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommandExecutor implements CommandExecutor{

    private final GameManager gameManager;

    public TeamCommandExecutor(GameManager gameManager){
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        PlayerManager pm = gameManager.getPlayerManager();
        UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

        if (args.length == 0){
            player.sendMessage("Send command help");
            return true;
        }

        // Don't allow the creation of teams during the game.
        if (gameManager.getGameState() != GameState.WAITING){
            return true;
        }

        String subCommand = args[0].toLowerCase();

        player.sendMessage("Invalid sub command");
        return true;
    }

}