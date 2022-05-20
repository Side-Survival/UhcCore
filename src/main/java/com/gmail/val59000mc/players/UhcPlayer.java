package com.gmail.val59000mc.players;

import com.gmail.val59000mc.events.UhcPlayerStateChangedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.utils.RandomUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class UhcPlayer {
	private final UUID uuid;
	private final String name;

	private UhcTeam team;
	private PlayerState state;
	private boolean globalChat;
	private int kills;
	private boolean hasBeenTeleportedToLocation;
	private final Map<String,Integer> craftedItems;
	private final Set<UhcTeam> teamInvites;
	private final Set<Scenario> scenarioVotes;
	private final Set<ItemStack> storedItems;

	private String nickName;
	private Scoreboard scoreboard;
	private boolean frozen;
	private UUID offlineZombieUuid;
	private UhcPlayer compassTarget = null;
	private int browsingPage;

	public UhcPlayer(UUID uuid, String name){
		this.uuid = uuid;
		this.name = name;

		team = null;
		setState(PlayerState.WAITING);
		globalChat = false;
		kills = 0;
		hasBeenTeleportedToLocation = false;
		craftedItems = new HashMap<>();
		teamInvites = new HashSet<>();
		scenarioVotes = new HashSet<>();
		storedItems = new HashSet<>();
		offlineZombieUuid = null;

		browsingPage = 0;
	}

	public Player getPlayer() throws UhcPlayerNotOnlineException {
		Player player = Bukkit.getPlayer(uuid);
		if(player != null)
			return player;
		throw new UhcPlayerNotOnlineException(name);
	}

	public Player getPlayerForce() {
		return Bukkit.getPlayer(uuid);
	}

	public Boolean isOnline(){
		Player player = Bukkit.getPlayer(uuid);
		return player != null;
	}

	/**
	 * Used to get the player name.
	 * @return Returns the player name, when they are nicked the nick-name will be returned.
	 */
	public String getName(){
		if (nickName != null){
			return nickName;
		}
		return name;
	}

	/**
	 * Used to get the players real name.
	 * @return Returns the players real name, even when they are nicked.
	 */
	public String getRealName(){
		return name;
	}

	/**
	 * Use ProtocolUtils.setPlayerNickName(); instead!
	 * @param nickName The player nickname. (Make sure its not over 16 characters long!)
	 */
	public void setNickName(String nickName){
		if (nickName != null){
			Validate.isTrue(nickName.length() <= 16, "Nickname is too long! (Max 16 characters)");
		}
		this.nickName = nickName;
	}

	public boolean hasNickName(){
		return nickName != null;
	}

	/**
	 * Used to get the player display-name.
	 * @return Returns the player team color (when enabled) followed by their name.
	 */
	public String getDisplayName(){
		return RandomUtils.color(team.getFullPrefix(true) + "&f " + getName());
	}

	public UUID getUuid() {
		return uuid;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public synchronized UhcTeam getTeam(){
		return team;
	}

	public synchronized void setTeam(UhcTeam team) {
		this.team = team;
	}

	public PlayerState getState() {
		return state;
	}

	public boolean isWaiting(){
		return state == PlayerState.WAITING;
	}

	public boolean isPlaying(){
		return state == PlayerState.PLAYING;
	}

	public boolean isDeath(){
		return state == PlayerState.DEAD;
	}

	public void setState(PlayerState state) {
		if (this.state == state){
			return; // Don't change the player state when the same.
		}

		PlayerState oldState = this.state;
		this.state = state;

		// Call UhcPlayerStateChangedEvent
		Bukkit.getPluginManager().callEvent(new UhcPlayerStateChangedEvent(this, oldState, state));
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void freezePlayer(){
		frozen = true;
	}

	public void releasePlayer(){
		frozen = false;
	}

	public synchronized Set<Scenario> getScenarioVotes() {
		return scenarioVotes;
	}

	public synchronized Set<UhcTeam> getTeamInvites() {
		return teamInvites;
	}

	public synchronized Set<ItemStack> getStoredItems(){
		return storedItems;
	}

	public UUID getOfflineZombieUuid() {
		return offlineZombieUuid;
	}

	public void setOfflineZombieUuid(UUID offlineZombieUuid) {
		this.offlineZombieUuid = offlineZombieUuid;
	}

	/**
	 * Counts the times the player has crafted the item.
	 * @param craftName Name of the craft.
	 * @param limit The maximum amount of time the player is allowed to craft the item.
	 * @return Returns true if crafting is allowed.
	 */
	public boolean addCraftedItem(String craftName, int limit){
		int quantity = craftedItems.getOrDefault(craftName, 0);

		if(quantity+1 <= limit){
			craftedItems.put(craftName,	quantity+1);
			return true;
		}

		return false;
	}

	public boolean isInTeamWith(UhcPlayer player) {
		return team != null && team.contains(player);
	}

	public void sendPrefixedMessage(String message){
		sendMessage(Lang.DISPLAY_MESSAGE_PREFIX+" "+message);
	}

	public void sendMessage(String message){
		try {
			getPlayer().sendMessage(message);
		} catch (UhcPlayerNotOnlineException e) {
			// No message to send
		}
	}

	public boolean isGlobalChat() {
		return globalChat;
	}

	public void setGlobalChat(boolean globalChat) {
		this.globalChat = globalChat;
	}

	public int getKills() {
		return kills;
	}

	public void addKill(){
		kills++;
	}

	public UhcPlayer pointCompassToNextPlayer() {
		PlayerManager pm = GameManager.getGameManager().getPlayerManager();
		List<UhcPlayer> pointPlayers = new ArrayList<>(team.getOnlinePlayingMembers());

		if ((pointPlayers.size() == 1 && pointPlayers.contains(this)) || pointPlayers.size() == 0) {
			return null;
		} else {
			int currentIndice = -1;
			for (int i = 0 ; i < pointPlayers.size() ; i++) {
				if (pointPlayers.get(i).equals(compassTarget))
					currentIndice = i;
			}

			// Switching to next player
			if (currentIndice == pointPlayers.size()-1)
				currentIndice = 0;
			else
				currentIndice++;


			// Skipping player if == this
			if (pointPlayers.get(currentIndice).equals(this))
				currentIndice++;

			// Correct indice if out of bounds
			if (currentIndice == pointPlayers.size())
				currentIndice = 0;

			// Pointing compass
			compassTarget = pointPlayers.get(currentIndice);

			return compassTarget;
		}
	}

	public void selectDefaultGlobalChat() {
		if (team.getMembers().size() == 1) {
			setGlobalChat(true);
		}
	}

	public Location getStartingLocation(){
		return team.getStartingLocation();
	}

	public boolean getHasBeenTeleportedToLocation() {
		return hasBeenTeleportedToLocation;
	}

	public void setHasBeenTeleportedToLocation(boolean hasBeenTeleportedToLocation) {
		this.hasBeenTeleportedToLocation = hasBeenTeleportedToLocation;
	}

	public int getBrowsingPage() {
		return browsingPage;
	}

	public void setBrowsingPage(int browsingPage) {
		this.browsingPage = browsingPage;
	}

	public UhcPlayer getCompassTarget() {
		return compassTarget;
	}
}
