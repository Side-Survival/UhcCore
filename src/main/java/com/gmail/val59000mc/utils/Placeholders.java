package com.gmail.val59000mc.utils;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
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
        } else if (identifier.equalsIgnoreCase("team_number")) {
            if (!player.isOnline())
                return "";

            UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player.getPlayer());
            UhcTeam uhcTeam = uhcPlayer.getTeam();

            int num = uhcTeam.getTeamNumber();
            String chars = "abcdefghijklmnopqrstuvwxyz";

            return String.valueOf(chars.charAt(num / 9) + (num % 9 > 0 ? num % 9 : 9));
        } else if (identifier.equalsIgnoreCase("motd")) {
            GameManager gm = GameManager.getGameManager();
            if (gm == null){
                return "0/48;" + Lang.DISPLAY_MOTD_LOADING;
            }

            String online = gm.getPlayerManager().getAliveOnlinePlayers().size() + "/48;";
            int spectateCount = gm.getPlayerManager().getOnlineSpectators().size();
            if (spectateCount > 0)
                online = online.substring(0, online.length() - 1) + " (" + spectateCount + " vēro);";

            if (gm.getGameState() == null)
                return "0/48;" + Lang.DISPLAY_MOTD_LOADING;

            switch(gm.getGameState()){
                case ENDED:
                    return online + Lang.DISPLAY_MOTD_ENDED;

                case DEATHMATCH:
                case PLAYING:
                    return online + Lang.DISPLAY_MOTD_PLAYING;

                case STARTING:
                    return online + Lang.DISPLAY_MOTD_STARTING;

                case WAITING:
                    return online + Lang.DISPLAY_MOTD_WAITING;

                default:
                    return "0/48;" + Lang.DISPLAY_MOTD_LOADING;
            }
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

        return uhcTeam.getTeamColor() + uhcTeam.getPrefix() + nickColor + " ";
    }
}
