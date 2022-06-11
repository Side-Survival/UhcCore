package com.gmail.val59000mc.tournament;

import com.gmail.val59000mc.players.UhcTeam;

import java.util.*;

public class PointManager {

    private static PointManager instance = new PointManager();

    private Long arenaStartTime = 0L;
    private Map<UhcTeam, Integer> points = new HashMap<>();
    private List<String> history = new ArrayList<>();

    public static PointManager get() {
        return instance;
    }

    public void init(List<UhcTeam> teams) {
        for (UhcTeam uhcTeam : teams) {
            points.put(uhcTeam, 1);
        }
        arenaStartTime = System.currentTimeMillis();
    }

    public Map<UhcTeam, Integer> getPoints() {
        return points;
    }

    public List<String> getHistory() {
        return history;
    }

    public long getStartTime() {
        return arenaStartTime;
    }
}
