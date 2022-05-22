package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.OreType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class DoubleOresListener extends ScenarioListener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.SURVIVAL || isEnabled(Scenario.VEIN_MINER)) {
            return;
        }

        Block block = e.getBlock();
        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
        Material type = block.getType();
        ItemStack drop = null;

        Optional<OreType> oreType = OreType.valueOf(type);
        System.out.println("mining!!! " + e.getPlayer() + " " + oreType + " " + oreType.isPresent());

        if (oreType.isPresent()) {
            int xp = oreType.get().getXpPerBlock() * 2;
            int count = oreType.get() != OreType.GOLD ? 2 : 1;
            System.out.println("oretype = " + oreType.get());

            if (oreType.get() == OreType.GOLD && isEnabled(Scenario.DOUBLE_GOLD)) {
                count *= 2;
            }

            drop = new ItemStack(oreType.get().getDrop(), count);
            UhcItems.spawnExtraXp(loc,xp);
        }

        if (type == Material.GRAVEL) {
            drop = new ItemStack(Material.FLINT);
        }

        if (drop != null) {
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, drop);
        }
    }
}