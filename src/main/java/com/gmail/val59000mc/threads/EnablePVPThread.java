package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.UniversalSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class EnablePVPThread implements Runnable{

	private final GameManager gameManager;
	private int timeBeforePvp;
	
	public EnablePVPThread(GameManager gameManager){
		this.gameManager = gameManager;
		timeBeforePvp = gameManager.getConfig().get(MainConfig.TIME_BEFORE_PVP);
	}
	
	@Override
	public void run() {
		if(!gameManager.getGameState().equals(GameState.PLAYING)) {
			return; // Stop thread
		}

		if(timeBeforePvp == 0){
			gameManager.setPvp(true);
			gameManager.broadcastInfoMessage(Lang.PVP_ENABLED);
			gameManager.getPlayerManager().playSoundAll(Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
			return; // Stop thread
		}

		if (timeBeforePvp != 300 && RandomUtils.isAnnounceTimer(timeBeforePvp)) {
			if(timeBeforePvp%60 == 0) {
				gameManager.broadcastInfoMessage(Lang.PVP_START_IN + " " + (timeBeforePvp / 60) + "m");
			}else{
				gameManager.broadcastInfoMessage(Lang.PVP_START_IN + " " + timeBeforePvp + "s");
			}

			gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
		}

		if(timeBeforePvp >= 20){
			timeBeforePvp -= 10;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,200);
		}else{
			timeBeforePvp --;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);
		}

	}

}