package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcTimeEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.CustomEventHandler;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;

public class ElapsedTimeThread implements Runnable{

	private final GameManager gameManager;
	private final CustomEventHandler customEventHandler;
	private final ElapsedTimeThread task;
	
	public ElapsedTimeThread(GameManager gameManager, CustomEventHandler customEventHandler) {
		this.gameManager = gameManager;
		this.customEventHandler = customEventHandler;
		this.task = this;
	}
	
	@Override
	public void run() {
		long time = gameManager.getElapsedTime() + 1;
		gameManager.setElapsedTime(time);

		Set<UhcPlayer> playingPlayers = gameManager.getPlayerManager().getOnlinePlayingPlayers();

		// Call time event
		UhcTimeEvent event = new UhcTimeEvent(playingPlayers, time);
		Bukkit.getScheduler().runTask(UhcCore.getPlugin(), () -> Bukkit.getServer().getPluginManager().callEvent(event));

		customEventHandler.handleTimeEvent(playingPlayers, time);

		if (!gameManager.getGameState().equals(GameState.ENDED)){
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
		}

		// Teleporting spectators to their teammates
		try {
			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getPlayersList()) {
				if (uhcPlayer.isDeath()) {
					Player player = uhcPlayer.getPlayer();
					if (player.hasPermission("uhc-core.spectator-bypass"))
						continue;

					Location pLoc = player.getLocation();
					Location tpTo;

					Optional<UhcPlayer> teammate = uhcPlayer.getTeam().getOnlinePlayingMembers().stream().findFirst();
					if (teammate.isPresent()) {
						tpTo = teammate.get().getPlayer().getLocation();
					} else {
						tpTo = RandomUtils.getSafePoint(new Location(pLoc.getWorld(), 0, 70, 0));
					}

					if (tpTo.getWorld() != pLoc.getWorld() || tpTo.distanceSquared(pLoc) > 6000) {
						player.teleport(tpTo);
						player.setAllowFlight(true);
						player.setFlying(true);
					}
				}
			}
		} catch (UhcPlayerNotOnlineException e) {
			e.printStackTrace();
		}
	}
}