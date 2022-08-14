package com.gmail.val59000mc.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.*;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.gui.ScenarioVoteGUI;
import com.gmail.val59000mc.gui.SpectatorPlayersGUI;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.*;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.threads.EnableGlowingThread;
import com.gmail.val59000mc.utils.RandomUtils;
import com.lishid.openinv.IOpenInv;
import org.bukkit.*;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ItemsListener implements Listener {

	private final GameManager gameManager;
	private final MainConfig config;
	private final PlayerManager playerManager;
	private final TeamManager teamManager;
	private final ScenarioManager scenarioManager;
	private final ScoreboardHandler scoreboardHandler;
	private final Map<UUID, List<UUID>> randomPlayerItemTargets = new HashMap<>();

	public ItemsListener(
			GameManager gameManager,
			MainConfig config,
			PlayerManager playerManager,
			TeamManager teamManager,
			ScenarioManager scenarioManager,
			ScoreboardHandler scoreboardHandler) {
		this.gameManager = gameManager;
		this.config = config;
		this.playerManager = playerManager;
		this.teamManager = teamManager;
		this.scenarioManager = scenarioManager;
		this.scoreboardHandler = scoreboardHandler;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClickItem(PlayerInteractEvent event){
		if (
				event.getAction() != Action.RIGHT_CLICK_AIR &&
				event.getAction() != Action.RIGHT_CLICK_BLOCK
		){
			return;
		}

		Player player = event.getPlayer();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
		ItemStack hand = player.getItemInHand();

		if (GameItem.isGameItem(hand)){
			event.setCancelled(true);
			GameItem gameItem = GameItem.getGameItem(hand);
			handleGameItemInteract(gameItem, player, uhcPlayer, hand);
			return;
		}

		if (hand.getType() == Material.PLAYER_HEAD) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1L, 1L);
			player.setItemInHand(null);
			event.setCancelled(true);
			return;
		}

		if (player.getGameMode() == GameMode.ADVENTURE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickInInventory(InventoryClickEvent event){
		ItemStack item = event.getCurrentItem();
		Player player = (Player) event.getWhoClicked();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);

		// Stop players from moving game items in their inventory.
		// Above item == null check as item is null on hotbar swap.
		if (gameManager.getGameState() == GameState.WAITING && event.getAction() == InventoryAction.HOTBAR_SWAP){
			event.setCancelled(true);
		}

		// Only handle clicked items.
		if (item == null){
			return;
		}

		// Listen for GameItems
		if (gameManager.getGameState() == GameState.WAITING){
			if (GameItem.isGameItem(item)){
				event.setCancelled(true);
				handleGameItemInteract(GameItem.getGameItem(item), player, uhcPlayer, item);
			}
		}

		// Ban level 2 potions
		if(event.getInventory().getType().equals(InventoryType.BREWING) && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS)){
			final BrewerInventory inv = (BrewerInventory) event.getInventory();
			final HumanEntity human = event.getWhoClicked();
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckBrewingStandAfterClick(inv.getHolder(), human),1);
		}
	}

	private void handleGameItemInteract(GameItem gameItem, Player player, UhcPlayer uhcPlayer, ItemStack item){
		switch (gameItem){
			case TEAM_LIST:
				teamManager.openTeamSelection(player);
				break;

			case SCENARIO_VIEWER:
				ScenarioVoteGUI scenarioVoteGUI = new ScenarioVoteGUI();
				scenarioVoteGUI.open(player);
				break;

			case COMPASS_ITEM:
				uhcPlayer.pointCompassToNextPlayer();
				break;

			case SPECTATOR_SPAWN:
				if (uhcPlayer.isDeath()) {
					Location loc;
					if (gameManager.getGameState() == GameState.DEATHMATCH || (gameManager.getGameState() == GameState.ENDED && gameManager.isDeathmatch()))
						loc = Bukkit.getWorld("uhc_arena").getSpawnLocation();
					else
						loc = RandomUtils.getSafePoint(gameManager.getMapLoader().getUhcWorld(World.Environment.NORMAL).getBlockAt(0, 70, 0).getLocation());
					loc.add(0.5, 0, 0.5);
					player.teleport(loc);
					player.setAllowFlight(true);
					player.setFlying(true);
				}
				break;

			case SPECTATOR_PLAYERS:
				if (uhcPlayer.isDeath() && player.hasPermission("uhc-core.spectator-players")) {
					SpectatorPlayersGUI gui = new SpectatorPlayersGUI();
					gui.open(player, gameManager.getPlayerManager().getAliveOnlinePlayers().size());
				}
				break;

			case RANDOM_PLAYER:
				if (uhcPlayer.isDeath() && player.hasPermission("uhc-core.spectator-players")) {
					if (!randomPlayerItemTargets.containsKey(player.getUniqueId()))
						randomPlayerItemTargets.put(player.getUniqueId(), new ArrayList<>());

					List<Player> available = new ArrayList<>();
					for (UhcPlayer playingPlayer : gameManager.getPlayerManager().getAllPlayingPlayers()) {
						if (!randomPlayerItemTargets.get(player.getUniqueId()).contains(playingPlayer.getUuid())) {
							available.add(playingPlayer.getPlayerForce());
						}
					}

					if (!available.isEmpty()) {
						Player target = available.remove(new Random().nextInt(available.size()));
						player.teleport(target);
						player.setAllowFlight(true);
						player.setFlying(true);

						if (available.isEmpty()) {
							randomPlayerItemTargets.get(player.getUniqueId()).clear();
						} else {
							randomPlayerItemTargets.get(player.getUniqueId()).add(target.getUniqueId());
						}
					}
				}
				break;

			case OPEN_INV:
				if (uhcPlayer.isDeath() && player.hasPermission("uhc-core.spectator-players")) {
					Entity targetEntity = player.getTargetEntity(40);
					if (!(targetEntity instanceof Player target))
						return;

					if (target.getGameMode() != GameMode.SURVIVAL)
						return;

					IOpenInv openInv = (IOpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
					if (openInv != null) {
						try {
							openInv.openInventory(player, openInv.getSpecialInventory(target, true));
						} catch (InstantiationException e) {
							e.printStackTrace();
						}
					}
				}
				break;

			case TEAM_CHEST:
				if (uhcPlayer.getState() != PlayerState.PLAYING){
					player.sendMessage(Lang.SCENARIO_TEAMINVENTORY_ERROR);
					return;
				}

				player.sendMessage(Lang.SCENARIO_TEAMINVENTORY_OPEN);
				player.openInventory(uhcPlayer.getTeam().getTeamInventory());
				break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperEvent(InventoryMoveItemEvent event) {
		Inventory inv = event.getDestination();
		if(inv.getType().equals(InventoryType.BREWING) && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS) && inv.getHolder() instanceof BrewingStand){
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(), null),1);
		}
		
	}
	
	private static class CheckBrewingStandAfterClick implements Runnable{
        private final BrewingStand stand;
        private final HumanEntity human;

        private CheckBrewingStandAfterClick(BrewingStand stand, HumanEntity human) {
        	this.stand = stand;
        	this.human = human;
        }

        @Override
        public void run(){
        	ItemStack ingredient = stand.getInventory().getIngredient();
			if(ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)){
				if(human != null){
                    human.sendMessage(Lang.ITEMS_POTION_BANNED);
                }

				stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
				stand.getInventory().setIngredient(new ItemStack(Material.AIR));
			}
        }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();

		if (event.getPlayer().getGameMode() == GameMode.ADVENTURE || gameManager.getGameState() == GameState.WAITING && GameItem.isGameItem(item)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e){
		if (e.getItem() == null) return;

		if (e.getItem().getType() == Material.MILK_BUCKET) {
			if (gameManager.isGlowing() || gameManager.isWithering())
				new BukkitRunnable() {
					@Override
					public void run() {
						if (gameManager.isGlowing())
							e.getPlayer().addPotionEffect(EnableGlowingThread.effect);
						if (gameManager.isWithering())
							e.getPlayer().addPotionEffect(GameManager.witherEffect);
					}
				}.runTaskLater(UhcCore.getPlugin(), 20L);
		}

		Craft craft = CraftsManager.getCraft(e.getItem());
		if (craft != null){
			for (Craft.OnConsumeListener listener : craft.getOnConsumeListeners()) {
				if (listener.onConsume(playerManager.getUhcPlayer(e.getPlayer()))) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.ADVENTURE)
			event.setCancelled(true);
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getEntered() instanceof Player) {
			if (((Player) event.getEntered()).getGameMode() != GameMode.SURVIVAL)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getAttacker() instanceof Player) {
			if (((Player) event.getAttacker()).getGameMode() != GameMode.SURVIVAL)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			event.getProjectile().setMetadata("shoot-origin", new FixedMetadataValue(UhcCore.getPlugin(), event.getEntity().getName()));
		}
	}

	@EventHandler
	public void onExpPickup(PlayerPickupExperienceEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.ADVENTURE)
			event.setCancelled(true);
	}
}