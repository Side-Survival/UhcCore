package com.gmail.val59000mc.customitems;

import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UhcItems{

	public static void giveGameItemTo(Player player, GameItem gameItem){
		if (!gameItem.meetsUsageRequirements()){
			return;
		}

		if (gameItem == GameItem.BUNGEE_ITEM){
			player.getInventory().setItem(8, gameItem.getItem());
		}else {
			player.getInventory().addItem(gameItem.getItem());
		}
	}

	public static void giveLobbyItemsTo(Player player){
		for (GameItem lobbyItem : GameItem.LOBBY_ITEMS){
			giveGameItemTo(player, lobbyItem);
		}
	}

	public static boolean isRegenHeadItem(ItemStack item) {
		return (
				item != null 
				&& item.getType() == UniversalMaterial.PLAYER_HEAD.getType()
				&& item.hasItemMeta()
				&& item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().contains(Lang.ITEMS_REGEN_HEAD)
		);
	}

	public static ItemStack createRegenHead(UhcPlayer player) {
		String name = player.getName();
		ItemStack item = VersionUtils.getVersionUtils().createPlayerSkull(name, player.getUuid());
		ItemMeta im = item.getItemMeta();

		// Setting up lore with team members
		im.setLore(Collections.singletonList(Lang.ITEMS_REGEN_HEAD));
		im.setDisplayName(name);
		item.setItemMeta(im);

		return item;
	}

	public static void spawnExtraXp(Location location, int quantity) {
		ExperienceOrb orb = (ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
		orb.setExperience(quantity);	
	}

	public static ItemStack createGoldenHeadPlayerSkull(String name, UUID uuid){
		ItemStack itemStack = VersionUtils.getVersionUtils().createPlayerSkull(name, uuid);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(Lang.ITEMS_GOLDEN_HEAD_SKULL_NAME.replace("%player%", name));

		List<String> lore = new ArrayList<>();
		lore.add(Lang.ITEMS_GOLDEN_HEAD_SKULL_HELP);
		itemMeta.setLore(lore);

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack createGoldenHead(){
		ItemStack itemStack = new ItemStack(Material.GOLDEN_APPLE);
		ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(Lang.ITEMS_GOLDEN_HEAD_APPLE_NAME);
		itemMeta.setLore(Collections.singletonList(Lang.ITEMS_GOLDEN_HEAD_APPLE_HELP));

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

}