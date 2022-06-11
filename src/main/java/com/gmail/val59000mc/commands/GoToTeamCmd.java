package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class GoToTeamCmd implements CommandExecutor {

    private final GameManager gameManager;

    public GoToTeamCmd(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player player) || args.length < 1)
            return true;

        int teamId;
        try {
            teamId = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            return true;
        }

        UhcTeam team = gameManager.getTeamManager().getTeamById(teamId);
        if (team == null)
            return true;

        Random rand = new Random();
        List<Player> players = new ArrayList<>();

        for (UhcPlayer uhcPlayer : team.getOnlinePlayingMembers()) {
            players.add(uhcPlayer.getPlayerForce());
        }

        if (players.isEmpty())
            return true;

        Player targetPlayer = players.remove(rand.nextInt(players.size()));
        Location loc = targetPlayer.getLocation();
        if (targetPlayer.getLocation().getBlock().getRelative(0, 2, 0).getType() == Material.AIR)
            loc = targetPlayer.getLocation().add(0, 1, 0);

        player.teleport(loc);
        player.setAllowFlight(true);
        player.setFlying(true);

        return true;
    }
}
