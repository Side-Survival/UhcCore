package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

public class PreStartThread implements Runnable{

	private static PreStartThread instance = null;

	private final GameManager gameManager;
	private final int timeBeforeStart;
	private int remainingTime;
	private final int minPlayers;
	private final boolean teamsAlwaysReady;
	private boolean pause, force;
	
	public PreStartThread(GameManager gameManager){
		this.gameManager = gameManager;
		MainConfig cfg = gameManager.getConfig();
		instance = this;
		this.timeBeforeStart = cfg.get(MainConfig.TIME_BEFORE_START_WHEN_READY);
		this.remainingTime = cfg.get(MainConfig.TIME_BEFORE_START_WHEN_READY);
		this.minPlayers = cfg.get(MainConfig.MIN_PLAYERS_TO_START);
		this.teamsAlwaysReady = cfg.get(MainConfig.TEAM_ALWAYS_READY);
		this.pause = false;
		this.force = false;
	}
	
	public static String togglePause(){
		instance.pause = !instance.pause;
		return "pause:"+instance.pause+"  "+"force:"+instance.force;
	}
	
	public static String toggleForce(){
		instance.force = !instance.force;
		return "pause:"+instance.pause+"  "+"force:"+instance.force;
	}

	public static void startUntil(long startTime) {
		instance.force = true;
		int toStart = (int) (startTime - Instant.now().getEpochSecond());
		if (toStart > 0)
			instance.remainingTime = toStart;
	}

	public static int getRemainingTime() {
		if (instance == null)
			return -1;

		return instance.remainingTime;
	}
	
	@Override
	public void run() {
		List<UhcTeam> teams = gameManager.getPlayerManager().listUhcTeams();
		double readyTeams = 0;
		double teamsNumber = teams.size();

		for(UhcTeam team : teams){
			if((teamsAlwaysReady || team.isReadyToStart()) && team.isOnline()) {
				readyTeams += 1;
			}
		}

		double percentageReadyTeams = 100*readyTeams/teamsNumber;
		int playersNumber = Bukkit.getOnlinePlayers().size();

		if(
				force ||
				(!pause && (remainingTime < 5 || (playersNumber >= minPlayers)))
		){
			if (remainingTime == timeBeforeStart+1) {
				if (gameManager.getConfig().get(MainConfig.PRACTICE_MODE) && minPlayers < 999)
					gameManager.broadcastInfoMessage(Lang.GAME_ENOUGH_TEAMS_READY);
				broadcastStart();
			}else if (RandomUtils.isAnnounceStartTimer(remainingTime)) {
				broadcastStart();
			}

			remainingTime--;

			if(remainingTime == -1) {
				GameManager.getGameManager().startGame();
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					onlinePlayer.setLevel(0);
				}
			}
			else {
				if (remainingTime < 60) {
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.setLevel(remainingTime + 1);
					}
				}
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, 20);
			}


		}else{
			if(!pause && remainingTime < timeBeforeStart+1){
				gameManager.broadcastInfoMessage(Lang.GAME_STARTING_CANCELLED);
			}
			remainingTime = timeBeforeStart+1;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);
		}
	}

	public void broadcastStart() {
		String time;
		if (remainingTime % 60 == 0)
			time = (remainingTime / 60) + "m";
		else
			time = remainingTime + "s";
		gameManager.broadcastInfoMessage(Lang.GAME_STARTING_IN.replace("%time%", time));
		gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
	}
}