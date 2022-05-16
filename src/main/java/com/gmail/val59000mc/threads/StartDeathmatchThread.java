package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.UniversalSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.List;

public class StartDeathmatchThread implements Runnable{

	private final GameManager gameManager;
	private int timeBeforePVP;
	private final boolean shrinkBorder;
	private final List<Location> cages;

	public StartDeathmatchThread(GameManager gameManager, boolean shrinkBorder, List<Location> cages) {
		this.gameManager = gameManager;
		this.timeBeforePVP = 11;
		this.shrinkBorder = shrinkBorder;
		this.cages = cages;
	}
	
	@Override
	public void run() {
		timeBeforePVP --;

		if (timeBeforePVP == 0) {
			gameManager.setPvp(true);
			gameManager.broadcastInfoMessage(Lang.PVP_ENABLED);
			gameManager.getPlayerManager().playSoundToAll(UniversalSound.WITHER_SPAWN);
			gameManager.getPlayerManager().setLastDeathTime();

			for (Location cage : cages) {
				gameManager.getDeathmatchHandler().removeCage(cage);
			}

			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getPlayersList()){
				uhcPlayer.releasePlayer();
			}

			// If center deathmatch move border.
			if (shrinkBorder){
				gameManager.getMapLoader().getUhcWorld(World.Environment.NORMAL).getWorldBorder().setSize(gameManager.getConfig().get(MainConfig.DEATHMATCH_END_SIZE), gameManager.getConfig().get(MainConfig.DEATHMATCH_TIME_TO_SHRINK));
				gameManager.getMapLoader().getUhcWorld(World.Environment.NORMAL).getWorldBorder().setDamageBuffer(1);
			}
		}else{

			if(timeBeforePVP <= 5 || (timeBeforePVP%5 == 0)){
				gameManager.broadcastInfoMessage(Lang.PVP_START_IN+" "+timeBeforePVP+"s");
				gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
			}

			if(timeBeforePVP > 0){
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);
			}
		}
	}
}