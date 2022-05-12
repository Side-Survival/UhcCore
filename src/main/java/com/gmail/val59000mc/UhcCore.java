package com.gmail.val59000mc;

import java.util.logging.Level;

import com.gmail.val59000mc.adapters.VersionAdapter;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.utils.FileUtils;

import com.gmail.val59000mc.utils.Placeholders;
import libs.fr.minuskube.inv.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UhcCore extends JavaPlugin{

	private static final int MIN_VERSION = 8;
	private static final int MAX_VERSION = 19;

	private static UhcCore pl;
	private static VersionAdapter versionAdapter;
	private static int version;
	private GameManager gameManager;
	private InventoryManager inventoryManager;

	@Override
	public void onEnable(){
		pl = this;

		loadServerVersion();

		try {
			versionAdapter = VersionAdapter.instantiate();
			getLogger().info("Successfully loaded version adapter: " + versionAdapter.getClass().getName());
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Unable to start plugin", e);
			return;
		}

		inventoryManager = new InventoryManager(this);
		inventoryManager.init();

		gameManager = new GameManager();
		Bukkit.getScheduler().runTaskLater(this, () -> gameManager.loadNewGame(), 1);

		// Delete files that are scheduled for deletion
		FileUtils.removeScheduledDeletionFiles();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			new Placeholders(this).register();
	}

	// Load the Minecraft version.
	private void loadServerVersion(){
		String versionString = Bukkit.getBukkitVersion();
		version = 0;

		for (int i = MIN_VERSION; i <= MAX_VERSION; i ++){
			if (versionString.contains("1." + i)){
				version = i;
			}
		}

		if (version == 0) {
			version = MIN_VERSION;
			Bukkit.getLogger().warning("[UhcCore] Failed to detect server version! " + versionString + "?");
		}else {
			Bukkit.getLogger().info("[UhcCore] 1." + version + " Server detected!");
		}
	}

	public static int getVersion() {
		return version;
	}
	
	public static UhcCore getPlugin(){
		return pl;
	}

	public static VersionAdapter getVersionAdapter() {
		return versionAdapter;
	}

	@Override
	public void onDisable(){
		gameManager.getScenarioManager().disableAllScenarios();

		Bukkit.getLogger().info("[UhcCore] Plugin disabled");
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}
}
