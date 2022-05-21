package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.UniversalSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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

			PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 10, true, false, false);

			for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getPlayersList()){
				uhcPlayer.releasePlayer();
				try {
					uhcPlayer.getPlayer().addPotionEffect(resistance);
				} catch (UhcPlayerNotOnlineException ignored) {}
			}

			// If center deathmatch move border.
			if (shrinkBorder) {
				World world = Bukkit.getWorld("uhc_arena");
				world.getWorldBorder().setSize(gameManager.getConfig().get(MainConfig.DEATHMATCH_END_SIZE), gameManager.getConfig().get(MainConfig.DEATHMATCH_TIME_TO_SHRINK));
				world.getWorldBorder().setDamageBuffer(1);
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					gameManager.setWithering(true);
					gameManager.broadcastInfoMessage(Lang.WITHERING_ENABLED);

					for (UhcPlayer uhcPlayer : gameManager.getPlayerManager().getOnlinePlayingPlayers()) {
						try {
							uhcPlayer.getPlayer().addPotionEffect(GameManager.witherEffect);
						} catch (UhcPlayerNotOnlineException e) {
							e.printStackTrace();
						}
					}
				}
			}.runTaskLater(UhcCore.getPlugin(), 20L * gameManager.getConfig().get(MainConfig.DEATHMATCH_TIME_TO_SHRINK));
		}else{
			if(RandomUtils.isAnnounceTimer(timeBeforePVP)){
				if(timeBeforePVP % 60 == 0) {
					gameManager.broadcastInfoMessage(Lang.PVP_START_IN.replace("%time%", (timeBeforePVP / 60) + "m"));
				}else{
					gameManager.broadcastInfoMessage(Lang.PVP_START_IN.replace("%time%", timeBeforePVP + "s"));
				}

				gameManager.getPlayerManager().playSoundAll(Sound.BLOCK_NOTE_BLOCK_BANJO, 1f, 1f);
			}

			if(timeBeforePVP > 0){
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);
			}
		}
	}
}