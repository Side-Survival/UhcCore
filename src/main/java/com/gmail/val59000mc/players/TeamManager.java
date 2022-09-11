package com.gmail.val59000mc.players;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.gui.TeamGUI;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.utils.color.ColorUtils;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamManager {

    private final GameManager gameManager = GameManager.getGameManager();
    private Map<Integer, UhcTeam> teams = new HashMap<>();
    private TeamGUI teamGUI;

    public void init() {
        teamGUI = new TeamGUI();
        update();
    }

    public void update() {
        teams.clear();
        int teamAmount = gameManager.getConfig().get(MainConfig.TEAM_AMOUNT);
        teamGUI.load(teamAmount);

        List<java.awt.Color> colors = ColorUtils.generateVisuallyDistinctColors(teamAmount, .8f, .3f);

        for (int i = 1; i <= teamAmount; i++) {
            teams.put(i, new UhcTeam(i, Color.fromRGB(colors.get(i - 1).getRed(), colors.get(i - 1).getGreen(), colors.get(i - 1).getBlue())));
        }
        TeamGUI.updateColorMap();
    }

    public List<UhcTeam> getPlayingUhcTeams(){
        List<UhcTeam> result = new ArrayList<>();
        for (UhcTeam team : teams.values()){
            if (team.getPlayingMemberCount() != 0){
                result.add(team);
            }
        }
        return result;
    }

    public List<UhcTeam> getNotEmptyUhcTeams(){
        List<UhcTeam> result = new ArrayList<>();
        for (UhcTeam team : teams.values()) {
            if (!team.getMembers().isEmpty()) {
                result.add(team);
            }
        }
        return result;
    }

    public List<UhcTeam> getAliveUhcTeams(){
        List<UhcTeam> result = new ArrayList<>();
        for (UhcTeam team : teams.values()){
            if (team.getAlivePlayingMemberCount() > 0){
                result.add(team);
            }
        }
        return result;
    }

    public List<UhcTeam> getUhcTeams(){
        return new ArrayList<>(teams.values());
    }

    public void joinTeam(UhcPlayer uhcPlayer, UhcTeam team){
        try{
            team.join(uhcPlayer);
        }catch (UhcTeamException ex){
            uhcPlayer.sendMessage(ex.getMessage());
        }
    }

    @Nullable
    public UhcTeam getTeamById(int id){
        return teams.get(id);
    }

    @Nullable
    public UhcTeam getTeamByName(String name){
        for (UhcTeam team : teams.values()){
            if (team.getTeamName().equals(name)){
                return team;
            }
        }

        return null;
    }

    public void openTeamSelection(Player player) {
        teamGUI.open(player);
    }

    public void openTeamSelection(Player player, int page) {
        teamGUI.open(player, page);
    }
}