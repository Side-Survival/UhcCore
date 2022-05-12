package com.gmail.val59000mc.utils;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private UhcCore plugin;

    public Placeholders(UhcCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "uhccore";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equalsIgnoreCase("team_prefix")) {
            if (!player.isOnline())
                return "";
            Player onlinePlayer = player.getPlayer();

            return getTeamPrefixFormatted(onlinePlayer);
        }
        return null;
    }

    public static String getTeamPrefixFormatted(Player player) {
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
        UhcTeam uhcTeam = uhcPlayer.getTeam();

        String nickColor = "&7";
        if (player.hasPermission("bm.admin.chat"))
            nickColor = "&c";

        if (uhcTeam == null)
            return nickColor;

        return uhcTeam.getTeamColor() + "&l" + uhcTeam.getPrefix() + nickColor + " ";
    }
}
