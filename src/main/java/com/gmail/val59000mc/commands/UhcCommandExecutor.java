package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.threads.PreStartThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class UhcCommandExecutor implements CommandExecutor, TabCompleter {

	private final GameManager gameManager;

	public UhcCommandExecutor(GameManager gameManager){
		this.gameManager = gameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

		if (args.length == 1 && args[0].equalsIgnoreCase("version")){
			sender.sendMessage(ChatColor.GREEN + "UhcCore version: " + UhcCore.getPlugin().getDescription().getVersion());
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
			if (!sender.hasPermission("uhc-core.commands.reload")){
				sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command");
				return true;
			}

			gameManager.getScoreboardManager().getScoreboardLayout().loadFile();
			Bukkit.getServer().resetRecipes();
			gameManager.loadConfig();
			sender.sendMessage(ChatColor.GREEN + "config.yml, lang.yml and scoreboard.yml have been reloaded");
			return true;
		}

		// debug commands
		if (!sender.hasPermission("uhc-core.commands.debug")){
			sender.sendMessage(ChatColor.RED + "You don't have the permission to use UHC-Core debug commands");
			return true;
		}

		if (args.length == 0){
			sender.sendMessage("Invalid command");
			return true;
		}

		PlayerManager pm = gameManager.getPlayerManager();

		switch(args[0]){
			case "gamestate":
				if(args.length == 2){
					try{
						GameState gameState = GameState.valueOf(args[1].toUpperCase());
						gameManager.setGameState(gameState);
						sender.sendMessage("Changed gamestate to: " + gameState.toString());
						return true;
					}catch(IllegalArgumentException e){
						sender.sendMessage(args[1]+" is not a valid game state");
						return true;
					}
				}else {
					sender.sendMessage("Current gamestate: " + gameManager.getGameState());
					return true;
				}
			case "playerstate":
				if(args.length == 3){
					try{
						Player player = Bukkit.getPlayer(args[1]);
						if(player == null){
							sender.sendMessage("Player "+args[1]+" is not online");
							return true;
						}
						PlayerState playerState = PlayerState.valueOf(args[2].toUpperCase());
						pm.getUhcPlayer(player).setState(playerState);
						sender.sendMessage("Changed " + player.getName() + "'s playerstate to " + playerState);
						return true;
					}catch(IllegalArgumentException e){
						sender.sendMessage(args[2]+" is not a valid player state");
						return true;
					}catch(Exception e){
						sender.sendMessage(e.getMessage());
						return true;
					}
				}else {
					sender.sendMessage("Invalid playerstate command");
					return true;
				}

			case "pvp":
				if(args.length == 2){
					boolean state = Boolean.parseBoolean(args[1]);
					gameManager.setPvp(state);
					sender.sendMessage("Changed PvP to " + state);
				}else {
					sender.sendMessage("Invalid pvp command");
				}
				return true;

			case "listplayers":
				listUhcPlayers(sender);
				return true;

			case "listteams":
				listUhcTeams(sender);
				return true;

			case "pause":
				String pauseState = PreStartThread.togglePause();
				sender.sendMessage("The starting thread state is now : "+pauseState);
				return true;

			case "force":
				String forceState = PreStartThread.toggleForce();
				sender.sendMessage("The starting thread state is now : "+forceState);
				return true;

			case "location":
				if (sender instanceof Player){
					sender.sendMessage(((Player) sender).getLocation().toString());
				}else{
					sender.sendMessage("Only players can use this sub-command.");
				}
				return true;

			case "skiptime":
				if (args.length == 2) {
					int toSkip = Integer.parseInt(args[1]);
					gameManager.setElapsedTime(gameManager.getElapsedTime() + toSkip);
					sender.sendMessage("Skipped time by " + toSkip);
				}else {
					sender.sendMessage("Invalid skiptime command");
				}
				return true;
		}

		sender.sendMessage("Unknown sub command " + args[0]);
		return true;
	}


	private void listUhcPlayers(CommandSender sender) {
		StringBuilder str = new StringBuilder();
		str.append("Current UhcPlayers : ");
		for(UhcPlayer player : gameManager.getPlayerManager().getPlayersList()){
			str.append(player.getName());
			str.append(" ");
		}
		sender.sendMessage(str.toString());
	}

	private void listUhcTeams(CommandSender sender) {
		StringBuilder str;
		Bukkit.getLogger().info("Current UhcTeams : ");

		for(UhcTeam team : gameManager.getPlayerManager().listUhcTeams()){
			str = new StringBuilder();
			str.append("Team ").append(team.getPrefix()).append(" : ");
			for(UhcPlayer player : team.getMembers()){
				str.append(player.getName()).append(" ");
			}
			sender.sendMessage(str.toString());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			if (sender.hasPermission("uhc-core.commands.reload"))
				completions.add("reload");
			if (sender.hasPermission("uhc-core.commands.debug")) {
				completions.add("gamestate");
				completions.add("playerstate");
				completions.add("pvp");
				completions.add("listplayers");
				completions.add("listteams");
				completions.add("pause");
				completions.add("force");
				completions.add("location");
				completions.add("skiptime");
			}
		}

		return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
	}
}