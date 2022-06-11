package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.PointType;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.scenarios.scenariolisteners.TeamInventoryListener;
import com.gmail.val59000mc.threads.TimeBeforeSendBungeeThread;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.UniversalSound;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lidded;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerDeathHandler {

    private final GameManager gameManager;
    private final ScenarioManager scenarioManager;
    private final PlayerManager playerManager;
    private final MainConfig config;
    private final CustomEventHandler customEventHandler;

    public PlayerDeathHandler(GameManager gameManager, ScenarioManager scenarioManager, PlayerManager playerManager, MainConfig config, CustomEventHandler customEventHandler) {
        this.gameManager = gameManager;
        this.scenarioManager = scenarioManager;
        this.playerManager = playerManager;
        this.config = config;
        this.customEventHandler = customEventHandler;
    }

    public void handlePlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);

        Set<ItemStack> modifiedDrops = handlePlayerDeath(uhcPlayer, player.getLocation(), new HashSet<>(event.getDrops()), player.getKiller());

        // Modify event drops
        event.getDrops().clear();
        event.getDrops().addAll(modifiedDrops);

        // handle player leaving the server
        boolean canContinueToSpectate = player.hasPermission("uhc-core.spectate.override")
                || config.get(MainConfig.CAN_SPECTATE_AFTER_DEATH);

        if (!canContinueToSpectate) {
            if (config.get(MainConfig.ENABLE_BUNGEE_SUPPORT)) {
                Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new TimeBeforeSendBungeeThread(playerManager, uhcPlayer, config.get(MainConfig.TIME_BEFORE_SEND_BUNGEE_AFTER_DEATH)));
            } else {
                player.kickPlayer(Lang.DISPLAY_MESSAGE_PREFIX + " " + Lang.KICK_DEAD);
            }
        }
    }

    public void handleOfflinePlayerDeath(UhcPlayer uhcPlayer, @Nullable Location location, @Nullable Player killer) {
        Set<ItemStack> modifiedDrops = handlePlayerDeath(uhcPlayer, location, new HashSet<>(uhcPlayer.getStoredItems()), killer);
        List<ItemStack> resultDrops = new ArrayList<>(modifiedDrops);
        resultDrops = uhcPlayer.getStoredArmorOffhand(resultDrops);

        // Drop player items
        if (location != null) {
            resultDrops.forEach(item -> location.getWorld().dropItem(location, item));
        }
    }

    private Set<ItemStack> handlePlayerDeath(UhcPlayer uhcPlayer, @Nullable Location location, Set<ItemStack> playerDrops, @Nullable Player killer) {
        if (uhcPlayer.getState() != PlayerState.PLAYING){
            Bukkit.getLogger().warning("[UhcCore] " + uhcPlayer.getName() + " died while already in 'DEAD' mode!");
            return playerDrops;
        }

        playerManager.setLastDeathTime();

        // kill event
        if (killer != null){
            UhcPlayer uhcKiller = playerManager.getUhcPlayer(killer);

            uhcKiller.addKill();

            // Call Bukkit event
            UhcPlayerKillEvent killEvent = new UhcPlayerKillEvent(uhcKiller, uhcPlayer);
            Bukkit.getServer().getPluginManager().callEvent(killEvent);

            customEventHandler.handleKillEvent(killer, uhcKiller);
            boolean alive = false;
            for (UhcPlayer member : uhcPlayer.getTeam().getMembers()) {
                if (member != uhcPlayer && !member.isDeath()) {
                    alive = true;
                    break;
                }
            }
            gameManager.getPointHandler().addGamePoints(uhcKiller.getTeam(), alive ? PointType.KILL : PointType.TEAM_KILL);
        }

        // Drop the team inventory if the last player on a team was killed
        if (scenarioManager.isEnabled(Scenario.TEAM_INVENTORY))
        {
            UhcTeam team = uhcPlayer.getTeam();
            if (team.getPlayingMemberCount() == 1)
            {
                ((TeamInventoryListener) scenarioManager.getScenarioListener(Scenario.TEAM_INVENTORY)).dropTeamInventory(team, location);
            }
        }

        uhcPlayer.setDeathLocation(location);

        // Store drops in case player gets re-spawned.
        uhcPlayer.getStoredItems().clear();
        List<ItemStack> storedDrops = new ArrayList<>(playerDrops);
        try {
            storedDrops = uhcPlayer.checkArmorOffhand(uhcPlayer.getPlayer(), storedDrops);
        } catch (UhcPlayerNotOnlineException ignored) {}
        uhcPlayer.getStoredItems().addAll(storedDrops);

        EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.CUSTOM;
        String damager = null;
        boolean damagerPlayer = false;

        if (uhcPlayer.isOnline()) {
            EntityDamageEvent event = uhcPlayer.getPlayerForce().getLastDamageCause();
            if (event != null) {
                cause = event.getCause();
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    Entity damagerEntity = ((EntityDamageByEntityEvent) event).getDamager();
                    if (damagerEntity instanceof Player) {
                        damagerPlayer = true;

                        try {
                            UhcPlayer damagerUPlayer = playerManager.getUhcPlayer(damagerEntity.getUniqueId());
                            if (damagerUPlayer.getTeam() != null)
                                damager = damagerUPlayer.getTeam().getTeamColor() + damagerEntity.getName();
                            else
                                damager = damagerEntity.getName();
                        } catch (UhcPlayerDoesNotExistException e) {
                            damager = damagerEntity.getName();
                        }
                    } else if (damagerEntity instanceof Projectile) {
                        if (damagerEntity.hasMetadata("shoot-origin")) {
                            damagerPlayer = true;
                            damager = damagerEntity.getMetadata("shoot-origin").get(0).asString();

                            try {
                                UhcPlayer damagerUPlayer = playerManager.getUhcPlayer(damager);
                                if (damagerUPlayer.getTeam() != null)
                                    damager = damagerUPlayer.getTeam().getTeamColor() + damagerUPlayer.getName();
                            } catch (UhcPlayerDoesNotExistException ignored) {}
                        }
                    } else {
                        damager = damagerEntity.getName().toLowerCase().replace("_", " ");
                    }
                }
            }
        }

        String playerName = uhcPlayer.getTeam() != null ? uhcPlayer.getTeam().getTeamColor() + uhcPlayer.getName() : uhcPlayer.getName();
        String deathMessage = getDeathMessage(cause, playerName, damager, damagerPlayer);
        gameManager.broadcastRedMessage(deathMessage);

        if(config.get(MainConfig.REGEN_HEAD_DROP_ON_PLAYER_DEATH)){
            playerDrops.add(UhcItems.createRegenHead(uhcPlayer));
        }

        if(location != null && config.get(MainConfig.ENABLE_GOLDEN_HEADS)){
            playerDrops.add(UhcItems.createGoldenHeadPlayerSkull(uhcPlayer.getName(), uhcPlayer.getUuid()));
        }

        if(location != null && config.get(MainConfig.ENABLE_EXP_DROP_ON_DEATH)){
            UhcItems.spawnExtraXp(location, config.get(MainConfig.EXP_DROP_ON_DEATH));
        }

        playerManager.setPlayerSpectating(uhcPlayer);

        boolean alive = false;
        for (UhcPlayer member : uhcPlayer.getTeam().getMembers()) {
            if (member != uhcPlayer && !member.isDeath()) {
                alive = true;
                break;
            }
        }
        if (!alive) {
            gameManager.getPointHandler().addGamePoints(uhcPlayer.getTeam(), PointType.PLACEMENT);
        }

        if (config.get(MainConfig.STRIKE_LIGHTNING_ON_DEATH)) {
            playerManager.strikeLightning(uhcPlayer);
        }
        playerManager.playSoundToAll(UniversalSound.WITHER_SPAWN);

        playerManager.checkIfRemainingPlayers();

        return playerDrops;
    }

    public String getDeathMessage(EntityDamageEvent.DamageCause cause, String player, String attacker, boolean attackerPlayer) {
        switch (cause) {
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                return Lang.DEATH_EXPLOSION.replace("%player%", player);
            case CUSTOM:
                return Lang.DEATH_CUSTOM.replace("%player%", player);
            case CONTACT:
            case THORNS:
                return Lang.DEATH_CONTACT.replace("%player%", player);
            case DROWNING:
                return Lang.DEATH_DROWNING.replace("%player%", player);
            case FALL:
                return Lang.DEATH_FALL.replace("%player%", player);
            case SUFFOCATION:
            case FALLING_BLOCK:
                return Lang.DEATH_FALLING_BLOCK.replace("%player%", player);
            case FIRE:
            case HOT_FLOOR:
            case FIRE_TICK:
                return Lang.DEATH_FIRE.replace("%player%", player);
            case LAVA:
                return Lang.DEATH_LAVA.replace("%player%", player);
            case FLY_INTO_WALL:
                return Lang.DEATH_FLY_INTO_WALL.replace("%player%", player);
            case MAGIC:
                return Lang.DEATH_MAGIC.replace("%player%", player);
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                if (attackerPlayer)
                    return Lang.DEATH_PLAYER.replace("%player%", player).replace("%killer%", attacker);
                else
                    return Lang.DEATH_MOB.replace("%player%", player).replace("%mob%", attacker);
            case PROJECTILE:
                if (attackerPlayer && attacker != null)
                    return Lang.DEATH_PLAYER_PROJECTILE.replace("%player%", player).replace("%killer%", attacker);

                return Lang.DEATH_PROJECTILE.replace("%player%", player);
            case SUICIDE:
                return Lang.DEATH_SUICIDE.replace("%player%", player);
            case STARVATION:
                return Lang.DEATH_STARVATION.replace("%player%", player);
            default:
                return Lang.DEATH_OTHER_CAUSE.replace("%player%", player).replace("%cause%", cause.toString().toLowerCase().replace("_", " "));
        }
    }
}
