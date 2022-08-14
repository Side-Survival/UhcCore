package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.handlers.FreezeHandler;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class UnfreezeCommandExecutor implements CommandExecutor, TabCompleter {

	private final GameManager gameManager;

	public UnfreezeCommandExecutor(GameManager gameManager){
		this.gameManager = gameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command!");
			return true;
		}

		Player player = (Player) sender;
		if (!player.hasPermission("uhc-core.commands.freeze-admin"))
			return true;

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			player.sendMessage("Unknown player!");
			return true;
		}

		UhcPlayer uhcTarget = gameManager.getPlayerManager().getUhcPlayer(target);

		if (!uhcTarget.getState().equals(PlayerState.PLAYING)){
			player.sendMessage("That target is not playing!");
			return true;
		}

		FreezeHandler.get().unfreeze(target);

		player.sendMessage("You have unfrozen " + uhcTarget.getName());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			if (!sender.hasPermission("uhc-core.commands.freeze-admin"))
				return completions;

			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getOnlinePlayingPlayers()) {
				completions.add(uhcPlayer.getName());
			}
		}

		return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
	}
}