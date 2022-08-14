package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerChatListener implements Listener{

	private final PlayerManager playerManager;
	private final MainConfig configuration;
	private final List<UUID> tipReceived = new ArrayList<>();
	public static final List<UUID> staffChatToggled = new ArrayList<>();

	public PlayerChatListener(PlayerManager playerManager, MainConfig configuration){
		this.playerManager = playerManager;
		this.configuration = configuration;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String prefix = "&7";
		Player player = event.getPlayer();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
		boolean isPublic = event.getMessage().startsWith("@");

		if (staffChatToggled.contains(player.getUniqueId())) {
			String msg = RandomUtils.color("&4&l[STAFF] &c" + player.getName() + " &8» &c" + event.getMessage());
			for (Player oPlayer : Bukkit.getOnlinePlayers()) {
				if (oPlayer.hasPermission("uhc-core.commands.staffchat"))
					oPlayer.sendMessage(msg);
			}
			return;
		}

		try {
			if (uhcPlayer.getTeam() != null) {
				// Game chat
				prefix = RandomUtils.color("&8[" + uhcPlayer.getTeam().getTeamColor() + (!isPublic ? Lang.TEAM_CHAT_PREFIX : uhcPlayer.getTeam().getTeamName()) + "&8] &7");

				if (!isPublic) {
					event.getRecipients().clear();
					for (UhcPlayer member : uhcPlayer.getTeam().getOnlineMembers()) {
						event.getRecipients().add(member.getPlayer());
					}
				} else if (event.getMessage().length() > 1) {
					event.setMessage(event.getMessage().substring(1));
				} else {
					event.setCancelled(true);
					return;
				}
			}

			if (uhcPlayer.isDeath()) {
				// Spectator chat
				prefix = Lang.DISPLAY_SPECTATOR_CHAT;
				if (isPublic && player.hasPermission("uhccore.chat.all")) {
					prefix = "&c";
					if (uhcPlayer.getTeam() == null)
						event.setMessage(event.getMessage().substring(1));
				} else {
					event.getRecipients().clear();
					event.getRecipients().addAll(GameManager.getGameManager().getPlayerManager().getOnlinePlayers().stream().filter(UhcPlayer::isDeath).map(UhcPlayer::getPlayerForce).toList());
				}
			}

			event.setFormat(RandomUtils.color(prefix + player.getName() + " &8» &f%2$s"));
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("uhccore.chat.all") && !event.getRecipients().contains(p))
					p.sendMessage(event.getFormat().replace("%2$s", event.getMessage()));
				else if (p.hasPermission("uhccore.chat.hide"))
					event.getRecipients().remove(p);
			}

			if (uhcPlayer.getTeam() != null && !isPublic && !tipReceived.contains(player.getUniqueId()) && !uhcPlayer.isDeath()) {
				player.sendMessage(Lang.DISPLAY_CHAT_TIP);
				player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
				tipReceived.add(player.getUniqueId());
			}
		} catch (UhcPlayerNotOnlineException e) {
			e.printStackTrace();
		}
	}
}