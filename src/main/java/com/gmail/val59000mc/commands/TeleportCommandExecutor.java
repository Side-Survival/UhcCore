package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeleportCommandExecutor implements CommandExecutor, TabCompleter {

	private final GameManager gameManager;

	public TeleportCommandExecutor(GameManager gameManager){
		this.gameManager = gameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (!(sender instanceof Player)){
			sender.sendMessage("Only players can teleport!");
			return true;
		}

		Player player = (Player) sender;

		UhcPlayer uhcPlayer = gameManager.getPlayerManager().getUhcPlayer(player);
		if(
				!player.hasPermission("uhc-core.commands.teleport-admin") &&
				!(uhcPlayer.getState().equals(PlayerState.DEAD) && gameManager.getConfig().get(MainConfig.SPECTATING_TELEPORT))
		){
			uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT_ERROR);
			return true;
		}

		if (args.length == 3 && player.hasPermission("uhc-core.commands.teleport-admin")){
			// teleport to coordinates
			double x, y, z;

			try {
				x = Double.parseDouble(args[0]);
				y = Double.parseDouble(args[1]);
				z = Double.parseDouble(args[2]);
			}catch (NumberFormatException ex){
				sender.sendMessage(ChatColor.RED + "Invalid coordinates!");
				return true;
			}

			Location loc = new Location(player.getWorld(), x, y, z);
			player.teleport(loc);
			player.setAllowFlight(true);

			player.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT.replace("%player%", x + "/" + y + "/" + z));
			return true;
		}

		if (args.length == 2 && player.hasPermission("uhc-core.commands.teleport-admin")){
			// teleport player to player
			Player player1, player2;

			player1 = Bukkit.getPlayer(args[0]);
			player2 = Bukkit.getPlayer(args[1]);

			if (player1 == null || player2 == null){
				sender.sendMessage(ChatColor.RED + "That player can not be found!");
				return true;
			}

			player1.teleport(player2.getLocation());

			player.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT.replace("%player%", player1.getName()));
			return true;
		}

		if (args.length != 1){
			uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT_ERROR);
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT_ERROR);
			return true;
		}

		UhcPlayer uhcTarget = gameManager.getPlayerManager().getUhcPlayer(target);

		if (!uhcTarget.getState().equals(PlayerState.PLAYING) && !player.hasPermission("uhc-core.commands.teleport-admin")){
			uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT_ERROR);
			return true;
		}

		if ((uhcPlayer.getTeam() == null || !Objects.equals(uhcPlayer.getTeam(), uhcTarget.getTeam())) && !player.hasPermission("uhc-core.commands.teleport-admin")) {
			uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT_ERROR);
			return true;
		}

		player.teleport(target);
		player.setAllowFlight(true);

		uhcPlayer.sendMessage(Lang.COMMAND_SPECTATING_TELEPORT.replace("%player%", uhcTarget.getName()));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			if (sender.hasPermission("uhc-core.commands.teleport-admin"))
				return null;

			Player player = (Player) sender;
			UhcPlayer uhcPlayer = gameManager.getPlayerManager().getUhcPlayer(player);

			if (uhcPlayer.getTeam() != null) {
				for (UhcPlayer member : uhcPlayer.getTeam().getOnlinePlayingMembers()) {
					completions.add(member.getName());
				}
			}
		}

		return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
	}
}