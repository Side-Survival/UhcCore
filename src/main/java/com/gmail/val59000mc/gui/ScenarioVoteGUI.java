package com.gmail.val59000mc.gui;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.TeamManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.utils.RandomUtils;
import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.InventoryManager;
import libs.fr.minuskube.inv.SmartInventory;
import libs.fr.minuskube.inv.content.InventoryContents;
import libs.fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ScenarioVoteGUI implements InventoryProvider {

    private final UhcCore plugin;
    private final InventoryManager invManager;
    private SmartInventory inventory;
    private static long updated = System.currentTimeMillis();
    private long localUpdated = System.currentTimeMillis();

    public ScenarioVoteGUI() {
        this.plugin = UhcCore.getPlugin();
        this.invManager = plugin.getInventoryManager();
    }

    public void load() {
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new ScenarioVoteGUI())
                .size(3, 9)
                .title(Lang.SCENARIO_GLOBAL_INVENTORY_VOTE)
                .build();
    }

    public void open(Player player) {
        this.load();
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemMeta itemMeta;
        ScenarioManager scenarioManager = GameManager.getGameManager().getScenarioManager();
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);

        Map<Scenario, Integer> votes = new HashMap<>();
        for (UhcPlayer uPlayer : GameManager.getGameManager().getPlayerManager().getPlayersList()){
            for (Scenario scenario : uPlayer.getScenarioVotes()){
                int totalVotes = votes.getOrDefault(scenario, 0) + 1;
                votes.put(scenario, totalVotes);
            }
        }

        Set<Scenario> playerVotes = uhcPlayer.getScenarioVotes();
        for (Scenario scenario : scenarioManager.getRegisteredScenarios()){
            ItemStack item = scenario.getScenarioItem().clone();
            itemMeta = item.getItemMeta();

            if (playerVotes.contains(scenario)) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            List<String> lore = itemMeta.getLore();
            lore.add("");
            lore.add(Lang.SCENARIO_GLOBAL_ITEM_INFO.replace("%votes%", String.valueOf(votes.getOrDefault(scenario, 0))));
            itemMeta.setLore(lore);

            item.setAmount(votes.getOrDefault(scenario, 0) + 1);
            item.setItemMeta(itemMeta);

            contents.add(ClickableItem.of(item, e -> {
                if (uhcPlayer.getScenarioVotes().contains(scenario)) {
                    uhcPlayer.getScenarioVotes().remove(scenario);
                    updated = System.currentTimeMillis();
                } else {
                    int maxVotes = GameManager.getGameManager().getConfig().get(MainConfig.MAX_SCENARIO_VOTES);
                    if (uhcPlayer.getScenarioVotes().size() == maxVotes){
                        player.sendMessage(Lang.SCENARIO_GLOBAL_VOTE_MAX.replace("%max%", String.valueOf(maxVotes)));
                        return;
                    }
                    uhcPlayer.getScenarioVotes().add(scenario);
                    updated = System.currentTimeMillis();
                }
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        if (updated <= localUpdated)
            return;

        localUpdated = updated;

        ItemMeta itemMeta;
        ScenarioManager scenarioManager = GameManager.getGameManager().getScenarioManager();
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);

        Map<Scenario, Integer> votes = new HashMap<>();
        for (UhcPlayer uPlayer : GameManager.getGameManager().getPlayerManager().getPlayersList()){
            for (Scenario scenario : uPlayer.getScenarioVotes()){
                int totalVotes = votes.getOrDefault(scenario, 0) + 1;
                votes.put(scenario, totalVotes);
            }
        }

        Set<Scenario> playerVotes = uhcPlayer.getScenarioVotes();
        int i = 0;
        for (Scenario scenario : scenarioManager.getRegisteredScenarios()){
            ItemStack item = scenario.getScenarioItem().clone();
            itemMeta = item.getItemMeta();

            if (playerVotes.contains(scenario)) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            List<String> lore = itemMeta.getLore();
            lore.add(Lang.SCENARIO_GLOBAL_ITEM_INFO.replace("%votes%", String.valueOf(votes.getOrDefault(scenario, 0))));
            itemMeta.setLore(lore);

            item.setAmount(votes.getOrDefault(scenario, 0) + 1);
            item.setItemMeta(itemMeta);

            contents.set(i / 9, i % 9, ClickableItem.of(item, e -> {
                if (uhcPlayer.getScenarioVotes().contains(scenario)) {
                    uhcPlayer.getScenarioVotes().remove(scenario);
                    updated = System.currentTimeMillis();
                } else {
                    int maxVotes = GameManager.getGameManager().getConfig().get(MainConfig.MAX_SCENARIO_VOTES);
                    if (uhcPlayer.getScenarioVotes().size() == maxVotes){
                        player.sendMessage(Lang.SCENARIO_GLOBAL_VOTE_MAX.replace("%max%", String.valueOf(maxVotes)));
                        return;
                    }
                    uhcPlayer.getScenarioVotes().add(scenario);
                    updated = System.currentTimeMillis();
                }
            }));
            i++;
        }
    }
}
