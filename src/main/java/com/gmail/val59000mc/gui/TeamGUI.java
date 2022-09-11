package com.gmail.val59000mc.gui;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
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
import libs.fr.minuskube.inv.content.Pagination;
import libs.fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TeamGUI implements InventoryProvider {

    private final UhcCore plugin;
    private final InventoryManager invManager;
    private final GameManager gameManager = GameManager.getGameManager();
    public static Map<Integer, ItemStack> colorMap;
    public static ItemStack prevPage;
    public static ItemStack nextPage;
    private SmartInventory inventory;

    public TeamGUI() {
        this.plugin = UhcCore.getPlugin();
        this.invManager = plugin.getInventoryManager();

        prevPage = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = prevPage.getItemMeta();
        itemMeta.setDisplayName(RandomUtils.color("&e←"));
        prevPage.setItemMeta(itemMeta);

        nextPage = new ItemStack(Material.ARROW);
        itemMeta = nextPage.getItemMeta();
        itemMeta.setDisplayName(RandomUtils.color("&e→"));
        nextPage.setItemMeta(itemMeta);
    }

    public static void updateColorMap() {
        colorMap = new HashMap<>();
        int amount = GameManager.getGameManager().getConfig().get(MainConfig.TEAM_AMOUNT);
        TeamManager teamManager = GameManager.getGameManager().getTeamManager();

        for (int i = 1; i <= amount; i++) {
            UhcTeam team = teamManager.getTeamById(i);
            if (team == null)
                continue;

            colorMap.put(i, RandomUtils.getColoredChestPlate(team.getColor().getRed(), team.getColor().getGreen(), team.getColor().getBlue()));
        }
    }

    public void load(int amount) {
        int size = amount / 9;
        if (size < 0)
            size = 1;
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

    public void open(Player player, int page) {
        this.inventory.open(player, page);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        TeamManager teamManager = GameManager.getGameManager().getTeamManager();

        Pagination pagination = contents.pagination();
        List<ClickableItem> items = new ArrayList<>();

        for (int i = 1; i <= gameManager.getConfig().get(MainConfig.TEAM_AMOUNT); i++) {
            UhcTeam team = teamManager.getTeamById(i);
            if (team == null)
                continue;

            ItemStack item = colorMap.get(i).clone();
            itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(Lang.TEAM_FULL_NAME.replace("%id%", team.getFullPrefix()));
            itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>(Lang.TEAM_GUI_LORE);

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                lore.add(RandomUtils.color(" &f" + uhcPlayer.getName()));
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            items.add(ClickableItem.of(item, e -> {
                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
                teamManager.joinTeam(uhcPlayer, team);

                player.closeInventory();
            }));
        }

        pagination.setItems(items.toArray(new ClickableItem[]{}));
        pagination.setItemsPerPage(45);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));

        if (items.size() > 45) {
            if (!pagination.isFirst())
                contents.set(5, 2, ClickableItem.of(prevPage, e -> teamManager.openTeamSelection(player, pagination.previous().getPage())));
            if (!pagination.isLast())
                contents.set(5, 6, ClickableItem.of(nextPage, e -> teamManager.openTeamSelection(player, pagination.next().getPage())));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        TeamManager teamManager = GameManager.getGameManager().getTeamManager();

        Pagination pagination = contents.pagination();
        List<ClickableItem> items = new ArrayList<>();

        for (int i = 1; i <= gameManager.getConfig().get(MainConfig.TEAM_AMOUNT); i++) {
            UhcTeam team = teamManager.getTeamById(i);
            if (team == null)
                continue;

            ItemStack item = colorMap.get(i).clone();
            itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(Lang.TEAM_FULL_NAME.replace("%id%", team.getFullPrefix()));
            itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>(Lang.TEAM_GUI_LORE);

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                lore.add(RandomUtils.color(" &f" + uhcPlayer.getName()));
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            items.add(ClickableItem.of(item, e -> {
                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
                teamManager.joinTeam(uhcPlayer, team);

                player.closeInventory();
            }));
        }

        pagination.setItems(items.toArray(new ClickableItem[]{}));
    }

    public static Map<Integer, ItemStack> getColorMap() {
        if (colorMap == null)
            updateColorMap();

        return colorMap;
    }
}
