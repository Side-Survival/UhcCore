package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.*;
import com.gmail.val59000mc.scoreboard.ScoreboardLayout;
import com.gmail.val59000mc.scoreboard.ScoreboardManager;
import com.gmail.val59000mc.scoreboard.ScoreboardType;
import com.gmail.val59000mc.threads.UpdateScoreboardThread;
import com.gmail.val59000mc.utils.TimeUtils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ScoreboardHandler {

    private final GameManager gameManager;
    private final MainConfig config;
    private final ScoreboardLayout scoreboardLayout;
    private Map<UUID, FastBoard> boards = new HashMap<>();

    public ScoreboardHandler(GameManager gameManager, MainConfig config, ScoreboardLayout scoreboardLayout) {
        this.gameManager = gameManager;
        this.config = config;
        this.scoreboardLayout = scoreboardLayout;

        Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new UpdateScoreboardThread(this),1L);
    }

    public void setUpPlayerScoreboard(UhcPlayer uhcPlayer, Player bukkitPlayer) {
        FastBoard board = new FastBoard(bukkitPlayer);
        board.updateTitle(scoreboardLayout.getTitle());

        boards.put(uhcPlayer.getUuid(), board);
    }

    public void removePlayerScoreboard(Player player) {
        if (boards.containsKey(player.getUniqueId())) {
            boards.get(player.getUniqueId()).delete();
            boards.remove(player.getUniqueId());
        }
    }

    public void updatePlayerSidebar(UhcPlayer uhcPlayer, ScoreboardType scoreboardType) {
        FastBoard board = boards.get(uhcPlayer.getUuid());
        if (board == null)
            return;

        ScoreboardManager scoreboardManager = gameManager.getScoreboardManager();
        Player player;

        try {
            player = uhcPlayer.getPlayer();
        }catch (UhcPlayerNotOnlineException ex) {
            throw new RuntimeException(ex);
        }

        List<String> lines = new ArrayList<>();

        for (String line : scoreboardLayout.getLines(scoreboardType)) {
            if (line.equalsIgnoreCase("%top%")) {
                lines.addAll(gameManager.getPointHandler().getTopPlaces(uhcPlayer));
            } else {
                String s = scoreboardManager.translatePlaceholders(line, uhcPlayer, player, scoreboardType);
                if (s != null) {
                    lines.add(s);
                } else {
                    lines.remove(lines.size() - 1);
                    lines.remove(lines.size() - 1);
                }
            }
        }

        String time = TimeUtils.getFormattedTime(gameManager.getElapsedTime());
        board.updateTitle(scoreboardLayout.getTitle().replace("%time%", time.isEmpty() ? time : " " + time));
        board.updateLines(lines);
    }

    public ScoreboardType getPlayerScoreboardType(UhcPlayer uhcPlayer) {
        GameState gameState = gameManager.getGameState();

        if (gameState.equals(GameState.WAITING)){
            return ScoreboardType.WAITING;
        }

        if (uhcPlayer.getState().equals(PlayerState.DEAD) && uhcPlayer.getKills() <= 0) {
            return ScoreboardType.SPECTATING;
        }

        if (gameState.equals(GameState.PLAYING) || gameState.equals(GameState.ENDED)){
            return ScoreboardType.PLAYING;
        }

        if (gameState.equals(GameState.DEATHMATCH)){
            return ScoreboardType.DEATHMATCH;
        }

        return ScoreboardType.PLAYING;
    }

}
