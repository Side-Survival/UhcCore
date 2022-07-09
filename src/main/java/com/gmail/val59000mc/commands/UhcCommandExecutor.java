package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.threads.PreStartThread;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.color.ColorUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		if (!sender.hasPermission("uhc-core.commands.admin")){
			sender.sendMessage(ChatColor.RED + "You don't have the permission to use UHC-Core admin commands");
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
						sender.sendMessage("Changed gamestate to: " + gameState);
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

			case "clearsavedscen":
				gameManager.getScenarioManager().clearPrevious();
				sender.sendMessage("Cleared previous scenario times!");
				return true;

			case "stop":
				gameManager.endGame();
				return true;

			case "teamsize":
				if (args.length == 2) {
					try {
						int newSize = Integer.parseInt(args[1]);
						gameManager.getConfig().setTeamSize(newSize);
						sender.sendMessage("Changed team size to " + newSize + " members!");
					} catch (NumberFormatException e) {
						sender.sendMessage("Invalid number!");
					}
				} else {
					sender.sendMessage("Current team size: " + gameManager.getConfig().get(MainConfig.MAX_PLAYERS_PER_TEAM));
				}
				return true;

			case "teamamount":
				if (args.length == 2) {
					try {
						int newAmount = Integer.parseInt(args[1]);
						gameManager.getConfig().setTeamAmount(newAmount);
						sender.sendMessage("Changed total team amount to " + newAmount + " teams!");
					} catch (NumberFormatException e) {
						sender.sendMessage("Invalid number!");
					}
				} else {
					sender.sendMessage("Current total team amount: " + gameManager.getConfig().get(MainConfig.TEAM_AMOUNT));
				}
				return true;

			case "test":
				try {
					int amount = Integer.parseInt(args[1]);
					List<java.awt.Color> colors = ColorUtils.generateVisuallyDistinctColors(amount, .8f, .3f);

					for (java.awt.Color color : colors) {
						String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
						sender.sendMessage(RandomUtils.color("&" + hex + "Color (" + hex + ")"));
					}
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid number!");
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
			if (sender.hasPermission("uhc-core.commands.admin")) {
				completions.add("teamsize");
				completions.add("teamamount");
				completions.add("gamestate");
				completions.add("playerstate");
				completions.add("pvp");
				completions.add("listplayers");
				completions.add("listteams");
				completions.add("pause");
				completions.add("force");
				completions.add("location");
				completions.add("clearsavedscen");
			}
		}

		return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
	}

	public static Color hslColor(float h, float s, float l) {
		float q, p, r, g, b;

		if (s == 0) {
			r = g = b = l; // achromatic
		} else {
			q = l < 0.5 ? (l * (1 + s)) : (l + s - l * s);
			p = 2 * l - q;
			r = hue2rgb(p, q, h + 1.0f / 3);
			g = hue2rgb(p, q, h);
			b = hue2rgb(p, q, h - 1.0f / 3);
		}
		return Color.fromRGB(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
	}

	private static float hue2rgb(float p, float q, float h) {
		if (h < 0) {
			h += 1;
		}

		if (h > 1) {
			h -= 1;
		}

		if (6 * h < 1) {
			return p + ((q - p) * 6 * h);
		}

		if (2 * h < 1) {
			return q;
		}

		if (3 * h < 2) {
			return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
		}

		return p;
	}
}