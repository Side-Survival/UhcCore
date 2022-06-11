package com.gmail.val59000mc.tournament;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import lv.side.events.response.ResponseAvailableArenasEvent;
import lv.side.events.response.ResponseMatchFinishedEvent;
import lv.side.events.response.ResponseMatchStartEvent;
import lv.side.objects.EventMatch;
import lv.side.objects.MatchResponse;
import lv.side.objects.SimpleTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignManager {

    private static AssignManager instance = new AssignManager();
    private static UhcCore plugin = UhcCore.getPlugin();

    public EventMatch assignedMatch = null;
    public Map<UhcTeam, SimpleTeam> assignedTeams = new HashMap<>();
    public List<String> assignedPlayers = new ArrayList<>();
    public Map<String, UhcTeam> playerTeams = new HashMap<>();

    public static AssignManager get() {
        return instance;
    }

    public void playerJoin(Player player) {
        if (player.hasPermission("event.spectate"))
            return;

        if (!assignedPlayers.contains(player.getName())) {
            player.kickPlayer("Notika kļūda pievienojot tevi spēlei, lūdzu sazinies ar turnīra organizatoriem!");
            return;
        }

        if (GameManager.getGameManager().getGameState() != GameState.WAITING)
            return;

        UhcTeam team = null;
        for (Map.Entry<UhcTeam, SimpleTeam> entry : assignedTeams.entrySet()) {
            if (entry.getValue().getMembers().contains(player.getName())) {
                team = entry.getKey();
                System.out.println("[match] found team " + entry.getValue().getName() + " for " + player.getName());
                break;
            }
        }

        if (team == null) {
            player.kickPlayer("Notika kļūda pievienojot tevi spēlei (nevarēja saistīt komandu), lūdzu sazinies ar turnīra organizatoriem!");
            return;
        }

        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
        playerTeams.put(player.getName(), team);

        System.out.println("[match] joining " + player.getName() + " to " + team.getTeamName());
        if (uhcPlayer.getTeam() != null && uhcPlayer.getTeam() == team)
            return;

        try {
            team.join(uhcPlayer);
        } catch (UhcTeamException e) {
            e.printStackTrace();
        }
    }

    public void updateAvailableArenas() {
        List<String> arenas = new ArrayList<>();
        GameManager gameManager = GameManager.getGameManager();

        if (gameManager.getGameState() == GameState.WAITING && assignedMatch == null)
            arenas.add("world");

        Bukkit.getPluginManager().callEvent(new ResponseAvailableArenasEvent(arenas));
    }

    public void callMatchStartEvent(int count, int code) {
        Bukkit.getPluginManager().callEvent(new ResponseMatchStartEvent(new MatchResponse(count, code)));
    }

    public void callMatchFinishedEvent(EventMatch match) {
        Bukkit.getPluginManager().callEvent(new ResponseMatchFinishedEvent(match));
    }
}
