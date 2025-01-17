package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.Optional;

public class EntityDamageListener implements Listener{

    private final GameManager gameManager;

    public EntityDamageListener(GameManager gameManager){
        this.gameManager = gameManager;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player damager) {
            if (gameManager.getPlayerManager().getUhcPlayer(damager).isDeath()) {
                e.setCancelled(true);
                return;
            }
         }

        handleOfflinePlayers(e);
    }

    private void handleOfflinePlayers(EntityDamageByEntityEvent e){
        if (e.getEntityType() != EntityType.ZOMBIE || (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Projectile))){
            return;
        }

        MainConfig cfg = gameManager.getConfig();
        PlayerManager pm = gameManager.getPlayerManager();
        
        // Offline players are disabled
        if (!cfg.get(MainConfig.SPAWN_OFFLINE_PLAYERS)){
            return;
        }

        Zombie zombie = (Zombie) e.getEntity();
        
        // Find zombie owner
        Optional<UhcPlayer> owner = pm.getPlayersList()
                .stream()
                .filter(uhcPlayer -> uhcPlayer.getOfflineZombieUuid() != null && uhcPlayer.getOfflineZombieUuid().equals(zombie.getUniqueId()))
                .findFirst();
        
        // Not a offline player
        if (!owner.isPresent()){
            return;
        }

        if (!gameManager.getPvp()) {
            e.setCancelled(true);
            return;
        }

        if (e.getDamager() instanceof Player) {
            UhcPlayer damager = pm.getUhcPlayer((Player) e.getDamager());
            boolean isTeamMember = owner.get().isInTeamWith(damager);
            boolean friendlyFire = cfg.get(MainConfig.ENABLE_FRIENDLY_FIRE);

            // If PvP is false or is team member & friendly fire is off
            if (isTeamMember && !friendlyFire){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onZombieTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null && event.getTarget().getType() == EntityType.PLAYER) {
            if (((Player) event.getTarget()).getGameMode() == GameMode.ADVENTURE) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntityType() != EntityType.ZOMBIE)
                return;

            Optional<UhcPlayer> owner = gameManager.getPlayerManager().getPlayersList()
                    .stream()
                    .filter(uhcPlayer -> uhcPlayer.getOfflineZombieUuid() != null && uhcPlayer.getOfflineZombieUuid().equals(event.getEntity().getUniqueId()))
                    .findFirst();

            if (owner.isPresent())
                event.setCancelled(true);
        }
    }
}