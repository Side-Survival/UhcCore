package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class WorldBorderThread implements Runnable{

	private long timeBeforeShrink;
	private final long timeToShrink;
	private final int endSize;
	
	public WorldBorderThread(long timeBeforeShrink, int endSize, long timeToShrink){
		this.timeBeforeShrink = timeBeforeShrink;
		this.endSize = endSize;
		this.timeToShrink = timeToShrink;
	}
	
	@Override
	public void run() {
		if(timeBeforeShrink <= 0){
			startMoving();
		} else {
			if (timeBeforeShrink < 300 && RandomUtils.isAnnounceTimer((int) timeBeforeShrink)) {
				if(timeBeforeShrink % 60 == 0) {
					GameManager.getGameManager().broadcastInfoMessage(Lang.GAME_BORDER_IN.replace("%time%", (timeBeforeShrink / 60) + "m"));
				}else{
					GameManager.getGameManager().broadcastInfoMessage(Lang.GAME_BORDER_IN.replace("%time%", timeBeforeShrink + "s"));
				}

				GameManager.getGameManager().getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
			}

			timeBeforeShrink--;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, 20);
		}
	}
	
	private void startMoving(){
		GameManager.getGameManager().broadcastInfoMessage(Lang.GAME_BORDER_START_SHRINKING);
		
		World overworld = GameManager.getGameManager().getMapLoader().getUhcWorld(World.Environment.NORMAL);
		WorldBorder overworldBorder = overworld.getWorldBorder();
		overworldBorder.setSize(2*endSize, timeToShrink);
		
		World nether = GameManager.getGameManager().getMapLoader().getUhcWorld(World.Environment.NETHER);
		if (nether != null) {
			WorldBorder netherBorder = nether.getWorldBorder();
			netherBorder.setSize(endSize, timeToShrink);
		}
	}

}