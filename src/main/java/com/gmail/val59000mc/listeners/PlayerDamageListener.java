package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener{

	private final GameManager gameManager;
	private final boolean friendlyFire;

	public PlayerDamageListener(GameManager gameManager){
		this.gameManager = gameManager;
		friendlyFire = gameManager.getConfig().get(MainConfig.ENABLE_FRIENDLY_FIRE);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		handlePvpAndFriendlyFire(event);
		handleLightningStrike(event);
		handleProjectiles(event);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageEvent event){
		handleAnyDamage(event);
	}
	
	///////////////////////
	// EntityDamageEvent //
	///////////////////////

	private void handleAnyDamage(EntityDamageEvent event){
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			PlayerManager pm = gameManager.getPlayerManager();
			UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

			PlayerState uhcPlayerState = uhcPlayer.getState();
			if(uhcPlayerState.equals(PlayerState.WAITING) || uhcPlayerState.equals(PlayerState.DEAD)){
				event.setCancelled(true);
				return;
			}

			if (uhcPlayer.isFrozen()){
				event.setCancelled(true);
				return;
			}

			if (gameManager.getGameIsEnding()) {
				event.setCancelled(true);
				return;
			}

			if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
				event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
			{
				event.setDamage(event.getDamage() / 2);
			}
		}
	}
	
	///////////////////////////////
	// EntityDamageByEntityEvent //
	///////////////////////////////
	
	private void handlePvpAndFriendlyFire(EntityDamageByEntityEvent event){

		PlayerManager pm = gameManager.getPlayerManager();
		
		
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			if(!gameManager.getPvp()){
				event.setCancelled(true);
				return;
			}
			
			Player damager = (Player) event.getDamager();
			Player damaged = (Player) event.getEntity();
			UhcPlayer uhcDamager = pm.getUhcPlayer(damager);
			UhcPlayer uhcDamaged = pm.getUhcPlayer(damaged);

			if(!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING) && uhcDamager.isInTeamWith(uhcDamaged)){
				event.setCancelled(true);
			} else {
				gameManager.setLastFight(event.getDamager().getUniqueId());
			}
		}
	}
	
	private void handleLightningStrike(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof LightningStrike && event.getEntity() instanceof Player){
			event.setCancelled(true);
		}
	}
	
	private void handleProjectiles(EntityDamageByEntityEvent event) {
		PlayerManager pm = gameManager.getPlayerManager();
		
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Projectile){
			Projectile projectile = (Projectile) event.getDamager();
			final Player shot = (Player) event.getEntity();
			if(projectile.getShooter() instanceof Player shooterPlayer){
				
				if(!gameManager.getPvp()){
					event.setCancelled(true);
					return;
				}

				UhcPlayer uhcDamager = pm.getUhcPlayer(shooterPlayer);
				UhcPlayer uhcDamaged = pm.getUhcPlayer(shot);

				if(!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING) && uhcDamager.isInTeamWith(uhcDamaged)){
					event.setCancelled(true);
				} else {
					gameManager.setLastFight(shooterPlayer.getUniqueId());
				}
			}
		}
	}

}