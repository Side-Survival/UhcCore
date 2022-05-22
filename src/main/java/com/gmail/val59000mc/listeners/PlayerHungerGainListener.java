package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class PlayerHungerGainListener implements Listener {

    private final PlayerManager playerManager;

    public PlayerHungerGainListener(PlayerManager playerManager){
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent e){

        if (!(e.getEntity() instanceof Player)){
            return;
        }

        Player player = (Player) e.getEntity();
        UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);

        // Cancel hunger when the game is not in playing state.
        if (uhcPlayer.getState() != PlayerState.PLAYING){
            e.setCancelled(true);
        }

        if (player.getFoodLevel() > e.getFoodLevel()) {
            // Reduce food requirement by 50%
            Random random = new Random();
            if (random.nextBoolean())
                e.setCancelled(true);
        }
    }
}
