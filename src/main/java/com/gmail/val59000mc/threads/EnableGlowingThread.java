package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EnableGlowingThread implements Runnable{

	private final GameManager gameManager;
	private int timeBeforeGlowing;
	public static PotionEffect effect;

	public EnableGlowingThread(GameManager gameManager){
		this.gameManager = gameManager;
		timeBeforeGlowing = gameManager.getConfig().get(MainConfig.TIME_BEFORE_GLOWING);

		effect = new PotionEffect(PotionEffectType.GLOWING, 999999, 0, true, false, true);
	}
	
	@Override
	public void run() {
		if (!gameManager.getGameState().equals(GameState.PLAYING)) {
			return; // Stop thread
		}

		if (timeBeforeGlowing == 0) {
			gameManager.setGlowing(true);
			gameManager.broadcastInfoMessage(Lang.GLOWING_ENABLED);
			gameManager.getPlayerManager().sendTitleAll(
					" ",
					Lang.GLOWING_ENABLED,
					5, 60, 5
			);

			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getOnlinePlayingPlayers()) {
				try {
					uhcPlayer.getPlayer().addPotionEffect(effect);
				} catch (UhcPlayerNotOnlineException e) {
					e.printStackTrace();
				}
			}

			gameManager.getPlayerManager().playSoundAll(Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f);
			return; // Stop thread
		}

		if (timeBeforeGlowing <= 300 && RandomUtils.isAnnounceTimer(timeBeforeGlowing)) {
			if (timeBeforeGlowing % 60 == 0) {
				gameManager.broadcastInfoMessage(Lang.GLOWING_START_IN.replace("%time%", (timeBeforeGlowing / 60) + "m"));
			} else {
				gameManager.broadcastInfoMessage(Lang.GLOWING_START_IN.replace("%time%", timeBeforeGlowing + "s"));
			}

			gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
		}

		if (timeBeforeGlowing >= 20) {
			timeBeforeGlowing -= 10;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,200);
		} else {
			timeBeforeGlowing--;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);
		}

	}

}