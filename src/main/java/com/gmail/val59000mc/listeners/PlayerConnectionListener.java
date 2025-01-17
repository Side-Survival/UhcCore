package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerJoinException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.FreezeHandler;
import com.gmail.val59000mc.game.handlers.PlayerDeathHandler;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.threads.KillDisconnectedPlayerThread;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener{

	private final GameManager gameManager;
	private final PlayerManager playerManager;
	private final PlayerDeathHandler playerDeathHandler;
	private final ScoreboardHandler scoreboardHandler;

	public PlayerConnectionListener(GameManager gameManager, PlayerManager playerManager, PlayerDeathHandler playerDeathHandler, ScoreboardHandler scoreboardHandler){
		this.gameManager = gameManager;
		this.playerManager = playerManager;
		this.playerDeathHandler = playerDeathHandler;
		this.scoreboardHandler = scoreboardHandler;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		// Player is not allowed to join so don't create UhcPlayer. (Server full, whitelist, ban, ...)
		if (event.getResult() != Result.ALLOWED){
			return;
		}
		
		try{
			boolean allowedToJoin = playerManager.isPlayerAllowedToJoin(event.getPlayer());

			if (allowedToJoin){
				// Create player if not existent.
				playerManager.getOrCreateUhcPlayer(event.getPlayer());
			}else{
				throw new UhcPlayerJoinException("An unexpected error as occured.");
			}
		}catch(final UhcPlayerJoinException e){
			event.setKickMessage(e.getMessage());
			event.setResult(Result.KICK_OTHER);
		}
	}

	@EventHandler
	public void onPreJoin(final AsyncPlayerPreLoginEvent event) {
		if (gameManager.getGameState() != GameState.WAITING)
			return;

		for (OfflinePlayer operator : Bukkit.getOperators()) {
			if (operator.getName() != null && operator.getName().equalsIgnoreCase(event.getName()))
				return;
		}

		int maxPlayers = gameManager.getConfig().get(MainConfig.MAX_PLAYERS_PER_TEAM) * gameManager.getConfig().get(MainConfig.TEAM_AMOUNT);

		int onlinePlayers = 0;
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (onlinePlayer.getGameMode() != GameMode.SPECTATOR)
				onlinePlayers++;
		}

		if (onlinePlayers >= maxPlayers)
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, Lang.GAME_FULL);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent event){
		String joinMessage = Lang.GAME_PLAYER_JOINED.replace("%player%", event.getPlayer().getName());
		if (gameManager.getGameState() != GameState.WAITING) {
			Player player = event.getPlayer();
			try {
				if (!playerManager.doesPlayerExist(player) || playerManager.getUhcPlayer(player.getUniqueId()).isDeath())
					joinMessage = "";
			} catch (UhcPlayerDoesNotExistException ignored) {}


			if (FreezeHandler.get().isFrozen(event.getPlayer())) {
				FreezeHandler.get().unfreeze(player);
				FreezeHandler.get().freeze(player);
			}
		}

		if (event.getPlayer().hasPermission("uhc-core.global-spectate"))
			joinMessage = "";

		event.setJoinMessage(joinMessage);
		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> playerManager.playerJoinsTheGame(event.getPlayer()), 1);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerDisconnect(PlayerQuitEvent event){
		String quitMessage = Lang.GAME_PLAYER_LEFT.replace("%player%", event.getPlayer().getName());
		if (gameManager.getGameState() != GameState.WAITING) {
			Player player = event.getPlayer();
			try {
				if (!playerManager.doesPlayerExist(player) || playerManager.getUhcPlayer(player.getUniqueId()).isDeath())
					quitMessage = "";
			} catch (UhcPlayerDoesNotExistException ignored) {}
		}

		if (event.getPlayer().hasPermission("uhc-core.global-spectate"))
			quitMessage = "";

		event.setQuitMessage(quitMessage);
		if(gameManager.getGameState().equals(GameState.WAITING) || gameManager.getGameState().equals(GameState.STARTING)){
			UhcPlayer uhcPlayer = playerManager.getUhcPlayer(event.getPlayer());

			if(gameManager.getGameState().equals(GameState.STARTING)){
				playerManager.setPlayerSpectateAtLobby(uhcPlayer);
//				gameManager.broadcastInfoMessage(uhcPlayer.getName()+" has left while the game was starting and has been killed.");
				if (gameManager.getConfig().get(MainConfig.STRIKE_LIGHTNING_ON_DEATH)) {
					playerManager.strikeLightning(uhcPlayer);
				}
			}

			if (gameManager.getConfig().get(MainConfig.PRACTICE_MODE) && uhcPlayer.getTeam() != null)
				uhcPlayer.getTeam().leave(uhcPlayer);

			// Update player tab
			playerManager.getPlayersList().remove(uhcPlayer);
		}

		if(gameManager.getGameState().equals(GameState.PLAYING) || gameManager.getGameState().equals(GameState.DEATHMATCH)){
			UhcPlayer uhcPlayer = playerManager.getUhcPlayer(event.getPlayer());
			if (uhcPlayer.isPlaying()) {
				if (gameManager.getConfig().get(MainConfig.ENABLE_KILL_DISCONNECTED_PLAYERS)) {

					KillDisconnectedPlayerThread killDisconnectedPlayerThread = new KillDisconnectedPlayerThread(
							playerDeathHandler, event.getPlayer().getUniqueId(),
							gameManager.getConfig().get(MainConfig.MAX_DISCONNECT_PLAYERS_TIME)
					);

					Bukkit.getScheduler().runTaskLaterAsynchronously(UhcCore.getPlugin(), killDisconnectedPlayerThread,1);
				}
				if (gameManager.getConfig().get(MainConfig.SPAWN_OFFLINE_PLAYERS)) {
					playerManager.spawnOfflineZombieFor(event.getPlayer());
				}
				playerManager.checkIfRemainingPlayers();
			}
		}

		scoreboardHandler.removePlayerScoreboard(event.getPlayer());
	}

}