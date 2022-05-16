package com.gmail.val59000mc.listeners;

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
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemsListener implements Listener {

	private final GameManager gameManager;
	private final MainConfig config;
	private final PlayerManager playerManager;
	private final TeamManager teamManager;
	private final ScenarioManager scenarioManager;
	private final ScoreboardHandler scoreboardHandler;

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

		GameState state = gameManager.getGameState();
		if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
				&& UhcItems.isRegenHeadItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
		) {
			event.setCancelled(true);
			uhcPlayer.getTeam().regenTeam(config.get(MainConfig.DOUBLE_REGEN_HEAD));
			player.getInventory().remove(hand);
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
				uhcPlayer.pointCompassToNextPlayer(config.get(MainConfig.PLAYING_COMPASS_MODE), config.get(MainConfig.PLAYING_COMPASS_COOLDOWN));
				break;
			case SPECTATOR_SPAWN:
				if (uhcPlayer.isDeath()) {
					Location loc = RandomUtils.getSafePoint(gameManager.getMapLoader().getUhcWorld(World.Environment.NORMAL).getBlockAt(0, 70, 0).getLocation());
					player.teleport(loc);
				}
				break;
			case SPECTATOR_PLAYERS:
				if (uhcPlayer.isDeath() && player.hasPermission("uhc-core.spectator-players")) {
					SpectatorPlayersGUI gui = new SpectatorPlayersGUI();
					gui.open(player, teamManager.getAliveUhcTeams().size());
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

		if (gameManager.getGameState() == GameState.WAITING && GameItem.isGameItem(item)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e){
		if (e.getItem() == null) return;

		Craft craft = CraftsManager.getCraft(e.getItem());
		if (craft != null){
			for (Craft.OnConsumeListener listener : craft.getOnConsumeListeners()) {
				if (listener.onConsume(playerManager.getUhcPlayer(e.getPlayer()))) {
					e.setCancelled(true);
					return;
				}
			}
		}

		if (e.getItem().isSimilar(UhcItems.createGoldenHead())){
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
		}
	}
}