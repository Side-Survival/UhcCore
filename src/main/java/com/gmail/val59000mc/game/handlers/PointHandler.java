package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.PointType;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.tournament.AssignManager;
import com.gmail.val59000mc.tournament.PointManager;
import com.gmail.val59000mc.utils.RandomUtils;
import lv.side.objects.SimpleTeam;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class PointHandler {

    private final UhcCore plugin = UhcCore.getPlugin();
    public Map<UhcTeam, Integer> points = new HashMap<>();
    public Map<UhcTeam, List<UhcTeam>> kills = new HashMap<>();
    public Map<Integer, Integer> placementPoints = new HashMap<>();
    public int placement = 0;
    public int addedPlace;
    private long startTime;

    public void init() {
        for (UhcTeam team : GameManager.getGameManager().getTeamManager().getNotEmptyUhcTeams()) {
            points.put(team, 1);
            kills.put(team, new ArrayList<>());
            placement++;
        }

        placementPoints.clear();
        for (Map.Entry<String, Integer> entry : GameManager.getGameManager().getConfig().getMap(MainConfig.POINTS_PLACEMENT).entrySet()) {
            placementPoints.put(Integer.valueOf(entry.getKey()), entry.getValue());
        }

        startTime = System.currentTimeMillis();
    }

    public void addGamePoints(UhcTeam team, PointType type) {
        int toAdd = 0;
        String timeNow = formatSeconds(System.currentTimeMillis() - startTime);
        GameManager gm = GameManager.getGameManager();
        PointManager pm = PointManager.get();

        if (type == PointType.KILL) {
            toAdd = gm.getConfig().get(MainConfig.POINTS_PER_KILL);

            pm.getHistory().add(
                    "[" + timeNow + " KILL punkti] " + team.getTeamName() + " +" + toAdd
            );

        } else if (type == PointType.TEAM_KILL) {
            toAdd = gm.getConfig().get(MainConfig.POINTS_PER_KILL) + gm.getConfig().get(MainConfig.POINTS_BONUS);

            pm.getHistory().add(
                    "[" + timeNow + " TEAM_KILL punkti] " + team.getTeamName() + " +" + toAdd
            );

        } else if (type == PointType.PLACEMENT) {
            toAdd = placementPoints.get(placement) - addedPlace;
            addedPlace += toAdd;

            int teamsAlive = 0;
            for (UhcTeam iUhcTeam : points.keySet()) {
                int aliveCount = iUhcTeam.getPlayingMemberCount();
                if (iUhcTeam != team && aliveCount == 0)
                    continue;

                if (iUhcTeam != team && aliveCount > 0)
                    teamsAlive++;

                pm.getPoints().put(iUhcTeam, pm.getPoints().getOrDefault(iUhcTeam, 0) + toAdd);
                addPoints(iUhcTeam, toAdd);

                if (iUhcTeam == team) {
                    pm.getHistory().add(
                            "[" + timeNow + " PLACEMENT punkti] " + team.getTeamName() + " +" + placementPoints.get(placement)
                    );
                }

                placement--;
            }

            if (!GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE)) {
                SimpleTeam simpleTeam = AssignManager.get().assignedTeams.get(team);
                if (simpleTeam != null)
                    AssignManager.get().assignedMatch.getAliveMs().put(simpleTeam.getId(), (int) (System.currentTimeMillis() - pm.getStartTime()));
            }

            placement = teamsAlive;
            return;
        }

        pm.getPoints().put(team, pm.getPoints().getOrDefault(team, 0) + toAdd);
        addPoints(team, toAdd);
    }

    public void addPoints(UhcTeam team, int toAdd) {
        if (!points.containsKey(team))
            points.put(team, 0);

        points.put(team, points.get(team) + toAdd);
        points = sortByValue(points);
    }

    public UhcTeam getWinnerUhcTeam() {
        points = sortByValue(points);

        for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
            return entry.getKey();
        }

        return null;
    }

    public ArrayList<String> getAll() {
        ArrayList<String> result = new ArrayList<>();

        int i = 1;
        boolean practiceMode = GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE);

        for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
            if (entry.getValue() == 0 && !practiceMode && !points.containsKey(entry.getKey()))
                continue;
            result.add(getAdvTeamPointsFormatted(entry.getKey(), i, entry.getValue(), !practiceMode));
            i++;
        }

        return result;
    }

    public ArrayList<String> getAll(Player player) {
        ArrayList<String> result = new ArrayList<>();

        int i = 1;
        boolean practiceMode = GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE);

        for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
            if (entry.getValue() == 0 && !practiceMode && !points.containsKey(entry.getKey()))
                continue;
            result.add(getTeamPointsFormatted(entry.getKey(), i, entry.getValue(), !practiceMode));
            i++;
        }

        return result;
    }

    public ArrayList<String> getTopPlaces(UhcPlayer uhcPlayer) {
        ArrayList<String> result = new ArrayList<>();

        int playerPos = getPlace(uhcPlayer.getTeam());

        int i = 1;
        int added = 0;

        for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
            if (i == 1 || (playerPos == i) ||
                (playerPos < 24 && (playerPos < i || playerPos - 1 == i)) ||
                (playerPos == 24 && i > 9)) {
                result.add(getTeamPointsFormatted(entry.getKey(), i, entry.getValue(), entry.getKey() == uhcPlayer.getTeam()));
                added++;
            }

            i++;
            if (added == 4)
                break;
        }

        return result;
    }

    public int getPlace(UhcTeam team) {
        int i = 1;
        for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
            if (entry.getKey() == team)
                return i;
            i++;
        }

        return -1;
    }

    private String getAdvTeamPointsFormatted(UhcTeam team, int place, int points, boolean added) {
        if (team == null)
            return "";

        return RandomUtils.color(GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().getPointEntry()
                .replace("%place%", String.valueOf(place))
                .replace("%team%", team.getFullPrefix())
                .replace("%points%", String.valueOf(points)) + (added ? " &7&o" + AssignManager.get().assignedTeams.get(team).getName() : "")
        );
    }

    private String getTeamPointsFormatted(UhcTeam team, int place, int points, boolean isPlayerTeam) {
        if (team == null)
            return "";

        return RandomUtils.color(GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().getPointEntry()
                .replace("%place%", String.valueOf(place))
                .replace("%team%", isPlayerTeam ? team.getFullPrefix(true) : team.getFullPrefix())
                .replace("%points%", String.valueOf(points))
        );
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void resetAll() {
        for (UhcTeam team : GameManager.getGameManager().getTeamManager().getUhcTeams()) {
            points.put(team, 0);
            kills.put(team, new ArrayList<>());
        }
        addedPlace = 0;
        placement = 0;
    }

    public boolean hasKilledBefore(UhcTeam team, UhcTeam toCheck) {
        return kills.get(team).contains(toCheck);
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public String formatSeconds(long ms) {
        Date d = new Date(ms);
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");

        return df.format(d);
    }
}
