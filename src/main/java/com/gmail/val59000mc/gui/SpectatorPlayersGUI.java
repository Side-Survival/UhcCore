package com.gmail.val59000mc.gui;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpectatorPlayersGUI implements InventoryProvider {

    private final UhcCore plugin;
    private final InventoryManager invManager;
    private SmartInventory inventory;

    public SpectatorPlayersGUI() {
        this.plugin = UhcCore.getPlugin();
        this.invManager = plugin.getInventoryManager();
    }

    public void load(int playerCount) {
        int size = playerCount / 9 + 1;
        this.inventory = SmartInventory.builder()
                .manager(invManager)
                .provider(new SpectatorPlayersGUI())
                .size(Math.min(size, 6), 9)
                .title(Lang.SPECTATOR_GUI_TITLE)
                .build();
    }

    public void open(Player player, int playerCount) {
        this.load(playerCount);
        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        TeamManager tm = GameManager.getGameManager().getTeamManager();
        List<UhcTeam> aliveTeams = tm.getAliveUhcTeams();
        ItemMeta itemMeta;

        for (int i = 1; i < 25; i++) {
            UhcTeam team = tm.getTeamById(i);
            if (team == null || !aliveTeams.contains(team))
                continue;

            for (UhcPlayer uhcPlayer : team.getOnlinePlayingMembers()) {
                ItemStack item = TeamGUI.getColorMap().get(i).clone();
                itemMeta = item.getItemMeta();
                assert itemMeta != null;
                itemMeta.setDisplayName(Lang.SPECTATOR_GUI_ITEM
                        .replace("%color%", team.getFullPrefix())
                        .replace("%player%", uhcPlayer.getName())
                );
                itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                List<String> lore = new ArrayList<>(Lang.SPECTATOR_GUI_LORE);

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);

                contents.add(ClickableItem.of(item, e -> {
                    player.closeInventory();
                    if (!uhcPlayer.isPlaying())
                        return;

                    try {
                        Player target = uhcPlayer.getPlayer();
                        player.teleport(target.getLocation());
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    } catch (UhcPlayerNotOnlineException ignored) {}
                }));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
