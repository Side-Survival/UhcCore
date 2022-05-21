package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.DeathmatchHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.TimeUtils;
import com.gmail.val59000mc.utils.UniversalSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class TimeBeforeDeathmatchThread implements Runnable{

	private final GameManager gameManager;
	private final DeathmatchHandler deathmatchHandler;

	public TimeBeforeDeathmatchThread(GameManager gameManager, DeathmatchHandler deathmatchHandler) {
		this.gameManager = gameManager;
		this.deathmatchHandler = deathmatchHandler;
	}
	
	@Override
	public void run() {
		long remainingTime = gameManager.getRemainingTime();

		remainingTime--;
		gameManager.setRemainingTime(remainingTime);
		
		if(remainingTime <= 300 && RandomUtils.isAnnounceTimer((int) remainingTime)){
			if(remainingTime % 60 == 0) {
				gameManager.broadcastInfoMessage(Lang.GAME_DEATHMATCH_IN.replace("%time%", (remainingTime / 60) + "m"));
			}else{
				gameManager.broadcastInfoMessage(Lang.GAME_DEATHMATCH_IN.replace("%time%", remainingTime + "s"));
			}
			gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
		}

		if (remainingTime == 0){
			deathmatchHandler.startDeathmatch();
		}else if(remainingTime > 0 && gameManager.getGameState() == GameState.PLAYING) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, TimeUtils.SECOND_TICKS);
		}
	}
	
}
