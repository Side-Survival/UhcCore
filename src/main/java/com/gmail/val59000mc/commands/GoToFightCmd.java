package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoToFightCmd implements CommandExecutor {

    private final GameManager gameManager;
    private final Map<UUID, UUID> lastTeleports = new HashMap<>();

    public GoToFightCmd(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (gameManager.getLastFight() == null || lastTeleports.get(player.getUniqueId()) == gameManager.getLastFight())
            return true;

        Player targetPlayer = Bukkit.getPlayer(gameManager.getLastFight());
        if (targetPlayer == null || targetPlayer.getGameMode() != GameMode.SURVIVAL)
            return true;

        lastTeleports.put(player.getUniqueId(), targetPlayer.getUniqueId());
        gameManager.setLastFight(targetPlayer.getUniqueId());

        Location loc = targetPlayer.getLocation();
        if (targetPlayer.getLocation().getBlock().getRelative(0, 2, 0).getType() == Material.AIR)
            loc = targetPlayer.getLocation().add(0, 1, 0);

        player.teleport(loc);
        player.setAllowFlight(true);
        player.setFlying(true);

        return true;
    }
}
