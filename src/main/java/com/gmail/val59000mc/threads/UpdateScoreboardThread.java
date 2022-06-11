package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scoreboard.ScoreboardType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

public class UpdateScoreboardThread implements Runnable {

	private static final long UPDATE_DELAY = 20L;
	private final ScoreboardHandler scoreboardHandler;
	private ScoreboardType scoreboardType;

	public UpdateScoreboardThread(ScoreboardHandler scoreboardHandler) {
		this.scoreboardHandler = scoreboardHandler;
	}

	@Override
	public void run() {
		TextComponent scenarioMessage = new TextComponent(Lang.SCENARIO_VOTE);

		boolean practice = GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE);

		for (UhcPlayer uhcPlayer : GameManager.getGameManager().getPlayerManager().getPlayersList()) {
			if (!uhcPlayer.isOnline())
				continue;

			ScoreboardType newType = scoreboardHandler.getPlayerScoreboardType(uhcPlayer);
			if (scoreboardType != newType)
				scoreboardType = newType;

			scoreboardHandler.updatePlayerSidebar(uhcPlayer, scoreboardType);
			if (practice && scoreboardType == ScoreboardType.WAITING) {
				uhcPlayer.getPlayerForce().spigot().sendMessage(
						ChatMessageType.ACTION_BAR,
						scenarioMessage
				);
			}

			if (uhcPlayer.isDeath())
				uhcPlayer.getPlayerForce().setFireTicks(0);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(),this, UPDATE_DELAY);
	}

}