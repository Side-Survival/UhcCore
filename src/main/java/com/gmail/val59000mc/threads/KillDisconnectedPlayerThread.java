package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.PlayerDeathHandler;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class KillDisconnectedPlayerThread implements Runnable{

	private final PlayerDeathHandler playerDeathHandler;
	private final UUID uuid;
	private int timeLeft;
	
	public KillDisconnectedPlayerThread(PlayerDeathHandler playerDeathHandler, UUID playerUuid, int maxDisconnectPlayersTime){
		this.playerDeathHandler = playerDeathHandler;
		uuid = playerUuid;
		timeLeft = maxDisconnectPlayersTime;
	}

	@Override
	public void run() {
		GameManager gm = GameManager.getGameManager();

		if (timeLeft <= 0 || gm.getGameState() == GameState.DEATHMATCH || gm.getGameState() == GameState.ENDED) {
			UhcPlayer uhcPlayer;
			PlayerManager pm = gm.getPlayerManager();
			try {
				uhcPlayer = pm.getUhcPlayer(uuid);
			} catch (UhcPlayerDoesNotExistException e){
				e.printStackTrace();
				return;
			}

			playerDeathHandler.handleOfflinePlayerDeath(uhcPlayer, null, null);
		} else {
			timeLeft-=5;
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), this, 100);
		}
	}

}