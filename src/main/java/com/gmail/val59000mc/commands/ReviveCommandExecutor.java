package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.MojangUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReviveCommandExecutor implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;

    public ReviveCommandExecutor(GameManager gameManager){
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1 && args.length != 2){
            sender.sendMessage(ChatColor.RED + "Correct usage: '/revive <player>' or use '/revive <player> clear' to respawn the player without giving their items back.");
            return true;
        }

        if (gameManager.getGameState() != GameState.PLAYING && gameManager.getGameState() != GameState.DEATHMATCH){
            sender.sendMessage(ChatColor.RED + "You can only use this command while playing!");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Loading player data ...");

        String name = args[0];
        Player player = Bukkit.getPlayer(name);
        boolean spawnWithItems = args.length != 2 || !args[1].equalsIgnoreCase("clear");

        if (player != null) {
            uuidCallback(player.getUniqueId(), player.getName(), spawnWithItems, sender);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "That player is not online!");
        return true;
    }

    private void uuidCallback(UUID uuid, String name, boolean spawnWithItems, CommandSender caller){
        if (!Bukkit.isPrimaryThread()){
            // Run in main bukkit thread
            Bukkit.getScheduler().runTask(UhcCore.getPlugin(), () -> uuidCallback(uuid, name, spawnWithItems, caller));
            return;
        }

        if (uuid == null){
            caller.sendMessage(ChatColor.RED + "Player not found!");
        }

        PlayerManager pm = gameManager.getPlayerManager();

        UhcPlayer uhcPlayer = pm.revivePlayer(uuid, name, spawnWithItems);

        if (uhcPlayer.isOnline()){
            caller.sendMessage(ChatColor.GREEN + name + " has been revived!");
        }else{
            caller.sendMessage(ChatColor.GREEN + name + " can now join the game!");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("uhc-core.commands.revive"))
            return completions;

        for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getPlayersList()) {
            if (uhcPlayer.getTeam() != null)
                completions.add(uhcPlayer.getName());
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}