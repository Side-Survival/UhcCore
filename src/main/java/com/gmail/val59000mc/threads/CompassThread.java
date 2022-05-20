package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class CompassThread implements Runnable{

	private final GameManager gameManager;

	public CompassThread(GameManager gameManager){
		this.gameManager = gameManager;
	}

	@Override
	public void run() {
		gameManager.getPlayerManager().checkIfRemainingPlayers();
		GameState state = gameManager.getGameState();

		if (state.equals(GameState.PLAYING) || state.equals(GameState.DEATHMATCH)) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, 10);

			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getOnlinePlayingPlayers()) {
				Player player = uhcPlayer.getPlayerForce();
				if (player == null || (player.getInventory().getItemInMainHand().getType() != Material.COMPASS && player.getInventory().getItemInOffHand().getType() != Material.COMPASS))
					continue;

				UhcPlayer target = uhcPlayer.getCompassTarget();
				if (target == null || !target.isOnline() || target.isDeath())
					target = uhcPlayer.pointCompassToNextPlayer();

				if (target == null) {
					player.spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							new TextComponent(Lang.ITEMS_COMPASS_PLAYING_ERROR)
					);
					return;
				}

				try {
					Player bukkitPlayerPointing = target.getPlayer();
					player.setCompassTarget(bukkitPlayerPointing.getLocation());

					int distance = (int) player.getLocation().distance(bukkitPlayerPointing.getLocation());
					String message = Lang.ITEMS_COMPASS_PLAYING_POINTING
							.replace("%player%", target.getName())
							.replace("%distance%", String.valueOf(distance));

					player.spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							new TextComponent(message)
					);
				} catch (UhcPlayerNotOnlineException ignored) {}
			}
		}
	}
}