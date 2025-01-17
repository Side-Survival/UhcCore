package com.gmail.val59000mc.scoreboard.placeholders;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scoreboard.Placeholder;
import com.gmail.val59000mc.scoreboard.ScoreboardType;
import com.gmail.val59000mc.utils.TimeUtils;
import org.bukkit.entity.Player;

public class TimersPlaceholder extends Placeholder{

    private enum Event {
        PVP,
        DEATHMATCH,
        BORDER,
        GLOWING,
        NONE
    }

    private Event nextEvent;

    public TimersPlaceholder(){
        super("timers", "timer-name", "timer-time");
    }

    @Override
    public String getReplacement(UhcPlayer uhcPlayer, Player player, ScoreboardType scoreboardType, String placeholder) {
        if (nextEvent == null){
            nextEvent = getNextEvent();
        }

        if (nextEvent == Event.NONE)
            return null;

        long timeRemaining = getTimeRemaining(nextEvent);

        if (timeRemaining <= 0 && nextEvent != Event.NONE){
            nextEvent = getNextEvent();
            timeRemaining = getTimeRemaining(nextEvent);
        }

        // When all events have passed return empty string.
        if (timeRemaining == -1) {
            return null;
        }

        switch (placeholder){
            case "timers":
                return getEventName(nextEvent) + ": " + TimeUtils.getFormattedTime(timeRemaining);
            case "timer-name":
                return getEventName(nextEvent);
            case "timer-time":
                return TimeUtils.getFormattedTime(timeRemaining);
            default:
                return "?";
        }
    }

    private Event getNextEvent(){
        Event nearestEvent = Event.NONE;
        long nearestEventTime = -1;
        for (Event event : Event.values()){
            long l = getTimeRemaining(event);
            if (l > 0 && (nearestEventTime < 0 || l < nearestEventTime)){
                nearestEvent = event;
                nearestEventTime = l;
            }
        }

        return nearestEvent;
    }

    // todo: add to translations
    private String getEventName(Event event){
        switch (event){
            case PVP:
                return Lang.GAME_STAGE_PVP;
            case DEATHMATCH:
                return Lang.GAME_STAGE_DEATHMATCH;
            case BORDER:
                return Lang.GAME_STAGE_BORDER;
            case GLOWING:
                return Lang.GAME_STAGE_GLOWING;
            default:
                return "-";
        }
    }

    private long getTimeRemaining(Event event){
        GameManager gm = GameManager.getGameManager();
        MainConfig cfg = gm.getConfig();
        switch (event){
            case PVP:
                return cfg.get(MainConfig.TIME_BEFORE_PVP) - gm.getElapsedTime();
            case GLOWING:
                return cfg.get(MainConfig.TIME_BEFORE_GLOWING) - gm.getElapsedTime();
            case DEATHMATCH:
                return gm.getRemainingTime();
            case BORDER:
                return cfg.get(MainConfig.BORDER_TIME_BEFORE_SHRINK) - gm.getElapsedTime();
            default:
                return -1;
        }
    }

}