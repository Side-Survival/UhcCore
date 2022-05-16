package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.maploader.MapLoader;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.schematics.DeathmatchArena;
import com.gmail.val59000mc.threads.StartDeathmatchThread;
import com.gmail.val59000mc.utils.LocationUtils;
import com.gmail.val59000mc.utils.UniversalSound;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DeathmatchHandler {

    private final GameManager gameManager;
    private final MainConfig config;
    private final PlayerManager playerManager;
    private final MapLoader mapLoader;
    private Clipboard dmCage;
    private Clipboard dmAir;

    public DeathmatchHandler(GameManager gameManager, MainConfig config, PlayerManager playerManager, MapLoader mapLoader) {
        this.gameManager = gameManager;
        this.config = config;
        this.playerManager = playerManager;
        this.mapLoader = mapLoader;

        File file = WorldEdit.getInstance().getWorkingDirectoryPath("schematics/dm-cage.schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            dmCage = reader.read();
        } catch (Exception ignored) {}

        file = WorldEdit.getInstance().getWorkingDirectoryPath("schematics/dm-air.schem").toFile();
        format = ClipboardFormats.findByFile(file);

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            dmAir = reader.read();
        } catch (Exception ignored) {}
    }

    public void startDeathmatch() {
        // DeathMatch can only be stated while GameState = Playing
        if (gameManager.getGameState() != GameState.PLAYING){
            return;
        }

        gameManager.setGameState(GameState.DEATHMATCH);
        gameManager.setPvp(false);
        gameManager.broadcastInfoMessage(Lang.GAME_START_DEATHMATCH);
        playerManager.playSoundToAll(UniversalSound.ENDERDRAGON_GROWL);

        startArenaDeathmatch();
    }

    private void startArenaDeathmatch() {
        World world = Bukkit.getWorld("uhc_arena");
        assert world != null;
        Location center = world.getSpawnLocation();

        mapLoader.setBorderSize(world, center.getBlockX(), center.getBlockZ(), config.get(MainConfig.DEATHMATCH_START_SIZE)*2);

        // Teleport players
        List<Location> spots = new ArrayList<>();
        List<UhcTeam> teams = gameManager.getTeamManager().getAliveUhcTeams();

        double deg = 360d / teams.size();
        double ang = deg;

        for (UhcTeam team : teams) {
            double x = (30 * Math.sin(ang));
            double z = (30 * Math.cos(ang));
            ang += deg;
            Location paste = new Location(center.getWorld(), center.getX() + x, 80, center.getZ() + z);
            paste = getGround(paste).getBlock().getLocation();
            pasteCage(paste);
            spots.add(paste);

            paste.add(0.5, 0, 0.5);

            int i = 0;
            for (UhcPlayer player : team.getMembers()) {
                Player bukkitPlayer;
                try {
                    bukkitPlayer = player.getPlayer();
                } catch (UhcPlayerNotOnlineException e) {
                    continue;
                }

                if (bukkitPlayer.getInventory().contains(Material.ENDER_PEARL)) {
                    bukkitPlayer.sendMessage(Lang.GAME_NO_PEARLS);
                    bukkitPlayer.getInventory().remove(Material.ENDER_PEARL);
                }

                if (player.getState().equals(PlayerState.PLAYING)) {
                    player.freezePlayer();
                    bukkitPlayer.teleport(i == 0 ? paste : paste.clone().add(0, 3, 0));
                    i++;
                } else {
                    bukkitPlayer.teleport(center);
                    bukkitPlayer.setAllowFlight(true);
                    bukkitPlayer.setFlying(true);
                }
            }
        }

        // Start Enable pvp thread
        Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StartDeathmatchThread(gameManager, true, spots), 20);
    }

    private Location getGround(Location loc) {
        while (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            loc.add(0, -1, 0);
            if (loc.getY() < 2)
                return loc;
        }

        return loc;
    }

    private void pasteCage(Location location) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(dmCage)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(location))
                    .build();
            Operations.complete(operation);
            editSession.commit();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    public void removeCage(Location location) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(dmAir)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(location))
                    .build();
            Operations.complete(operation);
            editSession.commit();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
}
