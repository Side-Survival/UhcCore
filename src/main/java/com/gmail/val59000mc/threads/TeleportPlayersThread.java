package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.utils.CageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TeleportPlayersThread implements Runnable{

	private final GameManager gameManager;
	private final UhcTeam team;
	
	public TeleportPlayersThread(GameManager gameManager, UhcTeam team) {
		this.gameManager = gameManager;
		this.team = team;
	}

	@Override
	public void run() {
		Location location = team.getStartingLocation().clone().add(0, 6, 0);
		CageUtils.placeCage(location);

		boolean addedLoc = false;
		for (UhcPlayer uhcPlayer : team.getMembers()){
			Player player;
			try {
				player = uhcPlayer.getPlayer();
			}catch (UhcPlayerNotOnlineException ex){
				continue;
			}

			Bukkit.getLogger().info("[UhcCore] Teleporting "+player.getName());

			uhcPlayer.freezePlayer();

			Location finalLoc = addedLoc ? location.clone().add(1, 0, 1) : location;
			addedLoc = !addedLoc;
			// Add 2 blocks to the Y location to prevent players from spawning underground.
			player.teleport(finalLoc);

			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.setFireTicks(0);
			uhcPlayer.setHasBeenTeleportedToLocation(true);
		}
	}

}