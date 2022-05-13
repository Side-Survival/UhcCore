package com.gmail.val59000mc.gui;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import libs.fr.minuskube.inv.ClickableItem;
import libs.fr.minuskube.inv.InventoryManager;
import libs.fr.minuskube.inv.SmartInventory;
import libs.fr.minuskube.inv.content.InventoryContents;
import libs.fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;

public class ScenarioGUI implements InventoryProvider {

    private final UhcCore plugin;
    private final InventoryManager invManager;
    private SmartInventory inventory;

    public ScenarioGUI() {
        this.plugin = UhcCore.getPlugin();
        this.invManager = plugin.getInventoryManager();
    }

    public void load(int scenarioCount) {
        int size = scenarioCount / 9 + 1;
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new ScenarioGUI())
                .size(Math.min(size, 6), 9)
                .title(Lang.SCENARIO_GLOBAL_INVENTORY)
                .build();
    }

    public void open(Player player, int scenarioCount) {
        this.load(scenarioCount);
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ScenarioManager scenarioManager = GameManager.getGameManager().getScenarioManager();

        for (Scenario scenario : scenarioManager.getEnabledScenarios()){
            contents.add(ClickableItem.empty(scenario.getScenarioItem()));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
