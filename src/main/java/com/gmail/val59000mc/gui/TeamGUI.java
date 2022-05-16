package com.gmail.val59000mc.gui;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.TeamManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.utils.RandomUtils;
import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.InventoryManager;
import libs.fr.minuskube.inv.SmartInventory;
import libs.fr.minuskube.inv.content.InventoryContents;
import libs.fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TeamGUI implements InventoryProvider {

    private final UhcCore plugin;
    private final InventoryManager invManager;
    public static Map<Integer, ItemStack> colorMap;
    private SmartInventory inventory;

    public TeamGUI() {
        this.plugin = UhcCore.getPlugin();
        this.invManager = plugin.getInventoryManager();

        if (colorMap == null) {
            initColorMap();
        }
    }

    public static void initColorMap() {
        colorMap = new HashMap<>();

        colorMap.put(1, RandomUtils.getColoredChestPlate(255, 238, 0));
        colorMap.put(2, RandomUtils.getColoredChestPlate(255, 255, 175));
        colorMap.put(3, RandomUtils.getColoredChestPlate(255, 136, 0));
        colorMap.put(4, RandomUtils.getColoredChestPlate(255, 0, 0));
        colorMap.put(5, RandomUtils.getColoredChestPlate(220, 20, 60));
        colorMap.put(6, RandomUtils.getColoredChestPlate(117, 33, 39));
        colorMap.put(7, RandomUtils.getColoredChestPlate(255, 0, 255));
        colorMap.put(8, RandomUtils.getColoredChestPlate(94, 52, 106));
        colorMap.put(9, RandomUtils.getColoredChestPlate(158, 71, 158));
        colorMap.put(10, RandomUtils.getColoredChestPlate(173, 255, 47));
        colorMap.put(11, RandomUtils.getColoredChestPlate(42, 255, 0));
        colorMap.put(12, RandomUtils.getColoredChestPlate(35, 110, 20));
        colorMap.put(13, RandomUtils.getColoredChestPlate(82, 114, 47));
        colorMap.put(14, RandomUtils.getColoredChestPlate(46, 139, 47));
        colorMap.put(15, RandomUtils.getColoredChestPlate(136, 158, 255));
        colorMap.put(16, RandomUtils.getColoredChestPlate(66, 91, 201));
        colorMap.put(17, RandomUtils.getColoredChestPlate(135, 206, 235));
        colorMap.put(18, RandomUtils.getColoredChestPlate(64, 224, 208));
        colorMap.put(19, RandomUtils.getColoredChestPlate(127, 255, 212));
        colorMap.put(20, RandomUtils.getColoredChestPlate(111, 68, 30));
        colorMap.put(21, RandomUtils.getColoredChestPlate(244, 164, 96));
        colorMap.put(22, RandomUtils.getColoredChestPlate(94, 94, 94));
        colorMap.put(23, RandomUtils.getColoredChestPlate(172, 172, 172));
        colorMap.put(24, RandomUtils.getColoredChestPlate(217, 203, 183));
    }

    public void load() {
        int size = 24 / 9 + 1;
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new TeamGUI())
                .size(Math.min(size, 6), 9)
                .title(Lang.TEAM_GUI_TITLE)
                .build();
    }

    public void open(Player player) {
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        TeamManager teamManager = GameManager.getGameManager().getTeamManager();

        for (int i = 1; i < 25; i++) {
            UhcTeam team = teamManager.getTeamById(i);
            if (team == null)
                continue;

            ItemStack item = colorMap.get(i).clone();
            itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(Lang.TEAM_FULL_NAME.replace("%color%", team.getFullPrefix()));
            itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>(Lang.TEAM_GUI_LORE);

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                lore.add(RandomUtils.color(" &f" + uhcPlayer.getName()));
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            contents.add(ClickableItem.of(item, e -> {
                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
                teamManager.joinTeam(uhcPlayer, team);

                player.closeInventory();
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        TeamManager teamManager = GameManager.getGameManager().getTeamManager();

        for (int i = 1; i < 25; i++) {
            UhcTeam team = teamManager.getTeamById(i);
            if (team == null)
                continue;

            ItemStack item = colorMap.get(i).clone();
            itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(Lang.TEAM_FULL_NAME.replace("%color%", team.getFullPrefix()));
            itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>(Lang.TEAM_GUI_LORE);

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                lore.add(RandomUtils.color(" &f" + uhcPlayer.getName()));
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            int slot = i - 1;
            contents.set(slot / 9, slot % 9, ClickableItem.of(item, e -> {
                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
                teamManager.joinTeam(uhcPlayer, team);

                player.closeInventory();
            }));
        }
    }

    public static Map<Integer, ItemStack> getColorMap() {
        if (colorMap == null)
            initColorMap();

        return colorMap;
    }
}
