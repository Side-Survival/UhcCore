package com.gmail.val59000mc.scoreboard;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.configuration.YamlFile;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardLayout {

    private List<String> waiting;
    private List<String> playing;
    private List<String> deathmatch;
    private List<String> spectating;
    private String title;
    private String pointEntry;
    private String fullPointEntry;

    public void loadFile(){
        YamlFile cfg;

        try{
            cfg = FileUtils.saveResourceIfNotAvailable(UhcCore.getPlugin(), "scoreboard.yml");
        }catch (InvalidConfigurationException ex){
            ex.printStackTrace();

            // Set default values.
            waiting = new ArrayList<>();
            playing = new ArrayList<>();
            deathmatch = new ArrayList<>();
            spectating = new ArrayList<>();
            title = ChatColor.RED + "Error";
            return;
        }

        waiting = RandomUtils.color(cfg.getStringList("waiting"));
        playing = RandomUtils.color(cfg.getStringList("playing"));
        deathmatch = RandomUtils.color(cfg.getStringList("deathmatch"));
        spectating = RandomUtils.color(cfg.getStringList("spectating"));
        title = RandomUtils.color(cfg.getString("title", ""));
        pointEntry = cfg.getString("point-entry", "");
        fullPointEntry = cfg.getString("point-entry-full", "");
    }

    public List<String> getLines(ScoreboardType scoreboardType){
        if (scoreboardType.equals(ScoreboardType.WAITING)){
            return waiting;
        }
        if (scoreboardType.equals(ScoreboardType.PLAYING)){
            return playing;
        }
        if (scoreboardType.equals(ScoreboardType.DEATHMATCH)){
            return deathmatch;
        }
        if (scoreboardType.equals(ScoreboardType.SPECTATING)){
            return spectating;
        }
        return null;
    }

    public String getTitle(){
        return title;
    }

    public String getPointEntry() {
        return pointEntry;
    }

    public String getFullPointEntry() {
        return fullPointEntry;
    }
}