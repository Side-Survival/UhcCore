package com.gmail.val59000mc.players;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UhcTeam {

	private final List<UhcPlayer> members = new ArrayList<>();
	private boolean readyToStart = false;
	private Location startingLocation = null;
	private final int teamNumber;
	private Color color;
	private String teamColor;
	private String prefix;
	private final Inventory teamInventory;

	public UhcTeam(int teamNumber, Color color) {
		this.teamNumber = teamNumber;
		this.prefix = "#" + teamNumber;
		this.color = color;
		this.teamColor = RandomUtils.color(String.format("&#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
		teamInventory = Bukkit.createInventory(null, 27, ChatColor.BOLD + "Team Inventory");
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public String getTeamName() {
		return prefix;
	}

	public Color getColor() {
		return color;
	}

	public String getTeamColor() {
		return teamColor;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getFullPrefix() {
		return teamColor + prefix;
	}

	public String getFullPrefix(boolean bold) {
		return teamColor + prefix;
	}

	public Inventory getTeamInventory() {
		return teamInventory;
	}

	public void sendMessage(String message){
		members.forEach(p -> p.sendMessage(message));
	}

	public boolean contains(UhcPlayer player){
		return members.contains(player);
	}

	public synchronized List<UhcPlayer> getMembers(){
		return members;
	}

	public List<UhcPlayer> getMembers(Predicate<UhcPlayer> filter){
		return members.stream().filter(filter).collect(Collectors.toList());
	}

	public int getMemberCount(){
		return members.size();
	}

	public boolean isSolo(){
		return getMemberCount() == 1;
	}

	public int getPlayingMemberCount(){
		return getMembers(UhcPlayer::isPlaying).size();
	}

	public int getAlivePlayingMemberCount() {
		return getOnlinePlayingMembers().size();
	}

	public int getKills(){
		return members.stream()
				.mapToInt(UhcPlayer::getKills)
				.sum();
	}

	public List<UhcPlayer> getOnlineMembers(){
		return members.stream()
				.filter(UhcPlayer::isOnline)
				.collect(Collectors.toList());
	}

	public List<UhcPlayer> getOnlinePlayingMembers(){
		return members.stream()
				.filter(UhcPlayer::isPlaying)
				.filter(UhcPlayer::isOnline)
				.collect(Collectors.toList());
	}

	public List<String> getMembersNames(){
		List<String> names = new ArrayList<>();
		for(UhcPlayer player : getMembers()){
			names.add(player.getName());
		}
		return names;
	}

	public void join(UhcPlayer player) throws UhcTeamException {
		if(isFull() && GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE)) {
			player.sendPrefixedMessage(Lang.TEAM_MESSAGE_FULL.replace("%player%", player.getName()).replace("%limit%", ""+ GameManager.getGameManager().getConfig().get(MainConfig.MAX_PLAYERS_PER_TEAM)));
			throw new UhcTeamException(Lang.TEAM_MESSAGE_FULL.replace("%player%", player.getName()).replace("%limit%", ""+ GameManager.getGameManager().getConfig().get(MainConfig.MAX_PLAYERS_PER_TEAM)));
		}else{
			if (player.getTeam() != null)
				player.getTeam().leave(player);

			player.sendPrefixedMessage(Lang.TEAM_MESSAGE_JOIN_AS_PLAYER.replace("%team%", getFullPrefix()));
			for(UhcPlayer teamMember : getMembers()){
				teamMember.sendPrefixedMessage(Lang.TEAM_MESSAGE_PLAYER_JOINS.replace("%player%",player.getName()));
			}
			getMembers().add(player);
			player.setTeam(this);
		}
	}

	public boolean isFull() {
		MainConfig cfg = GameManager.getGameManager().getConfig();
		return (cfg.get(MainConfig.MAX_PLAYERS_PER_TEAM) == getMembers().size());
	}

	public void leave(UhcPlayer player) {
		getMembers().remove(player);
		player.setTeam(null);

		player.sendPrefixedMessage(Lang.TEAM_MESSAGE_LEAVE_AS_PLAYER);
		for(UhcPlayer teamMember : getMembers()){
			teamMember.sendMessage(Lang.TEAM_MESSAGE_PLAYER_LEAVES.replace("%player%", player.getName()));
		}
	}

	public boolean isReadyToStart(){
		return readyToStart;
	}

	public boolean isOnline(){
		return members.stream().anyMatch(UhcPlayer::isOnline);
	}

	public void regenTeam(boolean doubleRegen) {
		for(UhcPlayer uhcPlayer : getMembers()){
			uhcPlayer.sendPrefixedMessage(Lang.ITEMS_REGEN_HEAD_ACTION);
			try{
				Player p = uhcPlayer.getPlayer();
				p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,100,doubleRegen?2:1));
			}catch(UhcPlayerNotOnlineException e){
				// No regen for offline players
			}
		}

	}

	public void setStartingLocation(Location loc){
		this.startingLocation = loc;
	}

	public Location getStartingLocation(){
		return startingLocation;
	}
}