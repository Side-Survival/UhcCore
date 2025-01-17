package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class EggsScenarioListener extends ScenarioListener{

    private static final EntityType[] MOBS = new EntityType[]{
            EntityType.CREEPER,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.ZOMBIE,
            EntityType.SLIME,
            EntityType.GHAST,
            EntityType.ENDERMAN,
            EntityType.CAVE_SPIDER,
            EntityType.SILVERFISH,
            EntityType.BLAZE,
            EntityType.MAGMA_CUBE,
            EntityType.BAT,
            EntityType.WITCH,
            EntityType.ENDERMITE,
            EntityType.GUARDIAN,
            EntityType.PIG,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.CHICKEN,
            EntityType.SQUID,
            EntityType.WOLF,
            EntityType.MUSHROOM_COW,
            EntityType.OCELOT,
            EntityType.IRON_GOLEM,
            EntityType.HORSE,
            EntityType.RABBIT,
    };

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        if (e.getEntityType() != EntityType.EGG){
            return;
        }

        EntityType type = getRandomEntity();
        Location loc = e.getEntity().getLocation();
        loc.getWorld().spawnEntity(loc, type);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        if (e.getEntityType() != EntityType.CHICKEN){
            return;
        }

        int i = RandomUtils.randomInteger(0, 99);
        if (i < 5){
            e.getDrops().add(new ItemStack(Material.EGG));
        }
    }

    @EventHandler
    public void onGameStarted(UhcStartedEvent e){
        ItemStack eggs = new ItemStack(Material.EGG, 4);

        for (UhcPlayer uhcPlayer : e.getPlayerManager().getOnlinePlayingPlayers()){
            try {
                Player player = uhcPlayer.getPlayer();
                player.getInventory().addItem(eggs);
            }catch (UhcPlayerNotOnlineException ex){
                // No rod for offline players
            }
        }
    }

    private EntityType getRandomEntity(){
        return MOBS[RandomUtils.randomInteger(0, MOBS.length-1)];
    }
}