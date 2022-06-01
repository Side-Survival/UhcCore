package com.gmail.val59000mc.players;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.GameItem;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.*;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerJoinException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.PointType;
import com.gmail.val59000mc.game.handlers.CustomEventHandler;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.threads.*;
import com.gmail.val59000mc.utils.*;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerManager {

	private final CustomEventHandler customEventHandler;
	private final ScoreboardHandler scoreboardHandler;
	private final List<UhcPlayer> players;
	private long lastDeathTime;

	public PlayerManager(CustomEventHandler customEventHandler, ScoreboardHandler scoreboardHandler) {
		this.customEventHandler = customEventHandler;
		this.scoreboardHandler = scoreboardHandler;
		players = Collections.synchronizedList(new ArrayList<>());
	}

	public void setLastDeathTime() {
		lastDeathTime = System.currentTimeMillis();
	}

	public boolean isPlayerAllowedToJoin(Player player) throws UhcPlayerJoinException {
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer;

		switch(gm.getGameState()){
			case LOADING:
				throw new UhcPlayerJoinException(Lang.KICK_LOADING.replace("%percent%", gm.getMapLoader().getActiveLoaderThread().getChunksLoaded() + "%"));

			case WAITING:
				return true;

			case STARTING:
				if (doesPlayerExist(player)){
					uhcPlayer = getUhcPlayer(player);
					if(uhcPlayer.getState().equals(PlayerState.PLAYING)){
						return true;
					}else{
						throw new UhcPlayerJoinException(Lang.KICK_STARTING);
					}
				}else{
					throw new UhcPlayerJoinException(Lang.KICK_STARTING);
				}
			case DEATHMATCH:
			case PLAYING:
				if (doesPlayerExist(player)){
					uhcPlayer = getUhcPlayer(player);

					boolean canSpectate = gm.getConfig().get(MainConfig.CAN_SPECTATE_AFTER_DEATH);
					if(
							uhcPlayer.getState().equals(PlayerState.PLAYING) ||
							((canSpectate || player.hasPermission("uhc-core.spectate.override")) && uhcPlayer.getState().equals(PlayerState.DEAD))
					){
						return true;
					}else{
						throw new UhcPlayerJoinException(Lang.KICK_PLAYING);
					}
				}else{
					if(player.hasPermission("uhc-core.join-override")
							|| player.hasPermission("uhc-core.spectate.override")
							|| gm.getConfig().get(MainConfig.CAN_JOIN_AS_SPECTATOR) && gm.getConfig().get(MainConfig.CAN_SPECTATE_AFTER_DEATH)){
						UhcPlayer spectator = newUhcPlayer(player);
						gm.getPlayerManager().setPlayerSpectating(spectator);
						return true;
					}
					throw new UhcPlayerJoinException(Lang.KICK_PLAYING);
				}

			case ENDED:
				if(player.hasPermission("uhc-core.join-override")){
					return true;
				}
				throw new UhcPlayerJoinException(Lang.KICK_ENDED);

		}
		return false;
	}

	/**
	 * This method is used to get the UhcPlayer object from Bukkit Player.
	 * When using this method in the PlayerJoinEvent please check the doesPlayerExist(Player) to see if the player has a matching UhcPlayer.
	 * @param player The Bukkit player you want the UhcPlayer from.
	 * @return Returns a UhcPlayer.
	 */
	public UhcPlayer getUhcPlayer(Player player){
		try {
			return getUhcPlayer(player.getUniqueId());
		}catch (UhcPlayerDoesNotExistException ex){
			throw new RuntimeException(ex);
		}
	}

	public boolean doesPlayerExist(Player player){
		try {
			getUhcPlayer(player.getUniqueId());
			return true;
		}catch (UhcPlayerDoesNotExistException ex){
			return false;
		}
	}

	public UhcPlayer getUhcPlayer(String name) throws UhcPlayerDoesNotExistException {
		for(UhcPlayer uhcPlayer : getPlayersList()){
			if(uhcPlayer.getName().equals(name)) {
				return uhcPlayer;
			}
		}
		throw new UhcPlayerDoesNotExistException(name);
	}

	public UhcPlayer getUhcPlayer(UUID uuid) throws UhcPlayerDoesNotExistException {
		for(UhcPlayer uhcPlayer : getPlayersList()){
			if(uhcPlayer.getUuid().equals(uuid)) {
				return uhcPlayer;
			}
		}
		throw new UhcPlayerDoesNotExistException(uuid.toString());
	}

	public UhcPlayer getOrCreateUhcPlayer(Player player){
		if (doesPlayerExist(player)){
			return getUhcPlayer(player);
		}else{
			return newUhcPlayer(player);
		}
	}

    public synchronized UhcPlayer newUhcPlayer(Player bukkitPlayer){
        return newUhcPlayer(bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
    }

    public synchronized UhcPlayer newUhcPlayer(UUID uuid, String name){
        UhcPlayer newPlayer = new UhcPlayer(uuid, name);
        getPlayersList().add(newPlayer);
        return newPlayer;
    }

	public synchronized List<UhcPlayer> getPlayersList(){
		return players;
	}

	public Set<UhcPlayer> getOnlinePlayingPlayers() {
		return players.stream()
				.filter(UhcPlayer::isPlaying)
				.filter(UhcPlayer::isOnline)
				.collect(Collectors.toSet());
	}

	public Set<UhcPlayer> getOnlinePlayers() {
		return players.stream()
				.filter(UhcPlayer::isOnline)
				.collect(Collectors.toSet());
	}

	public Set<UhcPlayer> getAllPlayingPlayers() {
		return players.stream()
				.filter(UhcPlayer::isPlaying)
				.collect(Collectors.toSet());
	}

	public Set<UhcPlayer> getAliveOnlinePlayers() {
		return players.stream()
				.filter(UhcPlayer::isOnline)
				.filter(uhcPlayer -> !uhcPlayer.isDeath())
				.collect(Collectors.toSet());
	}

	public Set<UhcPlayer> getOnlineSpectators() {
		return players.stream()
				.filter(UhcPlayer::isOnline)
				.filter(UhcPlayer::isDeath)
				.collect(Collectors.toSet());
	}

	public void playerJoinsTheGame(Player player){
		UhcPlayer uhcPlayer;

		if (doesPlayerExist(player)){
			uhcPlayer = getUhcPlayer(player);
		}else{
			uhcPlayer = newUhcPlayer(player);
			Bukkit.getLogger().warning("[UhcCore] None existent player joined!");
		}

		GameManager gm = GameManager.getGameManager();
		scoreboardHandler.setUpPlayerScoreboard(uhcPlayer, player);

		switch(uhcPlayer.getState()){
			case WAITING:
				setPlayerWaitsAtLobby(uhcPlayer);

				if(gm.getConfig().get(MainConfig.AUTO_ASSIGN_PLAYER_TO_TEAM)){
					autoAssignPlayerToTeam(uhcPlayer);
				}
				uhcPlayer.sendPrefixedMessage(Lang.PLAYERS_WELCOME_NEW);
				break;
			case PLAYING:
				setPlayerStartPlaying(uhcPlayer);

				if (!uhcPlayer.getHasBeenTeleportedToLocation()) {
					// Apply start potion effect.
					if (uhcPlayer.getDeathLocation() == null) {
						for (PotionEffect effect : GameManager.getGameManager().getConfig().get(MainConfig.POTION_EFFECT_ON_START)) {
							player.addPotionEffect(effect);
						}
					}

					// Teleport player
					player.teleport(uhcPlayer.getStartingLocation());
					uhcPlayer.setHasBeenTeleportedToLocation(true);

					// Remove lobby potion effects.
					player.removePotionEffect(PotionEffectType.BLINDNESS);
					player.setLevel(0);

					// Call event
					Bukkit.getPluginManager().callEvent(new PlayerStartsPlayingEvent(uhcPlayer));
				}

				if (uhcPlayer.getOfflineZombieUuid() != null){
					Optional<LivingEntity> zombie = player.getWorld().getLivingEntities()
							.stream()
							.filter(e -> e.getUniqueId().equals(uhcPlayer.getOfflineZombieUuid()))
							.findFirst();

					zombie.ifPresent(Entity::remove);
					uhcPlayer.setOfflineZombieUuid(null);
				}

				uhcPlayer.sendPrefixedMessage(Lang.PLAYERS_WELCOME_BACK_IN_GAME);

				if (gm.isGlowing())
					player.addPotionEffect(EnableGlowingThread.effect);
				if (gm.isWithering())
					player.addPotionEffect(GameManager.witherEffect);

				try {
					for (UhcPlayer uPlayer : getOnlinePlayers()) {
						if (!uPlayer.isPlaying())
							player.hidePlayer(UhcCore.getPlugin(), uPlayer.getPlayer());
					}
				} catch (UhcPlayerNotOnlineException ignored) {}

				break;
			case DEAD:
				setPlayerSpectateAtLobby(uhcPlayer);
				break;
		}
	}

	private void autoAssignPlayerToTeam(UhcPlayer uhcPlayer) {
		GameManager gm = GameManager.getGameManager();

		for(UhcTeam team : listUhcTeams()){
			if (team != uhcPlayer.getTeam() && team.getMembers().size() < gm.getConfig().get(MainConfig.MAX_PLAYERS_PER_TEAM)){
				try {
					team.join(uhcPlayer);
				} catch (UhcTeamException ignored) {
				}
				return;
			}
		}

		try {
			uhcPlayer.getPlayer().kickPlayer("Nav vairs vietas komandās, pievienojies pēc spēles sākuma!");
		} catch (UhcPlayerNotOnlineException ignored) {}
	}

	public void setPlayerWaitsAtLobby(UhcPlayer uhcPlayer){
		uhcPlayer.setState(PlayerState.WAITING);
		GameManager gm = GameManager.getGameManager();
		Player player;
		try {
			player = uhcPlayer.getPlayer();
			player.teleport(gm.getMapLoader().getLobby().getLocation());
			clearPlayerInventory(player);
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(20);
			player.setExhaustion(20);
			player.setFoodLevel(20);
			player.setExp(0);
			player.setLevel(0);

			UhcItems.giveLobbyItemsTo(player);
		} catch (UhcPlayerNotOnlineException e) {
			// Do nothing because WAITING is a safe state
		}

	}

	public void setPlayerStartPlaying(UhcPlayer uhcPlayer){

		Player player;
		MainConfig cfg = GameManager.getGameManager().getConfig();

		if(!uhcPlayer.getHasBeenTeleportedToLocation()){
			uhcPlayer.setState(PlayerState.PLAYING);
			uhcPlayer.selectDefaultGlobalChat();

			try {
				player = uhcPlayer.getPlayer();
				clearPlayerInventory(player);
				player.setFireTicks(0);

				for(PotionEffect effect : player.getActivePotionEffects())
				{
					player.removePotionEffect(effect.getType());
				}
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1), false);
				player.setGameMode(GameMode.SURVIVAL);
				player.setLevel(0);
				if(cfg.get(MainConfig.ENABLE_EXTRA_HALF_HEARTS)){
					VersionUtils.getVersionUtils().setPlayerMaxHealth(player, 20+((double) cfg.get(MainConfig.EXTRA_HALF_HEARTS)));
					player.setHealth(20+((double) cfg.get(MainConfig.EXTRA_HALF_HEARTS)));
				}
				if (uhcPlayer.getTeam().getMembers().size() > 1)
					UhcItems.giveGameItemTo(player, GameItem.COMPASS_ITEM);

				if (GameManager.getGameManager().getScenarioManager().isEnabled(Scenario.TEAM_INVENTORY))
					UhcItems.giveGameItemTo(player, GameItem.TEAM_CHEST);

				if (!uhcPlayer.getStoredItems().isEmpty()){
					uhcPlayer.getStoredItems().forEach(item -> player.getInventory().addItem(item));
					uhcPlayer.applyStoredArmorOffhand(player);
					uhcPlayer.getStoredItems().clear();
				}
			} catch (UhcPlayerNotOnlineException e) {
				// Nothing done
			}
		}
	}

	private void clearPlayerInventory(Player player) {
		player.getInventory().clear();

		//clear player armor
		ItemStack[] emptyArmor = new ItemStack[4];
		for(int i=0 ; i<emptyArmor.length ; i++){
			emptyArmor[i] = new ItemStack(Material.AIR);
		}
		player.getInventory().setArmorContents(emptyArmor);

	}

	public void setPlayerSpectating(UhcPlayer uhcPlayer) {
		uhcPlayer.setState(PlayerState.DEAD);

		try {
			Player player = uhcPlayer.getPlayer();
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);
			player.setFlying(true);
			player.getEquipment().clear();
			clearPlayerInventory(player);

			for (UhcPlayer uPlayer : getOnlinePlayers()) {
				uPlayer.getPlayer().hidePlayer(UhcCore.getPlugin(), player);
				if (!uPlayer.isPlaying())
					player.hidePlayer(UhcCore.getPlugin(), uPlayer.getPlayer());
			}

			UhcItems.giveGameItemTo(player, GameItem.SPECTATOR_SPAWN);
			if (player.hasPermission("uhc-core.spectator-players"))
				UhcItems.giveGameItemTo(player, GameItem.SPECTATOR_PLAYERS);
		} catch (UhcPlayerNotOnlineException ignored) {}
	}

	public void setPlayerSpectateAtLobby(UhcPlayer uhcPlayer){
		GameManager gm = GameManager.getGameManager();

		uhcPlayer.setState(PlayerState.DEAD);
		if (uhcPlayer.getTeam() != null)
			uhcPlayer.sendPrefixedMessage(Lang.PLAYERS_WELCOME_BACK_SPECTATING);

		if(gm.getConfig().get(MainConfig.SPECTATING_TELEPORT)) {
			uhcPlayer.sendPrefixedMessage(Lang.COMMAND_SPECTATING_HELP);
		}

		Player player;
		try {
			player = uhcPlayer.getPlayer();
			player.getEquipment().clear();
			clearPlayerInventory(player);
			player.setGameMode(GameMode.ADVENTURE);

			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

			UhcItems.giveGameItemTo(player, GameItem.SPECTATOR_SPAWN);
			if (player.hasPermission("uhc-core.spectator-players"))
				UhcItems.giveGameItemTo(player, GameItem.SPECTATOR_PLAYERS);

			if(gm.getGameState().equals(GameState.DEATHMATCH) || gm.getGameState().equals(GameState.ENDED)) {
				player.teleport(Bukkit.getWorld("uhc_arena").getSpawnLocation());
			}else{
				Location loc = RandomUtils.getSafePoint(gm.getMapLoader().getUhcWorld(World.Environment.NORMAL).getBlockAt(0, 70, 0).getLocation());
				player.teleport(loc);
				player.setAllowFlight(true);
				player.setFlying(true);
			}

			for (UhcPlayer uPlayer : getOnlinePlayers()) {
				uPlayer.getPlayer().hidePlayer(UhcCore.getPlugin(), player);
				if (!uPlayer.isPlaying())
					player.hidePlayer(UhcCore.getPlugin(), uPlayer.getPlayer());
			}

		} catch (UhcPlayerNotOnlineException ignored) {}
	}

	public void setAllPlayersEndGame() {
		GameManager gm = GameManager.getGameManager();
		MainConfig cfg = gm.getConfig();

		List<UhcPlayer> winners = getWinners();

		if (!winners.isEmpty()) {
			UhcPlayer player1 = winners.get(0);
			gm.broadcastInfoMessage(Lang.PLAYERS_WON_TEAM.replace("%team%", player1.getTeam().getTeamName()));
			gm.getPointHandler().setPlacement(1);
			gm.getPointHandler().addGamePoints(player1.getTeam(), PointType.PLACEMENT);
		}

		// send to bungee
		if(cfg.get(MainConfig.ENABLE_BUNGEE_SUPPORT) && cfg.get(MainConfig.TIME_BEFORE_SEND_BUNGEE_AFTER_END) >= 0){
			for(UhcPlayer player : getPlayersList()){
				Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new TimeBeforeSendBungeeThread(this, player, cfg.get(MainConfig.TIME_BEFORE_SEND_BUNGEE_AFTER_END)));
			}
		}

		UhcWinEvent event = new UhcWinEvent(new HashSet<>(winners));
		Bukkit.getServer().getPluginManager().callEvent(event);

		customEventHandler.handleWinEvent(new HashSet<>(winners));

		// When the game finished set all player states to DEAD
		getPlayersList().forEach(player -> {
			player.setState(PlayerState.DEAD);
			Player bukkitPlayer = player.getPlayerForce();
			if (bukkitPlayer != null) {
				bukkitPlayer.removePotionEffect(PotionEffectType.WITHER);
				bukkitPlayer.setFireTicks(0);
			}
		});
	}

	private List<UhcPlayer> getWinners(){
		List<UhcPlayer> winners = new ArrayList<>();
		for(UhcPlayer player : getPlayersList()){
			try{
				Player connected = player.getPlayer();
				if(connected.isOnline() && player.getState().equals(PlayerState.PLAYING))
					winners.add(player);
			}catch(UhcPlayerNotOnlineException e){
				// not adding the player to winner list
			}
		}
		return winners;
	}

	public List<UhcTeam> listUhcTeams(){
		return GameManager.getGameManager().getTeamManager().getUhcTeams();
	}

	public void randomTeleportTeams() {
		GameManager gm = GameManager.getGameManager();
		World world = gm.getMapLoader().getUhcWorld(World.Environment.NORMAL);
		double maxDistance = 0.9 * gm.getConfig().get(MainConfig.BORDER_START_SIZE);

		// For solo players to join teams
		if (gm.getConfig().get(MainConfig.FORCE_ASSIGN_SOLO_PLAYER_TO_TEAM_WHEN_STARTING)){
			List<UhcPlayer> onlinePlayers = new ArrayList<>(getPlayersList());
			for (UhcPlayer uhcPlayer : onlinePlayers) {
				// If player is spectating don't assign player.
				if (uhcPlayer.getState() == PlayerState.DEAD){
					continue;
				}

				if (uhcPlayer.getTeam() == null){
					autoAssignPlayerToTeam(uhcPlayer);
				}
			}
		}

		gm.getPointHandler().init();

		List<Location> locations = new ArrayList<>();
		List<UhcTeam> notEmptyTeams = GameManager.getGameManager().getTeamManager().getNotEmptyUhcTeams();
		double minDistanceBetween = (maxDistance * 2) / (Math.sqrt(notEmptyTeams.size()) + 1);
		if (minDistanceBetween > 30)
			minDistanceBetween -= 30;

		for (UhcTeam team : notEmptyTeams) {
			Location newLoc = LocationUtils.findRandomSafeLocation(world, maxDistance, locations, minDistanceBetween);
			if (newLoc == null) {
				UhcCore.getPlugin().getLogger().log(Level.SEVERE, "Unable to find starting location!");
				return;
			}
			locations.add(newLoc);
			team.setStartingLocation(newLoc);
		}

		Bukkit.getPluginManager().callEvent(new UhcPreTeleportEvent());

		long delayTeleportByTeam = 0;
		StringBuilder bcMessage = new StringBuilder(Lang.SCENARIO_GLOBAL_HEADER);

		List<String> scenarioNames = new ArrayList<>();
		for (Scenario scenario : gm.getScenarioManager().getEnabledScenarios()) {
			scenarioNames.add(scenario.getInfo().getName());

			bcMessage.append("\n").append(Lang.SCENARIO_GLOBAL_DESCRIPTION_HEADER.replace("%scenario%", scenario.getInfo().getName()));
			bcMessage.append("\n").append(Lang.SCENARIO_GLOBAL_DESCRIPTION_PREFIX);
			for (String s : scenario.getInfo().getDescription()) {
				bcMessage.append(s).append(" ");
			}
		}
		bcMessage.append("\n ");

		String enabledScenarios = String.join(", ", scenarioNames);

		gm.broadcastMessage(bcMessage.toString());
		sendTitleAll(
				Lang.SCENARIO_GLOBAL_TITLE,
				Lang.SCENARIO_GLOBAL_SUBTITLE.replace("%scenarios%", enabledScenarios),
				5, 80, 5
		);

		gm.getMapLoader().enableDay();

		for (UhcTeam team : gm.getTeamManager().getNotEmptyUhcTeams()) {
			for (UhcPlayer uhcPlayer : team.getMembers()){
				gm.getPlayerManager().setPlayerStartPlaying(uhcPlayer);
			}

			if (!team.isOnline())
				continue;

			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new TeleportPlayersThread(GameManager.getGameManager(), team), delayTeleportByTeam);
			Bukkit.getLogger().info("[UhcCore] Teleporting a team in "+delayTeleportByTeam+" ticks");
			delayTeleportByTeam += 10; // ticks
		}

		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> GameManager.getGameManager().startWatchingEndOfGame(), delayTeleportByTeam + 60);
	}

	public void strikeLightning(UhcPlayer uhcPlayer) {
		Location loc;
		try{
			loc = uhcPlayer.getPlayer().getLocation();
		}catch(UhcPlayerNotOnlineException e){
			loc = new Location(GameManager.getGameManager().getMapLoader().getUhcWorld(World.Environment.NORMAL),0, 200,0);
		}

		loc.getWorld().strikeLightningEffect(loc);

		// Extinguish fire
		if (loc.getBlock().getType() == Material.FIRE) {
			loc.getBlock().setType(Material.AIR);
		}
	}

	public void playSoundToAll(UniversalSound sound) {
		for(UhcPlayer player : getPlayersList()){
			playSoundTo(player, sound);
		}
	}

	public void playSoundToAll(UniversalSound sound, float v, float v1){
		for(UhcPlayer player : getPlayersList()){
			playSoundTo(player, sound,v,v1);
		}
	}

	public void playSoundTo(UhcPlayer player, UniversalSound sound) {
		playSoundTo(player,sound,1,1);
	}

	public void playSoundTo(UhcPlayer player, UniversalSound sound, float v, float v1) {
		try {
			Player p = player.getPlayer();
			p.playSound(p.getLocation(), sound.getSound(), v, v1);
		} catch (UhcPlayerNotOnlineException e) {
			// No sound played
		}
	}

	public void sendTitleAll(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		}
	}

	public void playSoundAll(Sound sound, float volume, float pitch) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}

	public void checkIfRemainingPlayers(){
		int playingPlayers = 0;
		int playingPlayersOnline = 0;
		int playingTeams = 0;
		int playingTeamsOnline = 0;

		for(UhcTeam team : GameManager.getGameManager().getTeamManager().getNotEmptyUhcTeams()){

			int teamIsOnline = 0;
			int teamIsPlaying = 0;

			for(UhcPlayer player : team.getMembers()){
				if(player.getState().equals(PlayerState.PLAYING)){
					playingPlayers++;
					teamIsPlaying = 1;
					try{
						player.getPlayer();
						playingPlayersOnline++;
						teamIsOnline = 1;
					}catch(UhcPlayerNotOnlineException e){
						// Player isn't online
					}
				}
			}

			playingTeamsOnline += teamIsOnline;
			playingTeams += teamIsPlaying;
		}

		GameManager gm = GameManager.getGameManager();
		MainConfig cfg = gm.getConfig();
		if(playingPlayers == 0){
			gm.endGame();
		}
		else if(
				gm.getGameState() == GameState.DEATHMATCH &&
				cfg.get(MainConfig.ENABLE_DEATHMATCH_FORCE_END) &&
				gm.getPvp() &&
				(lastDeathTime+(cfg.get(MainConfig.DEATHMATCH_FORCE_END_DELAY)*TimeUtils.SECOND)) < System.currentTimeMillis()
		){
			gm.endGame();
		}
		else if(playingPlayers>0 && playingPlayersOnline == 0){
			// Check if all playing players have left the game
			if(cfg.get(MainConfig.END_GAME_WHEN_ALL_PLAYERS_HAVE_LEFT)){
				gm.startEndGameThread();
			}
		}
		else if(playingPlayers>0 && playingPlayersOnline > 0 && playingTeamsOnline == 1 && playingTeams == 1 && !cfg.get(MainConfig.ONE_PLAYER_MODE)){
			// Check if one playing team remains
			gm.endGame();
		}
		else if(gm.getGameIsEnding()){
			gm.stopEndGameThread();
		}
	}

	public void startWatchPlayerPlayingThread() {
		for(Player player : Bukkit.getOnlinePlayers()){
			player.removePotionEffect(PotionEffectType.BLINDNESS);
		}

		// Unfreeze players
		for (UhcPlayer uhcPlayer : getPlayersList()){
			uhcPlayer.releasePlayer();
			Bukkit.getPluginManager().callEvent(new PlayerStartsPlayingEvent(uhcPlayer));
		}

		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckRemainingPlayerThread(GameManager.getGameManager()) , 40);
		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CompassThread(GameManager.getGameManager()) , 10);
	}

	public void sendPlayerToBungeeServer(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(GameManager.getGameManager().getConfig().get(MainConfig.SERVER_BUNGEE));
		player.sendMessage(Lang.PLAYERS_SEND_BUNGEE_NOW);
		player.sendPluginMessage(UhcCore.getPlugin(), "BungeeCord", out.toByteArray());
	}

	public void spawnOfflineZombieFor(Player player){
		UhcPlayer uhcPlayer = getUhcPlayer(player);

		Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
		zombie.setCustomName(uhcPlayer.getDisplayName());
		zombie.setCustomNameVisible(true);
		zombie.setBaby(false);
		zombie.setAI(false);

		EntityEquipment equipment = zombie.getEquipment();
		equipment.setHelmet(VersionUtils.getVersionUtils().createPlayerSkull(player.getName(), player.getUniqueId()));
		equipment.setChestplate(player.getInventory().getChestplate());
		equipment.setLeggings(player.getInventory().getLeggings());
		equipment.setBoots(player.getInventory().getBoots());
		equipment.setItemInMainHand(player.getInventory().getItemInMainHand());
		equipment.setItemInOffHand(player.getInventory().getItemInOffHand());

		uhcPlayer.getStoredItems().clear();
		List<ItemStack> storedDrops = new ArrayList<>();

		for (ItemStack item : player.getInventory().getContents()){
			if (item != null){
				storedDrops.add(item);
			}
		}

		uhcPlayer.checkArmorOffhand(player, storedDrops);
		uhcPlayer.getStoredItems().addAll(storedDrops);

		uhcPlayer.setOfflineZombieUuid(zombie.getUniqueId());
	}

	public UhcPlayer revivePlayer(UUID uuid, String name, boolean spawnWithItems){
		UhcPlayer uhcPlayer;

		try{
			uhcPlayer = getUhcPlayer(uuid);
		}catch (UhcPlayerDoesNotExistException ex){
			uhcPlayer = newUhcPlayer(uuid, name);
		}

		revivePlayer(uhcPlayer, spawnWithItems);
		return uhcPlayer;
	}

	public void revivePlayer(UhcPlayer uhcPlayer, boolean spawnWithItems){
		if (uhcPlayer.getTeam() == null)
			return;

		uhcPlayer.setHasBeenTeleportedToLocation(false);
		uhcPlayer.setState(PlayerState.PLAYING);

		if (uhcPlayer.getDeathLocation() == null) {
			GameManager gm = GameManager.getGameManager();
			Location loc;
			if (gm.getGameState() == GameState.DEATHMATCH || gm.getGameState() == GameState.ENDED)
				loc = Bukkit.getWorld("uhc_arena").getSpawnLocation();
			else
				loc = RandomUtils.getSafePoint(gm.getMapLoader().getUhcWorld(World.Environment.NORMAL).getBlockAt(0, 70, 0).getLocation());

			uhcPlayer.setDeathLocation(loc);
		}

		// If not respawn with items, clear stored items.
		if (!spawnWithItems){
			uhcPlayer.getStoredItems().clear();
		}

		try{
			Player player = uhcPlayer.getPlayer();
			playerJoinsTheGame(player);

			for (UhcPlayer onlinePlayer : getOnlinePlayers()) {
				onlinePlayer.getPlayerForce().showPlayer(UhcCore.getPlugin(), player);
			}
		}catch (UhcPlayerNotOnlineException ex){
			// Player gets revived next time they attempt to join.
		}
	}

}