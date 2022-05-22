package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.OreType;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CutCleanListener extends ScenarioListener{

    @Option(key = "check-correct-tool")
    private boolean checkTool = false;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        for(int i=0 ; i<e.getDrops().size() ; i++){
            UniversalMaterial replaceBy = null;
            UniversalMaterial type = UniversalMaterial.ofType(e.getDrops().get(i).getType());
            if (type != null) {
                switch (type) {
                    case RAW_BEEF:
                        replaceBy = UniversalMaterial.COOKED_BEEF;
                        break;
                    case RAW_CHICKEN:
                        replaceBy = UniversalMaterial.COOKED_CHICKEN;
                        break;
                    case RAW_MUTTON:
                        replaceBy = UniversalMaterial.COOKED_MUTTON;
                        break;
                    case RAW_RABBIT:
                        replaceBy = UniversalMaterial.COOKED_RABBIT;
                        break;
                    case RAW_PORK:
                        replaceBy = UniversalMaterial.COOKED_PORKCHOP;
                        break;
                    default:
                        break;
                }
            }
            if(replaceBy != null){
                ItemStack cookedFood = e.getDrops().get(i).clone();
                cookedFood.setType(replaceBy.getType());
                e.getDrops().set(i, cookedFood);
            }
        }
        if (e.getEntityType() == EntityType.COW) {
            e.getDrops().add(new ItemStack(Material.LEATHER));
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e){
        if (e.getPlayer().getGameMode() != GameMode.SURVIVAL || isEnabled(Scenario.TRIPLE_ORES) || (isEnabled(Scenario.VEIN_MINER) && e.getPlayer().isSneaking())){
            return;
        }

        Block block = e.getBlock();
        Material tool = e.getPlayer().getItemInHand().getType();
        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
        Material type = block.getType();
        ItemStack drop = null;

        Optional<OreType> oreType = OreType.valueOf(type);

        if (oreType.isPresent() && (!checkTool || oreType.get().isCorrectTool(tool))) {
            int xp = oreType.get().getXpPerBlock();
            int count = (oreType.get() != OreType.GOLD && oreType.get() != OreType.DIAMOND) ? 2 : 1;

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

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.SUGAR_CANE) {
            event.getEntity().getItemStack().setType(Material.BOOK);
            if (getScenarioManager().isEnabled(Scenario.FLY_HIGH))
                event.getEntity().getWorld().dropItem(event.getLocation(), new ItemStack(Material.PAPER));
        }
    }
}