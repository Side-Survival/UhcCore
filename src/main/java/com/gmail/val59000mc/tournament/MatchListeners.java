package com.gmail.val59000mc.tournament;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import lv.side.events.RequestArenaMatchEvent;
import lv.side.events.RequestAvailableArenasEvent;
import lv.side.events.RequestMatchResetEvent;
import lv.side.events.response.ResponseArenaMatchEvent;
import lv.side.events.response.ResponseMatchResetEvent;
import lv.side.objects.MatchResponse;
import lv.side.objects.SimpleTeam;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchListeners implements Listener {

    private static UhcCore plugin = UhcCore.getPlugin();

    @EventHandler
    public void onAvailableArenasRequest(RequestAvailableArenasEvent event) {
        AssignManager.get().updateAvailableArenas();
    }

    @EventHandler
    public void onArenaMatchRequest(RequestArenaMatchEvent event) {
        GameManager gm = GameManager.getGameManager();
        if (AssignManager.get().assignedMatch != null || gm.getGameState() != GameState.WAITING) {
            MatchResponse response = new MatchResponse(event.getMatch().getCount(), 0);
            Bukkit.getPluginManager().callEvent(new ResponseArenaMatchEvent(response));
            return;
        }

        List<UhcTeam> enabledTeams = new ArrayList<>(gm.getTeamManager().getUhcTeams());
        Random rand = new Random();

        for (SimpleTeam team : event.getMatch().getTeams().keySet()) {
            UhcTeam uhcTeam = enabledTeams.remove(rand.nextInt(enabledTeams.size()));
            AssignManager.get().assignedTeams.put(uhcTeam, team);
            for (String member : team.getMembers()) {
                AssignManager.get().assignedPlayers.add(member);
            }
            System.out.println("[match] assigning " + uhcTeam.getTeamName() + " to " + team.getName());
        }

        AssignManager.get().assignedMatch = event.getMatch();

        MatchResponse response = new MatchResponse(event.getMatch().getCount(), 1);
        Bukkit.getPluginManager().callEvent(new ResponseArenaMatchEvent(response));

        String[] params = event.getMatch().getParams().split(";");
        for (String param : params) {
            int ind;
            try {
                ind = Integer.parseInt(param);
            } catch (NumberFormatException ignored) {
                continue;
            }
            Scenario scenario = gm.getScenarioManager().getRegisteredScenarios().get(ind);
            gm.getScenarioManager().enableScenario(scenario);
        }
    }

    @EventHandler
    public void onMatchReset(RequestMatchResetEvent event) {
        Bukkit.getPluginManager().callEvent(new ResponseMatchResetEvent(new MatchResponse(event.getMatch().getCount(), 0)));

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
            }
        }.runTaskLater(plugin, 40L);
    }
}
