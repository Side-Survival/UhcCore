package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.gui.ScenarioVoteGUI;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scoreboard.ScoreboardType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class UpdateScoreboardThread implements Runnable {

	private static final long UPDATE_DELAY = 20L;
	private final ScoreboardHandler scoreboardHandler;
	private ScoreboardType scoreboardType;
	private GameManager gm = GameManager.getGameManager();

	public UpdateScoreboardThread(ScoreboardHandler scoreboardHandler) {
		this.scoreboardHandler = scoreboardHandler;
	}

	@Override
	public void run() {
		TextComponent scenarioMessage = new TextComponent(Lang.SCENARIO_VOTE);

		boolean practice = gm.getConfig().get(MainConfig.PRACTICE_MODE);

		for (UhcPlayer uhcPlayer : gm.getPlayerManager().getPlayersList()) {
			if (!uhcPlayer.isOnline())
				continue;

			ScoreboardType newType = scoreboardHandler.getPlayerScoreboardType(uhcPlayer);
			if (scoreboardType != newType)
				scoreboardType = newType;

			scoreboardHandler.updatePlayerSidebar(uhcPlayer, scoreboardType);
			Player player = uhcPlayer.getPlayerForce();
			if (scoreboardType == ScoreboardType.WAITING && player.getGameMode() == GameMode.ADVENTURE) {
				if (practice && !ScenarioVoteGUI.hasVoted.contains(uhcPlayer.getUuid())){
					player.spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							scenarioMessage
					);
				}

				if (player.getLocation().getY() < 90)
					player.teleport(gm.getMapLoader().getLobby().getLocation());
			}

			if (uhcPlayer.isDeath())
				uhcPlayer.getPlayerForce().setFireTicks(0);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(),this, UPDATE_DELAY);
	}

}